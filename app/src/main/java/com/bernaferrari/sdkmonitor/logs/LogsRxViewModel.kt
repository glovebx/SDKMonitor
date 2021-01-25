package com.bernaferrari.sdkmonitor.logs

import androidx.lifecycle.LiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.airbnb.mvrx.*
import com.bernaferrari.sdkmonitor.data.App
import com.bernaferrari.sdkmonitor.data.Version
import com.bernaferrari.sdkmonitor.di.AssistedViewModelFactory
import com.bernaferrari.sdkmonitor.di.DaggerMavericksViewModelFactory
import com.bernaferrari.sdkmonitor.main.MainDataSource
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

data class LogsState(
  val changesCount: Async<Int> = Uninitialized,
  val mapOfApps: Async<MutableMap<String, App>?> = Loading()
) : MavericksState

/**
 * initialState *must* be implemented as a constructor parameter.
 */
class LogsRxViewModel @AssistedInject constructor(
  @Assisted state: LogsState,
  private val mainRepository: MainDataSource
) : MavericksViewModel<LogsState>(state) {

  fun getAppList() = viewModelScope.async {
    mutableMapOf<String, App>().apply {
      mainRepository.getAppsList().forEach {
        this[it.packageName] = it
      }
    }
  }.execute { copy(mapOfApps = it) }

  fun getVersionCount() = viewModelScope.async {
      mainRepository.countNumberOfChanges()
    }.execute { copy(changesCount = it) }

  fun pagedVersion(): LiveData<PagedList<Version>> {

    val myPagingConfig = PagedList.Config.Builder()
      .setPageSize(20)
      .setPrefetchDistance(60)
      .setEnablePlaceholders(true)
      .build()

    return LivePagedListBuilder<Int, Version>(
      mainRepository.getVersionsPaged(),
      myPagingConfig
    ).build()
  }

  @AssistedInject.Factory
  interface Factory : AssistedViewModelFactory<LogsRxViewModel, LogsState> {
    override fun create(state: LogsState): LogsRxViewModel
  }

  companion object : DaggerMavericksViewModelFactory<LogsRxViewModel, LogsState>(LogsRxViewModel::class.java)

}
