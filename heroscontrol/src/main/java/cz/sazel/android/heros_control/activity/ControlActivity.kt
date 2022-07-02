package cz.sazel.android.heros_control.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
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

class ControlActivity : FragmentActivity() {

    private lateinit var binding: ControlMainBinding

    private val viewModel by lazy { ViewModelProvider(this)[ControlVM::class.java] }
    private var stopped = true

    /**
     * Called when the activity is first created.
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ControlMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        with(binding) {
            btPrvniOS.setOnClickListener {
                if (viewModel.idLiveData.value != null) {
                    viewModel.changeOs("Laura", 1)
                } else {
                    idWarningToast()
                }
            }

            btDruheOS.setOnClickListener {
                if (viewModel.idLiveData.value != null) {
                    viewModel.changeOs("Robert", 2)

                } else {
                    idWarningToast()
                }
            }

            btBlank.setOnClickListener {
                if (viewModel.idLiveData.value != null) {
                    viewModel.otherEvent(BLANK)
                } else {
                    idWarningToast()
                }
            }

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
}
