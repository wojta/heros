package cz.sazel.android.heros.base

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import cz.sazel.android.heros.base.Constants.COLOR_VARIANT
import cz.sazel.android.heros.base.Constants.MSG_TYPE
import cz.sazel.android.heros.base.Constants.NAME
import cz.sazel.android.heros.base.Constants.OTHER_EVENT

/**
 * Created on 7/16/19.
 */
class FirebaseMsgService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "onMessageReceived from ${message.from} data=${message.data}")
        val data = message.data
        val intent = Intent(Constants.ACTION_MSG_RECEIVED).apply {
            val bundle = Bundle().apply {
                putString(MSG_TYPE, data[MSG_TYPE])
                putString(OTHER_EVENT, data[OTHER_EVENT])
                putString(NAME, data[NAME])
                putString(COLOR_VARIANT, data[COLOR_VARIANT])
            }
            putExtras(bundle)
        }
        sendBroadcast(intent)
    }

    companion object {
        const val TAG = "FirebaseMsgService"
    }
}
