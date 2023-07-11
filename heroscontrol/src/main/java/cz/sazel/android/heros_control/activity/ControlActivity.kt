package cz.sazel.android.heros_control.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.KeyEvent
import android.view.KeyEvent.KEYCODE_B
import android.view.KeyEvent.KEYCODE_F
import android.view.KeyEvent.KEYCODE_J
import android.view.KeyEvent.KEYCODE_L
import android.view.KeyEvent.KEYCODE_R
import android.view.KeyEvent.KEYCODE_SPACE
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import cz.sazel.android.heros_control.Constants.Events.*
import cz.sazel.android.heros_control.Constants.ID
import cz.sazel.android.heros_control.Constants.IP
import cz.sazel.android.heros_control.Constants.NAME
import cz.sazel.android.heros_control.Id
import cz.sazel.android.heros_control.R
import cz.sazel.android.heros_control.SendRequest
import cz.sazel.android.heros_control.databinding.ControlMainBinding
import cz.sazel.android.heros_control.viewmodel.ControlVM
import java.util.Locale

class ControlActivity : FragmentActivity() {

    private lateinit var binding: ControlMainBinding

    private val viewModel by lazy { ViewModelProvider(this)[ControlVM::class.java] }
    private var stopped = true
    private val textToSpeechEngine: TextToSpeech by lazy {
        // Pass in context and the listener.
        TextToSpeech(this
        ) { status ->
            // set our locale only if init was success.
            if (status == TextToSpeech.SUCCESS) {
                Log.v(TAG,"TTS init success")
                textToSpeechEngine.language = Locale.forLanguageTag("cs")

            } else Log.w(TAG, "TTS initialization status=$status")
        }
    }
    private val prvniOSListener: (View?) -> Unit = {
        if (viewModel.idLiveData.value != null) {
            viewModel.changeOs("Laura", 1)
            textToSpeechEngine.speak("Laura", TextToSpeech.QUEUE_FLUSH, null)
        } else {
            idWarningToast()
        }
    }

    private val druhyOSListener: (View?) -> Unit = {
        if (viewModel.idLiveData.value != null) {
            viewModel.changeOs("Robert", 2)
            textToSpeechEngine.speak("Robert", TextToSpeech.QUEUE_FLUSH, null)

        } else {
            idWarningToast()
        }
    }

    private val blankOSListener: (View?) -> Unit = {
        if (viewModel.idLiveData.value != null) {
            viewModel.otherEvent(BLANK)
            textToSpeechEngine.speak("Zhasnout", TextToSpeech.QUEUE_FLUSH, null)
        } else {
            idWarningToast()
        }
    }

    /**
     * Called when the activity is first created.
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ControlMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        with(binding) {
            btPrvniOS.setOnClickListener(prvniOSListener)
            btDruheOS.setOnClickListener(druhyOSListener)
            btBlank.setOnClickListener(blankOSListener)

            btInstall.setOnClickListener {
                if (viewModel.idLiveData.value != null) {
                    btInstall.isEnabled = false
                    ckInstallUnlocked.isChecked = false
                    if (!ckWithoutQR.isChecked) {
                        viewModel.otherEvent(INSTALL)
                    } else {
                        viewModel.otherEvent(INSTALL_WITHOUT_QR)
                    }

                } else {
                    idWarningToast()
                }
            }

            ckInstallUnlocked.setOnCheckedChangeListener { buttonView, isChecked -> btInstall.isEnabled = isChecked }
            if (savedInstanceState != null) {
                val id = savedInstanceState.getString(ID) ?: ""
                txId.text = id
                viewModel.idLiveData.postValue(id)
                val name = savedInstanceState.getString(NAME) ?: ""
                viewModel.nameLiveData.postValue(name)
                val ip = savedInstanceState.getBoolean(IP)
                txId.text = name
                viewModel.sendRequest = SendRequest(Id(id, name, ip))
            }
            viewModel.toastLiveData.observe(this@ControlActivity, Observer { Toast.makeText(this@ControlActivity, it, Toast.LENGTH_LONG).show() })
            viewModel.buttonsEnabledLiveData.observe(this@ControlActivity, Observer { setEnableButtons(it) })
            viewModel.lastCommandLD.observe(this@ControlActivity, Observer { txLastCommand.text = it })
        }
    }

    override fun onDestroy() {
        textToSpeechEngine.shutdown()
        super.onDestroy()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        Log.w(TAG, "key down: $keyCode")
        when (keyCode) {
            KEYCODE_F, KEYCODE_L -> prvniOSListener(null)
            KEYCODE_R, KEYCODE_J -> druhyOSListener(null)
            KEYCODE_B -> blankOSListener(null)
            else -> super.onKeyDown(keyCode, event)
        }
        return true
    }

    private fun setEnableButtons(enableButtons: Boolean) = with(binding) {
        btPrvniOS.isEnabled = enableButtons
        btDruheOS.isEnabled = enableButtons
        btBlank.isEnabled = enableButtons
        btInstall.isEnabled = enableButtons && ckInstallUnlocked.isChecked
    }


    private fun idWarningToast() {
        Toast.makeText(this, R.string.idMustBeSelected, Toast.LENGTH_LONG).show()
    }

    override fun onBackPressed() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.closeActivityTitle).setMessage(R.string.closeActivityMessage).setPositiveButton(R.string.Yes) { dialog, which -> finish() }.setNegativeButton(R.string.No) { dialog, which -> }.show()
    }

    override fun onStart() {
        super.onStart()
        stopped = false
        viewModel.keepAlive()
    }

    override fun onStop() {
        super.onStop()
        stopped = true
    }

    override fun onSaveInstanceState(outState: Bundle) = with(binding) {
        super.onSaveInstanceState(outState)
        outState.putString(ID, txId.text.toString())
        outState.putString(NAME, viewModel.nameLiveData.value)
        outState.putBoolean(IP, viewModel.ipLiveData.value ?: false)
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        if (itemId == R.id.mnuSelectID) {
            val intent = Intent(this, SelectIdActivity::class.java)
            startActivityForResult(intent, 1)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) = with(binding) {
        when (requestCode) {
            1 -> if (resultCode == RESULT_OK && data != null) {
                val id = data.getStringExtra(ID)!!
                viewModel.idLiveData.postValue(id)
                val name = data.getStringExtra(NAME)!!
                viewModel.nameLiveData.postValue(name)
                val ip = data.getBooleanExtra(IP, false)
                viewModel.ipLiveData.postValue(ip)
                txId.text = name
                viewModel.sendRequest = SendRequest(Id(id, name, ip))
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    companion object {
        const val TAG = "ControlActivity"
    }
}
