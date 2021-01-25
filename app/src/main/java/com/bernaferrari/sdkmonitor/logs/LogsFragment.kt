package com.bernaferrari.sdkmonitor.logs

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.paging.PagedListEpoxyController
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import com.bernaferrari.sdkmonitor.*
import com.bernaferrari.sdkmonitor.data.App
import com.bernaferrari.sdkmonitor.data.Version
import com.bernaferrari.sdkmonitor.details.DetailsDialog
import com.bernaferrari.sdkmonitor.extensions.apiToColor
import com.bernaferrari.sdkmonitor.extensions.apiToVersion
import com.bernaferrari.sdkmonitor.extensions.convertTimestampToDate
import com.bernaferrari.sdkmonitor.views.LogsItemModel_
import com.bernaferrari.ui.dagger.DaggerBaseRecyclerFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LogsFragment : DaggerBaseRecyclerFragment() {

  private val viewModel: LogsRxViewModel by fragmentViewModel()

  private val pagingController = TestController()

  override fun epoxyController() = pagingController

  var mapOfApps = mapOf<String, App>()
  var numberOfApps = 0
  var hasLoaded = false

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

//        mapOfApps = viewModel.getAppList()
    viewModel.getAppList()
    viewModel.getVersionCount()

    hasLoaded = true

    viewModel.pagedVersion()
      .observe(requireActivity(), Observer {
        pagingController.submitList(it)
      })
  }

  override fun invalidate() {
    withState(viewModel) { state ->
      state.mapOfApps()?.let {
        mapOfApps = it
      }
      state.changesCount()?.let {
        numberOfApps = it
      }
    }
    super.invalidate()
  }

  inner class TestController : PagedListEpoxyController<Version>() {
    override fun buildItemModel(currentPosition: Int, item: Version?): EpoxyModel<*> {

      if (item == null) {
        // this should never happen since placeholders are disabled.
        return LogsItemBindingModel_().id("error")
      }

      val sdk = item.targetSdk

      return LogsItemModel_()
        .id(item.versionId)
        .title(mapOfApps.getValue(item.packageName).title)
        .targetSDKVersion(sdk.toString())
        .targetSDKDescription(sdk.apiToVersion())
        .apiColor(sdk.apiToColor()) // 0xFF9812FF
        .packageName(item.packageName)
        .subtitle(item.lastUpdateTime.convertTimestampToDate())
        .onClick { v ->
          DetailsDialog.show(requireActivity(), mapOfApps.getValue(item.packageName))
        }
    }

    init {
      isDebugLoggingEnabled = BuildConfig.DEBUG
    }

    override fun addModels(models: List<EpoxyModel<*>>) {

      when {
        models.isNotEmpty() -> marquee {
          id("header")
          title("TargetSDK Logs")
          subtitle("$numberOfApps changes detected")
        }
        numberOfApps == 0 && hasLoaded -> logsEmptyItem { id("empty") }
        else -> loadingRow { id("loading") }
      }

      super.addModels(models)
    }

    override fun onExceptionSwallowed(exception: RuntimeException) {
      throw exception
    }
  }
}
