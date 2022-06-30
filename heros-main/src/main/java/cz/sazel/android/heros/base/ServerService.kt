package cz.sazel.android.heros.base

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import cz.sazel.android.heros.base.Constants.CHANGE_OS_REQUEST
import cz.sazel.android.heros.base.Constants.COLOR_VARIANT
import cz.sazel.android.heros.base.Constants.MSG_TYPE
import cz.sazel.android.heros.base.Constants.NAME
import cz.sazel.android.heros.base.Constants.OTHER_EVENT
import cz.sazel.android.heros.base.Constants.OTHER_REQUEST
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.ServerSocket

class ServerService : Service() {
    private var mServerThread: ServerThread? = null

    private inner class ServerThread : Thread() {
        private var mSocket: ServerSocket? = null
        private var mRunning: Boolean = false

        fun close() {
            if (mSocket != null) {
                try {
                    mSocket!!.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }

        override fun run() {
            super.run()
            mRunning = true
            try {
                mSocket = ServerSocket(PORT)
                while (mRunning) {
                    val mClientSocket = mSocket!!.accept()
                    Log.d(TAG, "Client socket accept")
                    val t = Thread {
                        try {
                            val br = BufferedReader(InputStreamReader(mClientSocket.getInputStream()))
                            val line: String? = br.readLine()

                            try {
                                line?.let {
                                    parseLine(line)
                                } ?: Log.w(TAG, "empty line")
                            } catch (e: JSONException) {
                                Log.e(TAG, "invalid JSON: $line")
                            }
                            br.close()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        } finally {
                            mClientSocket.close()
                        }
                    }
                    t.start()
                }
                mSocket!!.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        @Throws(JSONException::class)
        private fun parseLine(line: String) {
            val jsonObject = JSONObject(line)
            val intent = Intent(Constants.ACTION_MSG_RECEIVED)
            val msgType = jsonObject.getString(MSG_TYPE)
            intent.putExtra(MSG_TYPE, msgType)
            if (CHANGE_OS_REQUEST == msgType) {
                intent.putExtra(NAME, jsonObject.getString(NAME))
                intent.putExtra(
                    COLOR_VARIANT,
                    jsonObject.getString(COLOR_VARIANT)
                )
            } else if (OTHER_REQUEST == msgType) {
                intent.putExtra(
                    OTHER_EVENT,
                    jsonObject.getString(OTHER_EVENT)
                )
            }
            sendBroadcast(intent)
        }

        fun stopListening() {
            mRunning = false
            close()
        }

    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "service created")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (ACTION_START == intent.action) {
            if (mServerThread == null) {
                mServerThread = ServerThread()
            }
            mServerThread!!.start()
            Log.d(TAG, "listening thread started")
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder? {
        // TODO: Return the communication channel to the service.
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onDestroy() {
        mServerThread!!.stopListening()
        mServerThread = null
        Log.d(TAG, "service destroyed")
        super.onDestroy()
    }

    companion object {

        val ACTION_START = Constants.PACKAGE + ".ACTION_START"
        private val TAG = ServerService::class.java.simpleName
        const val PORT = 12345
    }
}
