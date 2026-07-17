package com.sanskar.eventhive.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sanskar.eventhive.data.Resource
import com.sanskar.eventhive.data.model.Team
import com.sanskar.eventhive.data.model.Ticket
import com.sanskar.eventhive.data.model.User
import com.sanskar.eventhive.data.repository.Inteface.TicketRepository
import com.sanskar.eventhive.domain.CancelTicketUseCase
import com.sanskar.eventhive.domain.IssueTicketUseCase
import com.sanskar.eventhive.domain.UpdateTicketUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject



@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class TicketViewModel @Inject constructor(
    private val repo: TicketRepository,
    private val issueTicketUseCase: IssueTicketUseCase,
    private val updateTicketUseCase: UpdateTicketUseCase,
    private val cancelTicketUseCase: CancelTicketUseCase
) : ViewModel() {

    val allTickets: StateFlow<List<Ticket>> = repo.getAllTickets()
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = emptyList()
        )

    private val _ticketsForEvent = MutableStateFlow<List<Ticket>>(emptyList())
    val ticketsForEvent: StateFlow<List<Ticket>> = _ticketsForEvent.asStateFlow()
    private var ticketsForEventJob: Job? = null

    fun getTicketsForEvent(categoryId: String, clubId: String, eventId: String) {
        ticketsForEventJob?.cancel()
        ticketsForEventJob = viewModelScope.launch {
            repo.getAllTicketsForEvent(categoryId, clubId, eventId).collect { list ->
                _ticketsForEvent.value = list
            }
        }
    }

    private val _ticket = MutableStateFlow<Ticket?>(null)
    val ticket: StateFlow<Ticket?> = _ticket.asStateFlow()
    private var ticketJob: Job? = null

    fun getTicket(categoryId: String, clubId: String, eventId: String, ticketId: String) {
        ticketJob?.cancel()
        ticketJob = viewModelScope.launch {
            repo.getTicket(categoryId, clubId, eventId, ticketId).collect { ticket ->
                _ticket.value = ticket
            }
        }
    }

    private val _userTickets = MutableStateFlow<List<Ticket>>(emptyList())
    val userTickets: StateFlow<List<Ticket>> = _userTickets.asStateFlow()
    private var userTicketsJob: Job? = null

    fun getUserTickets(userId: String) {
        userTicketsJob?.cancel()
        userTicketsJob = viewModelScope.launch {
            repo.getAllTicketsForUser(userId).collect { list ->
                _userTickets.value = list
            }
        }
    }

    private val _singleUserTicket = MutableStateFlow<Ticket?>(null)
    val singleUserTicket: StateFlow<Ticket?> = _singleUserTicket.asStateFlow()
    private var singleUserTicketJob: Job? = null

    fun getSingleUserTicket(userId: String, ticketId: String) {
        singleUserTicketJob?.cancel()
        singleUserTicketJob = viewModelScope.launch {
            repo.getSingleTicketForUser(userId, ticketId).collect { ticket ->
                _singleUserTicket.value = ticket
            }
        }
    }

    // — All single‑ticket operations share this status flow —
    private val _actionStatus = MutableSharedFlow<Resource<Unit>>(replay = 0)
    val actionStatus: SharedFlow<Resource<Unit>> = _actionStatus.asSharedFlow()

    fun issueTicket(ticket: Ticket,team: Team) = viewModelScope.launch {
        _actionStatus.emit(Resource.Loading)
        val res = issueTicketUseCase(ticket,team)
        _actionStatus.emit(res)
    }

    fun updateTicket(oldParticipantIds: List<String>,ticket: Ticket,team: Team) = viewModelScope.launch {
        _actionStatus.emit(Resource.Loading)
        val res = updateTicketUseCase(oldParticipantIds,ticket,team)
        _actionStatus.emit(res)
    }

    fun cancelTicket(
        categoryId: String,
        clubId: String,
        eventId: String,
        ticketId: String,
        teamId: String,
        userId: String,
        participantIds: List<String>,
    ) = viewModelScope.launch {
        _actionStatus.emit(Resource.Loading)
        val res = cancelTicketUseCase(
            categoryId,
            clubId,
            eventId,
            ticketId,
            teamId,
            userId,
            participantIds
        )
        _actionStatus.emit(res)
    }

    fun redeemTicket(
        categoryId: String,
        clubId: String,
        eventId: String,
        ticketId: String,
        userId: String
    ) = viewModelScope.launch {
        _actionStatus.emit(Resource.Loading)
        val res = repo.redeemTicket(categoryId, clubId, eventId, ticketId,userId)
        _actionStatus.emit(res)
    }
    fun resetActionStatus() {
        viewModelScope.launch {
            _actionStatus.emit(Resource.Idle)
        }
    }

    private val _team = MutableStateFlow<Team?>(null)
    val team: StateFlow<Team?> = _team.asStateFlow()
    private var teamForTicketJob: Job? = null

    fun getTeamForTicket(ticketId: String,teamId: String) {
        teamForTicketJob?.cancel()
        teamForTicketJob = viewModelScope.launch {
            repo.getTeamForTicket(ticketId,teamId).collect { team ->
                _team.value = team
            }
        }
    }

    private val _teamsForEvent = MutableStateFlow<List<Team>>(emptyList())
    val teamsForEvent: StateFlow<List<Team>> = _teamsForEvent.asStateFlow()
    private var teamsForEventJob: Job? = null

    fun getTeamsForEvent(categoryId: String, clubId: String, eventId: String) {
        teamsForEventJob?.cancel()
        teamsForEventJob = viewModelScope.launch {
            repo.getAllTeamsForEvent(categoryId, clubId, eventId).collect { list ->
                _teamsForEvent.value = list
            }
        }
    }

    private val _membersNotRegistered = MutableStateFlow<List<User>>(emptyList())
    val membersNotRegistered: StateFlow<List<User>> = _membersNotRegistered.asStateFlow()
    private var membersNotRegisteredJob: Job? = null

    fun getMembersNotRegistered(
        categoryId: String,
        clubId: String,
        eventId: String,
        participantIds: List<String>,
        requesterUserId: String,
    ) {
        membersNotRegisteredJob?.cancel()
        membersNotRegisteredJob = viewModelScope.launch {
            repo.getAllMembersNotRegistered(
                categoryId,
                clubId,
                eventId,
                participantIds,
                requesterUserId
            ).collect { list ->
                _membersNotRegistered.value = list
            }
        }
    }



    fun getTicketForUserInEvent(eventId: String, userId: String) {
        ticketJob?.cancel()
        ticketJob = viewModelScope.launch {
            repo.getTicketForUserInEvent(eventId, userId).collect { ticket ->
                _ticket.value = ticket
            }
        }
    }
}
