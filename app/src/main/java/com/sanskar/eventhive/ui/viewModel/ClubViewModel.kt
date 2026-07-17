package com.sanskar.eventhive.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sanskar.eventhive.data.Resource
import com.sanskar.eventhive.data.model.Club
import com.sanskar.eventhive.data.model.ClubUser
import com.sanskar.eventhive.data.model.User
import com.sanskar.eventhive.data.repository.Inteface.ClubRepository
import com.sanskar.eventhive.data.repository.Inteface.UserRepository
import com.sanskar.eventhive.domain.CreateClubUseCase
import com.sanskar.eventhive.domain.DeleteClubUseCase
import com.sanskar.eventhive.domain.JoinClubUseCase
import com.sanskar.eventhive.domain.LeaveClubUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class ClubViewModel @Inject constructor(
    private val repo: ClubRepository,
    private val userRepo: UserRepository,
    private val createClubUseCase: CreateClubUseCase,  // renamed
    private val deleteClubUseCase: DeleteClubUseCase,
    private val joinClubUseCase: JoinClubUseCase,
    private val leaveClubUseCase: LeaveClubUseCase
) : ViewModel() {

    val allClubs: StateFlow<List<Club>> = repo.getAllClubs()
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = emptyList()
        )

    private val _clubByCategory = MutableStateFlow<List<Club>>(emptyList())
    val clubByCategory: StateFlow<List<Club>> = _clubByCategory.asStateFlow()
    private var clubByCategoryJob: Job? = null

    fun getClubByCategory(categoryId: String) {
        clubByCategoryJob?.cancel()
        clubByCategoryJob = viewModelScope.launch {
            repo.getAllClubsByCategory(categoryId).collect { list ->
                _clubByCategory.value = list
            }
        }
    }

    private val _club = MutableStateFlow<Club?>(null)
    val club: StateFlow<Club?> = _club.asStateFlow()
    private var clubJob: Job? = null

    fun getClub(categoryId: String, clubId: String) {
        clubJob?.cancel()
        clubJob = viewModelScope.launch {
            repo.getClub(categoryId, clubId).collect { club ->
                _club.value = club
            }
        }
    }


    // Save/update/delete operations
    private val _operationStatus = MutableSharedFlow<Resource<Unit>>(replay = 0)
    val operationStatus: SharedFlow<Resource<Unit>> = _operationStatus.asSharedFlow()

    fun createClub(club: Club,clubUser: ClubUser) = viewModelScope.launch {
        _operationStatus.emit(Resource.Loading)
        _operationStatus.emit(createClubUseCase(club, clubUser))
    }



    fun updateClub(club: Club) = viewModelScope.launch {
        _operationStatus.emit(Resource.Loading)
        _operationStatus.emit(repo.updateClub(club))
    }

    fun deleteClub(categoryId: String, clubId: String) = viewModelScope.launch {
        _operationStatus.emit(Resource.Loading)
        _operationStatus.emit(deleteClubUseCase(categoryId, clubId))
    }

    // Join/leave club
    private val _joinState = MutableStateFlow<Resource<Unit>>(Resource.Idle)
    val joinState: StateFlow<Resource<Unit>> = _joinState.asStateFlow()

    fun joinClub(categoryId: String, clubId: String, userId: String) = viewModelScope.launch {

        _operationStatus.emit(Resource.Loading)
        _operationStatus.emit(joinClubUseCase(categoryId, clubId, userId))
    }

    fun leaveClub(categoryId: String, clubId: String, userId: String) = viewModelScope.launch {

        _operationStatus.emit(Resource.Loading)
        _operationStatus.emit(leaveClubUseCase(categoryId, clubId, userId))
    }

    private val _changeVisibility = MutableStateFlow<Triple<String, String, Boolean>?>(null)
    val changeVisibilityState: StateFlow<Resource<Unit>> = _changeVisibility
        .filterNotNull()
        .flatMapLatest { (cat, club, visibility) ->
            flow {
                emit(Resource.Loading)
                emit(repo.changeClubVisibility(cat, club, visibility))
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, Resource.Idle)


    fun changeVisibility(categoryId: String, clubId: String, visibility: Boolean) {
        _changeVisibility.value = Triple(categoryId, clubId, visibility)
    }

    //club member Collection

    private val _clubAllMembers = MutableStateFlow<List<ClubUser>>(emptyList())
    val clubAllMembers: StateFlow<List<ClubUser>> = _clubAllMembers.asStateFlow()
    private var clubAllMembersJob: Job? = null

    fun getAllClubMembers(categoryId: String, clubId: String) {
        clubAllMembersJob?.cancel()
        clubAllMembersJob = viewModelScope.launch {
            repo.getAllClubMembers(categoryId, clubId).collect { list ->
                _clubAllMembers.value = list
            }
        }
    }

    private val _clubMember = MutableStateFlow<ClubUser?>(null)
    val clubMember: StateFlow<ClubUser?> = _clubMember.asStateFlow()
    private var clubMemberJob: Job? = null

    fun getClubMember(categoryId: String, clubId: String, userId: String) {
        clubMemberJob?.cancel()
        clubMemberJob = viewModelScope.launch {
            repo.getClubMember(categoryId, clubId, userId).collect { club ->
                _clubMember.value = club
            }
        }
    }

    private val _clubMemberOperationStatus = MutableSharedFlow<Resource<Unit>>(replay = 0)
    val clubMemberOperationStatus: SharedFlow<Resource<Unit>> = _clubMemberOperationStatus.asSharedFlow()

    fun clubMemberChangeRole(categoryId: String, clubId: String, userId: String, role: String) = viewModelScope.launch {
        _clubMemberOperationStatus.emit(Resource.Loading)
        _clubMemberOperationStatus.emit(repo.changeClubUserRole(categoryId, clubId, userId, role))
    }

    // Fetch all member details for a given club
    suspend fun getClubMembersDetails(club: Club): List<User> {
        val members = club.members
        val users = mutableListOf<User>()
        for (userId in members) {
            val result = userRepo.getUser(userId)
            if (result is Resource.Success && result.data != null) {
                users.add(result.data)
            }
        }
        return users
    }


}
