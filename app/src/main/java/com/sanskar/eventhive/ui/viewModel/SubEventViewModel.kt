package com.sanskar.eventhive.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.sanskar.eventhive.data.Resource
import com.sanskar.eventhive.data.model.SubEvent
import com.sanskar.eventhive.data.repository.Inteface.SubEventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubEventViewModel @Inject constructor(
    private val repository: SubEventRepository
) : ViewModel() {








    var subEvents by mutableStateOf<List<SubEvent>>(emptyList())
        private set
    var isLoadingSubEvents by mutableStateOf(false)
        private set
    var subEventsError by mutableStateOf<String?>(null)
        private set

    // --- Fetch all sub-events ---
    fun fetchSubEvents(categoryId: String, clubId: String, eventId: String) {
        viewModelScope.launch {
            isLoadingSubEvents = true
            subEventsError = null
            when (val result = repository.fetchSubEvents(categoryId, clubId, eventId)) {
                is Resource.Success -> subEvents = result.data.orEmpty()
                is Resource.Error -> subEventsError = result.exception.localizedMessage
                else -> {}
            }
            isLoadingSubEvents = false
        }
    }


    // --- Single SubEvent ---
    var selectedSubEvent by mutableStateOf<SubEvent?>(null)
        private set
    var isLoadingSubEvent by mutableStateOf(false)
        private set
    var subEventError by mutableStateOf<String?>(null)
        private set
    // --- Fetch single sub-event ---
    fun fetchSubEvent(categoryId: String, clubId: String, eventId: String, subEventId: String) {
        viewModelScope.launch {
            isLoadingSubEvent = true
            subEventError = null
            when (val result = repository.fetchSubEvent(categoryId, clubId, eventId, subEventId)) {
                is Resource.Success -> selectedSubEvent = result.data
                is Resource.Error -> subEventError = result.exception.localizedMessage
                else -> {}

            }
            isLoadingSubEvent = false
        }
    }

    // --- Create sub-event ---
    fun createSubEvent(
        categoryId: String,
        clubId: String,
        eventId: String,
        subEvent: SubEvent,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            when (repository.createSubEvent(categoryId, clubId, eventId, subEvent)) {
                is Resource.Success -> onComplete(true)
                is Resource.Error -> onComplete(false)
                else -> {}

            }
        }
    }

    // --- Update sub-event ---
    fun updateSubEvent(
        categoryId: String,
        clubId: String,
        eventId: String,
        subEventId: String,
        subEvent: SubEvent,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            when (repository.updateSubEvent(categoryId, clubId, eventId, subEventId, subEvent)) {
                is Resource.Success -> onComplete(true)
                is Resource.Error -> onComplete(false)
                else -> {}

            }
        }
    }

    // --- Delete sub-event ---
    fun deleteSubEvent(
        categoryId: String,
        clubId: String,
        eventId: String,
        subEventId: String,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            when (repository.deleteSubEvent(categoryId, clubId, eventId, subEventId)) {
                is Resource.Success -> onComplete(true)
                is Resource.Error -> onComplete(false)
                else -> {}

            }
        }
    }

    // --- Register / Unregister ---
    fun registerForSubEvent(
        categoryId: String,
        clubId: String,
        eventId: String,
        subEventId: String,
        userId: String,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            when (repository.registerForSubEvent(categoryId, clubId, eventId, subEventId, userId)) {
                is Resource.Success -> onComplete(true)
                is Resource.Error -> onComplete(false)
                else -> {}

            }
        }
    }

    fun unregisterFromSubEvent(
        categoryId: String,
        clubId: String,
        eventId: String,
        subEventId: String,
        userId: String,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            when (repository.unregisterFromSubEvent(categoryId, clubId, eventId, subEventId, userId)) {
                is Resource.Success -> onComplete(true)
                is Resource.Error -> onComplete(false)
                else -> {}

            }
        }
    }


    // --- Organizers ---
    var organizers by mutableStateOf<List<String>>(emptyList())
        private set
    var isLoadingOrganizers by mutableStateOf(false)
        private set
    var organizersError by mutableStateOf<String?>(null)
        private set

    // --- Fetch organizers ---
    fun fetchOrganizers(
        categoryId: String,
        clubId: String,
        eventId: String,
        subEventId: String
    ) {
        viewModelScope.launch {
            isLoadingOrganizers = true
            organizersError = null
            when (val result = repository.getSubEventOrganizers(categoryId, clubId, eventId, subEventId)) {
                is Resource.Success -> organizers = result.data.orEmpty()
                is Resource.Error -> organizersError = result.exception.localizedMessage
                else -> {}

            }
            isLoadingOrganizers = false
        }
    }


    // --- Sponsors ---
    var sponsors by mutableStateOf<List<String>>(emptyList())
        private set
    var isLoadingSponsors by mutableStateOf(false)
        private set
    var sponsorsError by mutableStateOf<String?>(null)
        private set
    // --- Fetch sponsors ---
    fun fetchSponsors(
        categoryId: String,
        clubId: String,
        eventId: String,
        subEventId: String
    ) {
        viewModelScope.launch {
            isLoadingSponsors = true
            sponsorsError = null
            when (val result = repository.getSubEventSponsors(categoryId, clubId, eventId, subEventId)) {
                is Resource.Success -> sponsors = result.data.orEmpty()
                is Resource.Error -> sponsorsError = result.exception.localizedMessage
                else -> {}

            }
            isLoadingSponsors = false
        }
    }


    // --- Tickets ---
    var tickets by mutableStateOf<List<String>>(emptyList())
        private set
    var isLoadingTickets by mutableStateOf(false)
        private set
    var ticketsError by mutableStateOf<String?>(null)
        private set

    // --- Fetch tickets ---
    fun fetchTickets(
        categoryId: String,
        clubId: String,
        eventId: String,
        subEventId: String
    ) {
        viewModelScope.launch {
            isLoadingTickets = true
            ticketsError = null
            when (val result = repository.getSubEventTickets(categoryId, clubId, eventId, subEventId)) {
                is Resource.Success -> tickets = result.data.orEmpty()
                is Resource.Error -> ticketsError = result.exception.localizedMessage
                else -> {}

            }
            isLoadingTickets = false
        }
    }

    // --- Issue ticket ---
    fun issueTicket(
        categoryId: String,
        clubId: String,
        eventId: String,
        subEventId: String,
        ticketId: String,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            when (repository.issueSubEventTicket(categoryId, clubId, eventId, subEventId, ticketId)) {
                is Resource.Success -> onComplete(true)
                is Resource.Error -> onComplete(false)
                else -> {}

            }
        }
    }

    // --- Redeem ticket ---
    fun redeemTicket(
        categoryId: String,
        clubId: String,
        eventId: String,
        subEventId: String,
        ticketId: String,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            when (repository.redeemSubEventTicket(categoryId, clubId, eventId, subEventId, ticketId)) {
                is Resource.Success -> onComplete(true)
                is Resource.Error -> onComplete(false)
                else -> {}

            }
        }
    }

    // --- Fetch my ticket by userId ---
    var myTicketId by mutableStateOf<String?>(null)
        private set
    var isLoadingMyTicket by mutableStateOf(false)
        private set
    var myTicketError by mutableStateOf<String?>(null)
        private set

    fun fetchMyTicket(
        categoryId: String,
        clubId: String,
        eventId: String,
        subEventId: String,
        userId: String
    ) {
        viewModelScope.launch {
            isLoadingMyTicket = true
            myTicketError = null
            when (val result = repository.getMySubEventTickets(categoryId, clubId, eventId, subEventId, userId)) {
                is Resource.Success -> myTicketId = result.data
                is Resource.Error -> myTicketError = result.exception.localizedMessage
                else -> {}

            }
            isLoadingMyTicket = false
        }
    }

    // --- Fetch ticket owner by ticketId ---
    var ticketOwnerId by mutableStateOf<String?>(null)
        private set
    var isLoadingTicketOwner by mutableStateOf(false)
        private set
    var ticketOwnerError by mutableStateOf<String?>(null)
        private set

    fun fetchTicketOwner(
        categoryId: String,
        clubId: String,
        eventId: String,
        subEventId: String,
        ticketId: String
    ) {
        viewModelScope.launch {
            isLoadingTicketOwner = true
            ticketOwnerError = null
            when (val result = repository.getSubEventTicket(categoryId, clubId, eventId, subEventId, ticketId)) {
                is Resource.Success -> ticketOwnerId = result.data
                is Resource.Error -> ticketOwnerError = result.exception.localizedMessage
                else -> {}

            }
            isLoadingTicketOwner = false
        }
    }
}
