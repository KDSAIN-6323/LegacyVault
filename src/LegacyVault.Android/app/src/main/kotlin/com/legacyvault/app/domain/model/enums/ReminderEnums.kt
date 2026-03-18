package com.legacyvault.app.domain.model.enums

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Matches ReminderTag in types/index.ts — serialized as lowercase strings in page content JSON. */
@Serializable
enum class ReminderTag {
    @SerialName("birthday")    birthday,
    @SerialName("anniversary") anniversary,
    @SerialName("holiday")     holiday,
    @SerialName("appointment") appointment,
    @SerialName("custom")      custom;

    val label: String get() = when (this) {
        birthday    -> "Birthday"
        anniversary -> "Anniversary"
        holiday     -> "Holiday"
        appointment -> "Appointment"
        custom      -> "Custom"
    }
}

/** Matches ReminderRecurrence in types/index.ts. */
@Serializable
enum class ReminderRecurrence {
    @SerialName("once")    once,
    @SerialName("weekly")  weekly,
    @SerialName("monthly") monthly,
    @SerialName("yearly")  yearly;

    val label: String get() = when (this) {
        once    -> "Once"
        weekly  -> "Weekly"
        monthly -> "Monthly"
        yearly  -> "Yearly"
    }
}

/** Matches NotifyUnit in types/index.ts. */
@Serializable
enum class NotifyUnit {
    @SerialName("hours") hours,
    @SerialName("days")  days,
    @SerialName("weeks") weeks;

    val label: String get() = when (this) {
        hours -> "Hours"
        days  -> "Days"
        weeks -> "Weeks"
    }
}
