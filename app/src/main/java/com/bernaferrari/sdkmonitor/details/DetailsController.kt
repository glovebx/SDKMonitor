package com.bernaferrari.sdkmonitor.details

import com.airbnb.epoxy.*
import com.bernaferrari.sdkmonitor.SdkHistoryBindingModel_
import com.bernaferrari.sdkmonitor.data.Version
import com.bernaferrari.sdkmonitor.detailsText
import com.bernaferrari.sdkmonitor.extensions.convertTimestampToDate
import com.bernaferrari.sdkmonitor.main.AppDetails
import com.bernaferrari.sdkmonitor.textSeparator

internal class DetailsController : AsyncEpoxyController() {

    var apps: List<AppDetails> = emptyList()
        set(value) {
            field = value
            requestModelBuild()
        }

    var versions: List<Version> = emptyList()
        set(value) {
            field = value
            requestModelBuild()
        }

    override fun buildModels() {

        apps.forEach { app ->

            detailsText {
                id(app.title)
                this.title(app.title)
                this.subtitle(app.subtitle)
            }
        }

        textSeparator {
            id("separator")
          this.label("xxxx")
//            this.label(Injector.get().appContext().getString(R.string.target_history))
        }

        val historyModels = mutableListOf<SdkHistoryBindingModel_>()

        versions.forEach {
            historyModels.add(
                SdkHistoryBindingModel_()
                    .id(it.targetSdk)
                    .targetSDKVersion(it.targetSdk.toString())
                    .title(it.lastUpdateTime.convertTimestampToDate())
                    .version("V. Code: ${it.version}")
                    .versionName("V. Name: ${it.versionName}")
            )
        }

        CarouselModel_()
            .id("carousel")
            .models(historyModels)
            .addTo(this)
    }
}
