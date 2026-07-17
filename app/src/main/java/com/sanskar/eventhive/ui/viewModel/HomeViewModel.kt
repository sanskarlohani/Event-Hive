package com.sanskar.eventhive.ui.viewModel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _initialLoadingShown = MutableStateFlow(false)
    val initialLoadingShown: StateFlow<Boolean> = _initialLoadingShown

    /** Call once when data has been loaded / initial animation should stop */
    fun markInitialLoadingShown() {
        _initialLoadingShown.value = true
    }
}
