package cz.sazel.android.heros.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import cz.sazel.android.heros.base.App;
import cz.sazel.android.heros.event.ChangeOSEvent;
import cz.sazel.android.heros.event.OtherEvent;

/**
 * Created by wojta on 27.4.14.
 */
public class GcmBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = GcmBroadcastReceiver.class.getSimpleName();
    public static final String CHANGE_OS_REQUEST="1";
    public static final String OTHER_REQUEST="2";
    public static final String MSG_TYPE = "msgType";
    public static final String NAME = "name";
    public static final String COLOR_VARIANT = "colorVariant";
    public static final String OTHER_EVENT = "otherEvent";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "GCM intent receiver " + intent.toString());
        App.bus().register(context);
        if (CHANGE_OS_REQUEST.equals(intent.getStringExtra(MSG_TYPE))) {
            App.bus().post(new ChangeOSEvent(intent.getStringExtra(NAME), Integer.valueOf(intent.getStringExtra(COLOR_VARIANT))));
        } else if (OTHER_REQUEST.equals(intent.getStringExtra(MSG_TYPE))) {
            App.bus().post(new OtherEvent(OtherEvent.EventType.valueOf(intent.getStringExtra(OTHER_EVENT))));
        }
    }


}
