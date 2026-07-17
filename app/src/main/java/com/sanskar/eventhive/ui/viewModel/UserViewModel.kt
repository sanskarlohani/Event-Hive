package com.sanskar.eventhive.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sanskar.eventhive.data.model.User
import com.sanskar.eventhive.data.model.UserClub
import com.sanskar.eventhive.data.Resource
import com.sanskar.eventhive.data.repository.Inteface.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class UserViewModel @Inject constructor(
    private val repo: UserRepository
) : ViewModel() {



    // Real-time all users
    val allUsers: StateFlow<Resource<List<User>>> = repo.observeAllUsers()
        .map<List<User>, Resource<List<User>>> { Resource.Success(it) }
        .onStart { emit(Resource.Loading) }
        .catch { e -> emit(Resource.Error(e as Exception)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Resource.Idle)

    // Real-time single user
    private val _userId = MutableStateFlow<String?>(null)

    val user: StateFlow<Resource<User?>> = _userId
        .filterNotNull()
        .flatMapLatest { id ->
            repo.observeUser(id)
                .map<User?, Resource<User?>> { Resource.Success(it) }
                .onStart { emit(Resource.Loading) }
                .catch { e -> emit(Resource.Error(e as Exception)) }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, Resource.Idle)

    fun selectUser(userId: String) {
        _userId.value = userId
    }

    private val _currentUser = MutableStateFlow<Resource<User>>(Resource.Loading)
    val currentUser: StateFlow<Resource<User>> = _currentUser

    fun fetchCurrentUser() {
        viewModelScope.launch {
            _currentUser.value = Resource.Loading
            try {
                val user = repo.getCurrentUser()
                if (user != null) {
                    _currentUser.value = Resource.Success(user)
                } else {
                    _currentUser.value = Resource.Error(Exception("User not found"))
                }
            } catch (e: Exception) {
                _currentUser.value = Resource.Error(Exception("Failed to fetch current user"))
            }
        }
    }
    init {
        fetchCurrentUser()
    }
    // Real-time current user
    val observeUser: StateFlow<Resource<User?>> = repo.observeCurrentUser()
        .map<User?, Resource<User?>> { Resource.Success(it) }
        .onStart { emit(Resource.Loading) }
        .catch { e -> emit(Resource.Error(e as Exception)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Resource.Idle)

    // UI state for save/update/delete
    private val _operationStatus = MutableSharedFlow<Resource<Unit>>(replay = 0)
    val operationStatus: SharedFlow<Resource<Unit>> = _operationStatus.asSharedFlow()

    fun saveUser(user: User) = viewModelScope.launch {
        _operationStatus.emit(Resource.Loading)
        val result = repo.saveUser(user)
        _operationStatus.emit(result)
    }

    fun updateUser(user: User) = viewModelScope.launch {
        _operationStatus.emit(Resource.Loading)
        val result = repo.updateUser(user)
        _operationStatus.emit(result)
    }

    fun deleteUser(userId: String) = viewModelScope.launch {
        _operationStatus.emit(Resource.Loading)
        val result = repo.deleteUser(userId)
        _operationStatus.emit(result)
    }

    private val _joinedClubRefs = MutableStateFlow<List<UserClub>>(emptyList())
    val joinedClubRefs: StateFlow<List<UserClub>> = _joinedClubRefs.asStateFlow()
    private var joinedClubRefsJob: Job? = null

    fun observeJoinedClubs(userId: String) {
        joinedClubRefsJob?.cancel()
        joinedClubRefsJob = viewModelScope.launch {
            repo.observeJoinedClubsForUser(userId).collect { list ->
                _joinedClubRefs.value = list
            }
        }
    }
}
