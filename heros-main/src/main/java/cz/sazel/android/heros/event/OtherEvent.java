package cz.sazel.android.heros.event;

/**
 * Created by wojta on 15.5.14.
 */
public class OtherEvent {

    public enum EventType { BLANK, INSTALL };

    public EventType eventType;

    public OtherEvent(EventType eventType) {
        this.eventType = eventType;
    }


}
