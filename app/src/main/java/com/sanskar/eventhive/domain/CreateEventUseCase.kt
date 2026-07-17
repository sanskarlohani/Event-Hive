package com.sanskar.eventhive.domain

import com.sanskar.eventhive.data.Resource
import com.sanskar.eventhive.data.model.Event
import com.sanskar.eventhive.data.repository.Inteface.ClubRepository
import com.sanskar.eventhive.data.repository.Inteface.EventRepository
import javax.inject.Inject

class CreateEventUseCase @Inject constructor(
    private val eventRepository: EventRepository,
    private val clubRepository: ClubRepository,
) {
    suspend operator fun invoke(event: Event): Resource<Unit> {
        when(val result = eventRepository.createEvent(event)){
            is Resource.Error -> return result
            is Resource.Success -> {
                return when (val clubResult = clubRepository.addEventToClub(event.categoryId, event.clubId, event.eventId)) {
                    is Resource.Error -> clubResult
                    is Resource.Success -> result
                    else -> Resource.Error(Exception("Unknown error while linking event to club"))
                }
            }
            else -> {}

        }
        return Resource.Error(Exception("Unknown error"))

    }
}
