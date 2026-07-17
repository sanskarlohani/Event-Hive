package com.sanskar.eventhive.data.model


enum class ResultType {
    FIRST_PLACE, SECOND_PLACE, THIRD_PLACE, SPECIAL_MENTION, CONSOLATION, OTHER
}



//data class EventResult(
//    val resultId: String = System.currentTimeMillis().toString(),
//    val eventId: String = "",
//    val subEventId: String? = null,
//    val categoryId: String = "",
//    val clubId: String = "",
//    val userId: String? = null,
//    val teamMemberIds: List<String> = emptyList(),
//    val position: Int? = null, // nullable if not rank-based
//    val resultType: ResultType = ResultType.FIRST_PLACE,
//    val awardTitle: String? = null, // e.g. "Best Speaker"
//    val score: String? = null,
//    val remarks: String? = null,
//    val awardedAt: Long = System.currentTimeMillis()
//)
