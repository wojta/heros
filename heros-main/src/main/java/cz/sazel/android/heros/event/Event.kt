package cz.sazel.android.heros.event

import cz.sazel.android.heros_control.Id

/**
 * Created on 7/15/19.
 */
sealed class Event {
    class ChangeOSEvent(var name: String, var colorVariant: Int) : Event()
    class GCMConnectedEvent(val id: Id) : Event()

    class OtherEvent(var eventType: EventType) : Event() {

        enum class EventType {
            BLANK,
            INSTALL,
            INSTALL_WITHOUT_QR,
            KEEPALIVE
        }

    }
}