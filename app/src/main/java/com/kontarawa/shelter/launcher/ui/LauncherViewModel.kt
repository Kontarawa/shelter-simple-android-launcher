package com.kontarawa.shelter.launcher.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kontarawa.shelter.launcher.data.AppItem
import com.kontarawa.shelter.launcher.data.InstalledAppsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = InstalledAppsRepository(application)

    private val _allApps = MutableStateFlow<List<AppItem>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    private val _isLoading = MutableStateFlow(false)
    private var loadJob: Job? = null
    private var searchJob: Job? = null

    val searchQuery: StateFlow<String> = _searchQuery
    val isLoading: StateFlow<Boolean> = _isLoading
    val filteredApps: StateFlow<List<AppItem>> = combine(_allApps, _searchQuery) { apps, query ->
        val q = query.trim().lowercase(Locale.ROOT)
        if (q.isBlank()) apps
        else apps.filter {
            it.label.trim().lowercase(Locale.ROOT).contains(q) ||
                    it.packageName.trim().lowercase(Locale.ROOT).contains(q)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        loadApps()
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.update { query }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
        }
    }

    fun refreshApps() {
        loadApps()
    }

    private fun loadApps() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _isLoading.value = true
            try {
                _allApps.value = repository.getLaunchableApps()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
