package cz.sazel.android.heros_control.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.sazel.android.heros_control.Constants
import cz.sazel.android.heros_control.Id
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.ProtocolException
import java.net.URL

/**
 * Created on 7/11/19.
 */
class SelectIdVM : ViewModel() {

    val idsLiveData: MutableLiveData<MutableList<Id>> = MutableLiveData()

    fun load() {
        viewModelScope.launch(Dispatchers.IO) {
            callLoad()
        }
    }

    suspend private fun callLoad() {
        val data = mutableListOf<Id>()
        try {
            val url = URL(Constants.GET_IDS_URL)
            val urlConnection = url.openConnection() as HttpURLConnection
            try {
                urlConnection.requestMethod = "GET"
                val br = BufferedReader(InputStreamReader(urlConnection.inputStream))
                var line: String? = br.readLine()
                while (line != null) {
                    val pola = line.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    if (pola.size == 2) {
                        val id = Id(pola[0], pola[1])
                        if (data.indexOf(id) == -1) data.add(id)
                    }
                    line = br.readLine()
                }
            } catch (e: ProtocolException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                urlConnection.disconnect()
            }
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            idsLiveData.postValue(data)
        }
    }
}