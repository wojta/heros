package cz.sazel.android.heros.base

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat.startActivity
import cz.sazel.android.heros.OsActivity

/**
 * Created on 7/16/19.
 */
class MessageReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "message received $intent")
        Intent(context, OsActivity::class.java).apply {
            action = Constants.ACTION_MSG_RECEIVED
            addFlags(Intent.FLAG_FROM_BACKGROUND)
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtras(intent.extras ?: Bundle())
            startActivity(context, this@apply, null)
        }
    }

    companion object {
        const val TAG = "MessageReceiver"
    }
}