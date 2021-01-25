package com.bernaferrari.sdkmonitor.details

import android.app.Dialog
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import com.bernaferrari.sdkmonitor.R
import com.bernaferrari.sdkmonitor.data.App
import com.bernaferrari.sdkmonitor.extensions.darken
import com.bernaferrari.ui.extras.BaseDaggerMvRxDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.details_fragment.view.*

@AndroidEntryPoint
class DetailsDialog : BaseDaggerMvRxDialogFragment() {

    private val viewModel: DetailsViewModel by fragmentViewModel()
    private val detailsController = DetailsController()

    companion object {
        private const val TAG = "[DetailsDialog]"
        private const val KEY_APP = "app"

        fun <T> show(
            fragment: T,
            app: App
        ) where T : FragmentActivity {
            val dialog = DetailsDialog().apply {
                arguments = Bundle().apply {
                    putParcelable(KEY_APP, app)
                }
            }

            val ft = fragment.supportFragmentManager
                .beginTransaction()
                .addToBackStack(TAG)

            dialog.show(ft, TAG)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = activity ?: blowUp()

        val args = arguments ?: blowUp()
        val app = args.getParcelable(KEY_APP) as? App ?: blowUp()

        return MaterialDialog(context)
            .customView(R.layout.details_fragment, noVerticalPadding = true)
            .also { it.getCustomView().setUpViews(app) }
    }


    private fun View.setUpViews(app: App) {

        titlecontent.text = app.title

        title_bar.background = ColorDrawable(app.backgroundColor.darken.darken)

        closecontent.setOnClickListener { dismiss() }

        play_store.also {
            it.isVisible = app.isFromPlayStore
            it.setOnClickListener {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=${app.packageName}")
                )

                startActivity(intent)
            }
        }

        info.setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:" + app.packageName)
            startActivity(intent)
        }

        recycler.background = ColorDrawable(app.backgroundColor.darken)

//        val detailsController = DetailsController()
        recycler.setController(detailsController)

        viewModel.fetchAppDetails(app.packageName)
//        val packageName = app.packageName
//        val data = viewModel.fetchAppDetails(packageName)
//        if (data.isEmpty()) {
//            viewModel.removePackageName(packageName)
//            this@DetailsDialog.dismiss()
//        } else {
//            val versions = viewModel.fetchAllVersions(packageName)
//            detailsController.setData(data, versions)
//        }
    }

    override fun invalidate() {
        withState(viewModel) { state ->
            if (!state.listOfDetails.complete) return@withState

            state.listOfVersions()?.let {
//                    detailsController.setData(state.listOfDetails.invoke(), it)
                detailsController.versions = it
            }

            state.listOfDetails()?.let {
                if (it.isEmpty()) {
                    viewModel.removePackageName(state.packageName)
                    this@DetailsDialog.dismiss()
                } else {
                    detailsController.apps = it
                    viewModel.fetchAllVersions(state.packageName)
                }
            }

        }
    }

    private fun <T> blowUp(): T {
        throw IllegalStateException("Oh no!")
    }

    override fun onStart() {
        super.onStart()
        // This ensures that invalidate() is called for static screens that don't
        // subscribe to a ViewModel.
        postInvalidate()
    }
}
