package cz.sazel.android.heros_control.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import cz.sazel.android.heros_control.Constants
import cz.sazel.android.heros_control.Constants.Events.KEEPALIVE
import cz.sazel.android.heros_control.R
import cz.sazel.android.heros_control.SendRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.IOException

/**
 * Created on 7/11/19.
 */
class ControlVM(val app: Application) : AndroidViewModel(app) {

    var idLiveData = MutableLiveData<String>()
    var nameLiveData = MutableLiveData<String>()
    var ipLiveData = MutableLiveData<Boolean>()
    var toastLiveData = MutableLiveData<String>()
    var buttonsEnabledLiveData = MutableLiveData<Boolean>()

    var sendRequest: SendRequest? = null
    var lastCommandLD = MutableLiveData<String>()


    fun keepAlive() {
        viewModelScope.launch(Dispatchers.IO) {
            var wasError = true
            while (isActive) {
                if (sendRequest != null) {
                    try {
                        sendRequest!!.otherEvent(KEEPALIVE.name)
                        if (wasError) {
                            lastCommandLD.postValue("Connection ok!")
                            wasError=false
                        }
                    } catch (e: IOException) {
                        lastCommandLD.postValue(app.getString(R.string.connectionError))
                        wasError = true
                    }
                }
                delay(5000)
            }
        }
    }

    private fun sendRequest(fn: ((SendRequest).() -> Unit), text: String = "OK") {
        if (sendRequest != null) {
            val request = sendRequest!!
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    fn.invoke(request)
                    lastCommandLD.postValue("${app.getString(R.string.lastCmd)}\n$text")
                } catch (e: IOException) {
                    e.printStackTrace()
                    lastCommandLD.postValue(app.getString(R.string.connectionError))
                } finally {
                    buttonsEnabledLiveData.postValue(true)
                }
            }
        }
    }

    fun changeOs(name: String, colorVariant: Int) {
        buttonsEnabledLiveData.postValue(false)
        sendRequest({
            changeOSEvent(name, colorVariant)
        }, "ChangeOS($name)")
    }

    fun otherEvent(type: Constants.Events) {
        buttonsEnabledLiveData.postValue(false)
        sendRequest({
            otherEvent(type.name)
        }, "Other(${type.name})")
    }

    companion object {
        const val TAG="ControlVM"
    }
}