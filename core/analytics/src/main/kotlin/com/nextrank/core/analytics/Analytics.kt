package com.nextrank.core.analytics

/**
 * Событие аналитики.
 * Названия в snake_case согласно Analytics Specification.
 */
sealed interface AnalyticsEvent {
    val name: String
    val properties: Map<String, String>

    data class SimpleEvent(
        override val name: String,
        override val properties: Map<String, String> = emptyMap(),
    ) : AnalyticsEvent

    data class UserEvent(
        val userId: String?,
        override val properties: Map<String, String> = emptyMap(),
    ) : AnalyticsEvent {
        override val name: String = "user_event"
    }
}

/**
 * Интерфейс аналитики.
 * Реализация заменяема (Firebase, Amplitude, self-hosted).
 */
interface Analytics {
    fun track(event: AnalyticsEvent)
    fun setUserId(userId: String?)
    fun setUserProperty(name: String, value: String?)
    fun logError(message: String, error: Throwable? = null)
}

/**
 * Заглушка аналитики для debug-сборки.
 * Ничего не отправляет.
 */
class AnalyticsNoOp : Analytics {
    override fun track(event: AnalyticsEvent) {
        android.util.Log.d("Analytics", "Track: ${event.name} ${event.properties}")
    }

    override fun setUserId(userId: String?) {
        android.util.Log.d("Analytics", "SetUserId: $userId")
    }

    override fun setUserProperty(name: String, value: String?) {
        android.util.Log.d("Analytics", "SetProperty: $name = $value")
    }

    override fun logError(message: String, error: Throwable?) {
        android.util.Log.e("Analytics", message, error)
    }
}
