package com.sanskar.eventhive.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sanskar.eventhive.data.Resource
import com.sanskar.eventhive.data.model.Category
import com.sanskar.eventhive.data.repository.Inteface.ClubCategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject


@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ClubCategoryViewModel @Inject constructor(
    private val repo: ClubCategoryRepository
) : ViewModel() {


    val categories: StateFlow<List<Category>> = repo.getAllCategories()
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = emptyList()
        )

    private val _category = MutableStateFlow<Category?>(null)
    val category: StateFlow<Category?> = _category.asStateFlow()
    private var categoryJob: Job? = null

    fun getCategory(categoryId: String) {
        categoryJob?.cancel()
        categoryJob = viewModelScope.launch {
            repo.getCategory(categoryId).collect { category ->
                _category.value = category
            }
        }
    }



    private val _operationStatus = MutableSharedFlow<Resource<Unit>>(replay = 0)
    val operationStatus: SharedFlow<Resource<Unit>> = _operationStatus.asSharedFlow()
    // i use shared flow because when there will save , update, or delete then shared flow notify the ui once that
    // i am completed and then we can update the ui

    fun saveCategory(category: Category) = viewModelScope.launch {
        _operationStatus.emit(Resource.Loading)
        val result = repo.saveCategory(category)
        _operationStatus.emit(result)
    }

    fun updateCategory(category: Category) = viewModelScope.launch {
        _operationStatus.emit(Resource.Loading)
        val result = repo.updateCategory(category)
        _operationStatus.emit(result)
    }

    fun deleteCategory(categoryId: String) = viewModelScope.launch {
        _operationStatus.emit(Resource.Loading)
        val result = repo.deleteCategory(categoryId)
        _operationStatus.emit(result)
    }

    suspend fun saveCategoryDirect(category: Category): Resource<Unit> {
        return repo.saveCategory(category)
    }
}
