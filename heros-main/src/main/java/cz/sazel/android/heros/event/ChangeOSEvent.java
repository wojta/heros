package cz.sazel.android.heros.event;

/**
 * Created by wojta on 28.4.14.
 */
public class ChangeOSEvent {

    public String name;
    public int colorVariant;

    public ChangeOSEvent(String name, int colorVariant) {
        this.name = name;
        this.colorVariant = colorVariant;
    }
}
