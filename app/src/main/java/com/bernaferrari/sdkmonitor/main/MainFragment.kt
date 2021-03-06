package com.bernaferrari.sdkmonitor.main

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import com.afollestad.rxkprefs.Pref
import com.airbnb.mvrx.*
import com.bernaferrari.base.misc.toDp
import com.bernaferrari.base.mvrx.simpleController
import com.bernaferrari.sdkmonitor.R
import com.bernaferrari.sdkmonitor.data.App
import com.bernaferrari.sdkmonitor.details.DetailsDialog
import com.bernaferrari.sdkmonitor.emptyContent
import com.bernaferrari.sdkmonitor.extensions.apiToColor
import com.bernaferrari.sdkmonitor.extensions.apiToVersion
import com.bernaferrari.sdkmonitor.loadingRow
import com.bernaferrari.sdkmonitor.util.InsetDecoration
import com.bernaferrari.sdkmonitor.util.asFlow
import com.bernaferrari.sdkmonitor.views.LogsItemModel_
import com.bernaferrari.ui.dagger.DaggerBaseSearchFragment
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import java.util.*
import javax.inject.Inject
import javax.inject.Named

class AppVersion(
    val app: App,
    val sdkVersion: Int,
    val lastUpdateTime: String
)

data class AppDetails(val title: String, val subtitle: String)

data class MainState(
  val listOfItems: Async<List<AppVersion>> = Loading()
) : MavericksState

@AndroidEntryPoint
class MainFragment : DaggerBaseSearchFragment() {

    private val viewModel: MainViewModel by fragmentViewModel()
//    @Inject
//    lateinit var mainViewModelFactory: MainViewModel.Factory
    @Inject
    @Named(value = "orderBySdk") lateinit var orderBySdk: Pref<Boolean>

    lateinit var fastScroller: View

    override val showKeyboardWhenLoaded = false

    override fun onTextChanged(searchText: String) {
        viewModel.inputRelay.tryEmit(searchText)
    }

    private val standardItemDecorator by lazy {
        val isRightToLeft =
            TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == View.LAYOUT_DIRECTION_RTL

        // the padding should be on right side, but because of RTL layouts, it can change.
        InsetDecoration(
            resources.getDimensionPixelSize(R.dimen.right_padding_for_fast_scroller),
            isRightToLeft,
            !isRightToLeft
        )
    }

    override fun epoxyController() = simpleController(viewModel) { state ->

        when (state.listOfItems) {
            is Loading ->
                loadingRow { id("loading") }
            else -> {
                if (state.listOfItems()?.isEmpty() == true) {
                    val label = if (state.listOfItems is Fail) {
                        state.listOfItems.error.localizedMessage
                    } else {
                        getString(R.string.empty_search)
                    }

                    emptyContent {
                        this.id("empty")
                        this.label(label)
                    }
                }
            }
        }

        state.listOfItems()?.forEach {
            val item = it.app

            LogsItemModel_()
                .id(item.packageName)
                .title(item.title)
                .targetSDKVersion(it.sdkVersion.toString())
                .targetSDKDescription(it.sdkVersion.apiToVersion())
                .apiColor(it.sdkVersion.apiToColor()) // 0xFF9812FF
                .packageName(item.packageName)
                .subtitle(it.lastUpdateTime)
                .onClick { v -> DetailsDialog.show(requireActivity(), it.app) }
                .addTo(this)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView.updatePadding(
            left = 8.toDp(resources),
            bottom = 8.toDp(resources),
            right = 8.toDp(resources),
            top = 8.toDp(resources)
        )

//        viewModel.inputRelay.accept(getInputText())
        viewModel.inputRelay.tryEmit(getInputText())

        fastScroller = viewContainer.inflateFastScroll()

        fastScroller.setupFastScroller(recyclerView, activity) {
            if (getModelAtPos(it) is LogsItemModel_) viewModel.itemsList.getOrNull(it) else null
        }

        setInputHint("Loading...")

        launch {
            viewModel.maxListSize.collect {
              setInputHint(resources.getQuantityString(R.plurals.searchApps, it, it))
            }

          orderBySdk.asFlow().collect { orderBySdk ->
            fastScroller.isVisible = !orderBySdk

            if (orderBySdk) {
              recyclerView.removeItemDecoration(standardItemDecorator)
            } else {
              recyclerView.addItemDecoration(standardItemDecorator)
            }
          }
        }
//        disposableManager += viewModel.maxListSize.subscriptionCount.launchIn(coroutineContext)
//        .observeOn(AndroidSchedulers.mainThread())
//            .subscribe { setInputHint(resources.getQuantityString(R.plurals.searchApps, it, it)) }

//        // observe when order changes
//        disposableManager += Injector.get().orderBySdk().observe()
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe { orderBySdk ->
//                fastScroller.isVisible = !orderBySdk
//
//                if (orderBySdk) {
//                    recyclerView.removeItemDecoration(standardItemDecorator)
//                } else {
//                    recyclerView.addItemDecoration(standardItemDecorator)
//                }
//            }
    }

    override val closeIconRes: Int? = null
}
