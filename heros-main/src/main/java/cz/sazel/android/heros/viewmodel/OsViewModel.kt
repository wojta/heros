package cz.sazel.android.heros.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.sazel.android.heros.base.Constants
import cz.sazel.android.heros.event.Event
import cz.sazel.android.heros_control.Id
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.HttpURLConnection
import java.net.ProtocolException
import java.net.URL

/**
 * Created on 7/15/19.
 */
class OsViewModel : ViewModel() {

    fun install(onFinished: () -> Unit) {
        viewModelScope.launch {
            installInProgressLD.postValue(true)
            for (i in 0..999) {
                progressLiveData.postValue(i)
                delay(20)
            }
            onFinished()
            installInProgressLD.postValue(false)
        }
    }

    fun registerNewId(regid: Id) {
        viewModelScope.launch(Dispatchers.IO) {
            val builder = Uri.parse(Constants.REG_IDS_URL).buildUpon()
            builder.appendQueryParameter("id", regid.id)
            builder.appendQueryParameter("info", regid.name)
            val url = URL(builder.build().toString())

            val connection = url.openConnection() as HttpURLConnection
            try {

                connection.requestMethod = "GET"
                connection.doOutput = true
                connection.readTimeout = 10000
                connection.connect()
                if (connection.responseCode == 200) {
                    Log.v(TAG, "sucessfully written regid")
                    eventLiveData.postValue(Event.GCMConnectedEvent(regid))
                } else {
                    Log.e(TAG, "regid failed")
                }
            } catch (e: ProtocolException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                connection.disconnect()
            }
        }
    }


    var eventLiveData = MutableLiveData<Event>()
    var visibleLD = MutableLiveData<Boolean>()
    var blankLiveData = MutableLiveData<Boolean>()
    var progressLiveData = MutableLiveData<Int>()
    var installInProgressLD = MutableLiveData<Boolean>()

    companion object {
        const val TAG = "OsViewModel"
    }

}