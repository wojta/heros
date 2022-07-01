package cz.sazel.android.heros_control

import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.Socket
import java.net.URL
import javax.net.ssl.HttpsURLConnection

/**
 * Created by wojta on 16.5.14.
 */
class SendRequest(private val mId: Id) {

    @Throws(IOException::class)
    private fun connect(): HttpsURLConnection {
        val url = URL(Constants.GCM_API_URL)
        val connection = url.openConnection() as HttpsURLConnection
        connection.requestMethod = "POST"
        connection.doOutput = true
        connection.doInput = true
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Authorization", "key=" + Constants.GCM_API_KEY)
        connection.setChunkedStreamingMode(0)
        return connection
    }

    @Throws(IOException::class)
    fun changeOSEvent(name: String, colorVariant: Int) {
        //        {
        //            "registration_ids" : ["APA91bHOn9uAcJbnQTc_21JEkTlIlcJVgj4r3_twx4ItAIuEyu9aFjg8PJhqD8cJAffD0ZAGYkYooIKUdNIGjeOWEwq7x7P5lxA3BWKh3XMUkDIzHKJYt8nx6Y5s9jGn2j32ncF53rl0IQ4PY8jjf8eegFe9-se-FiP38qppD5TDbqxHLFF5BDM"],
        //            "data" : {  "msgType":1,"name":"Petr","colorVariant":"2" },"time_to_live":0
        //        }
        try {
            val json = JSONObject()
            val ids = JSONArray()
            ids.put(mId.id)
            json.put("registration_ids", ids)
            val data = JSONObject()
            data.put("msgType", 1)
            data.put("name", name)
            data.put("colorVariant", colorVariant)
            json.put("data", data)
            sendRequest(json)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }

    @Throws(IOException::class, JSONException::class)
    private fun sendRequest(json: JSONObject) {
        val pw: PrintWriter
        var connection: HttpsURLConnection? = null
        var socket: Socket? = null
        Log.i(TAG, "<==== Sending request ${json.toString(2)}")
        if (!mId.isIp) {
            connection = connect()
            pw = PrintWriter(OutputStreamWriter(connection.outputStream))
        } else {
            socket = Socket(mId.id.trim { it <= ' ' }, 12345)
            pw = PrintWriter(OutputStreamWriter(socket.getOutputStream()))
        }
        try {
            pw.println(if (mId.isIp) json.getJSONObject("data").toString() else json.toString())
            if (connection != null && connection.responseCode != 200)
                throw IOException("error " + connection.responseCode + ":" + connection.responseMessage)
        } finally {
            pw.close()
            socket?.close()
        }
        Log.i(TAG, "Send request complete ${json.toString(2)} =====>")
    }

    @Throws(IOException::class)
    fun otherEvent(eventType: String) {
        try {
            val json = JSONObject()
            val ids = JSONArray()
            ids.put(mId.id)
            json.put("registration_ids", ids)
            val data = JSONObject()
            data.put("msgType", 2)
            data.put("otherEvent", eventType)
            json.put("data", data)
            sendRequest(json)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    companion object {
        const val TAG = "SendRequest"
    }
}