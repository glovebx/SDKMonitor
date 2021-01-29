package com.bernaferrari.sdkmonitor.di

import com.bernaferrari.sdkmonitor.details.DetailsViewModel
import com.bernaferrari.sdkmonitor.logs.LogsRxViewModel
import com.bernaferrari.sdkmonitor.main.MainViewModel
import com.bernaferrari.sdkmonitor.settings.SettingsViewModel
import com.squareup.inject.assisted.dagger2.AssistedModule
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.multibindings.IntoMap

@AssistedModule
@Module(includes = [AssistedInject_AppModule::class])
@InstallIn(ApplicationComponent::class)
interface AppModule {
    @Binds
    @IntoMap
    @ViewModelKey(LogsRxViewModel::class)
    fun logsRxViewModelFactory(factory: LogsRxViewModel.Factory): AssistedViewModelFactory<*, *>

    @Binds
    @IntoMap
    @ViewModelKey(DetailsViewModel::class)
    fun detailsViewModelFactory(factory: DetailsViewModel.Factory): AssistedViewModelFactory<*, *>

    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    fun mainViewModelFactory(factory: MainViewModel.Factory): AssistedViewModelFactory<*, *>

//    @Binds
//    @IntoMap
//    @ViewModelKey(SettingsViewModel::class)
//    fun settingsViewModelFactory(factory: SettingsViewModel.Factory): AssistedViewModelFactory<*, *>
}
