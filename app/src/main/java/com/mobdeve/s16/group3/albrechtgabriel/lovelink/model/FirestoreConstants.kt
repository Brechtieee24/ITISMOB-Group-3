package com.mobdeve.s16.group3.albrechtgabriel.lovelink.model

object UserConstants {
    // Collection name
    const val USERS_COLLECTION = "User"

    // Field names
    const val ID_FIELD = "_id"  // Firestore document ID, usually "_id"
    const val FIRST_NAME_FIELD = "firstName"
    const val LAST_NAME_FIELD = "lastName"
    const val EMAIL_FIELD = "email"
    const val COMMITTEE_FIELD = "committee"
    const val IS_OFFICER_FIELD = "isOfficer"
    const val ABOUT_INFO_FIELD = "aboutInfo"
    const val LAST_LOGIN_FIELD = "lastLogin"
    const val FORMATTED_RESIDENCY_TIME_FIELD = "formattedResidencyTime"
    const val TOTAL_RESIDENCY_TIME_FIELD = "totalResidencyTime"
}

object EventConstants {
    // Collection name
    const val EVENTS_COLLECTION = "Event"

    // Field names
    const val ID_FIELD = "_id"          // Firestore document ID
    const val EVENT_NAME_FIELD = "eventName"
    const val DATE_FIELD = "date"       // store as Date in Firestore
}

object ActivityParticipationConstants {
    // Collection name
    const val ACTIVITY_PARTICIPATION_COLLECTION = "ActivityParticipation"

    // Field names
    const val ID_FIELD = "_id"           // Firestore document ID
    const val MEMBER_ID_FIELD = "memberId"  // reference to User ID
    const val EVENT_ID_FIELD = "eventId"    // reference to Event ID
}

object ResidencyHoursConstants {
    // Collection name
    const val RESIDENCY_HOURS_COLLECTION = "ResidencyHours"

    // Field names
    const val ID_FIELD = "_id"          // Firestore document ID
    const val TIME_IN_FIELD = "timeIn"
    const val TIME_OUT_FIELD = "timeOut"
    const val MEMBER_ID_FIELD = "memberId"  // reference to User ID
}