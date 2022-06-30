package cz.sazel.android.heros

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import cz.sazel.android.heros.base.Constants
import cz.sazel.android.heros.base.MessageReceiver
import cz.sazel.android.heros.base.ServerService
import cz.sazel.android.heros.databinding.IncallBinding
import cz.sazel.android.heros.event.Event.*
import cz.sazel.android.heros.util.Utils
import cz.sazel.android.heros.viewmodel.OsViewModel
import cz.sazel.android.heros_control.Id
import java.io.IOException
import kotlin.math.abs

class OsActivity : FragmentActivity() {
    private lateinit var messageReceiver: MessageReceiver
    private var mFadeInAnimation: Animation? = null

    private val viewModel by lazy { ViewModelProvider(this)[OsViewModel::class.java] }
    private lateinit var binding: IncallBinding


    /**
     * Called when the activity is first created.
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        binding = IncallBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mFadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_anim)

        with(binding) {
            tvIP.text = Utils.getIPAddress(true)

            val serviceIntent = Intent(this@OsActivity, ServerService::class.java)
            serviceIntent.action = ServerService.ACTION_START
            startService(serviceIntent)
            viewModel.eventLiveData.observe(this@OsActivity, Observer {
                it?.let { event ->
                    when (event) {
                        is ChangeOSEvent -> osChanged(event)
                        is OtherEvent -> otherEventReceived(event)
                        is GCMConnectedEvent -> onGCMConnected(event)
                    }
                }
            })
            viewModel.visibleLD.observe(this@OsActivity, Observer {
                val visible = it ?: true
                if (visible) {
                    mFadeInAnimation!!.interpolator = ReverseInterpolator()
                } else {
                    mFadeInAnimation!!.interpolator = LinearInterpolator()
                }
                ivCircle!!.startAnimation(mFadeInAnimation)
            })
            viewModel.blankLiveData.observe(this@OsActivity, Observer {
                rlOSMain.visibility = if (it == true) View.GONE else View.VISIBLE
            })
            viewModel.progressLiveData.observe(this@OsActivity, Observer {
                progressBar.progress = it
                if (it >= 999) {
                    rlOSMain.visibility = View.VISIBLE
                    rlOSInstall.visibility = View.GONE
                    viewModel.visibleLD.postValue(true)
                }
            })
            viewModel.installInProgressLD.observe(this@OsActivity, Observer {
                rlOSMain.visibility = if (it == true) View.GONE else View.VISIBLE
                rlOSInstall.visibility = if (it == true) View.VISIBLE else View.GONE
                if (it == true) {
                    progressBar.progress = 0
                    progressBar.max = 1000
                }
            })


            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                var msg = ""
                if (!task.isSuccessful) {
                    Log.e(TAG, "Not successfully registered to Firebase")
                    return@OnCompleteListener
                }

                try {
                    val id = task.result ?: "<null>"
                    msg = "Device registered, registration ID=$id"
                    val info = Build.MODEL + ":" + Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
                    val regid = Id(id, info)
                    viewModel.registerNewId(regid)
                } catch (ex: IOException) {
                    msg = "Error :" + ex.message
                }
                Log.d(TAG, msg)
            })
        }
    }

    private fun install(event: ChangeOSEvent?) {
        viewModel.install {
            event?.apply { osChanged(this) }
        }
    }

    fun otherEventReceived(event: OtherEvent) {
        when (event.eventType) {
            OtherEvent.EventType.BLANK -> {
                viewModel.blankLiveData.postValue(!(viewModel.blankLiveData.value ?: false))
            }
            OtherEvent.EventType.INSTALL -> {
                //install();
                viewModel.visibleLD.postValue(false)
                val intent = Intent("com.google.zxing.client.android.SCAN")
                intent.putExtra("SCAN_MODE", "QR_CODE_MODE")
                //                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                try {
                    startActivityForResult(intent, 0)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(this, R.string.mustHaveQRCodeReader, Toast.LENGTH_LONG).show()
                }

                Log.v(TAG, "Scan activity started")
            }
            OtherEvent.EventType.INSTALL_WITHOUT_QR -> install(null)
            OtherEvent.EventType.KEEPALIVE -> viewModel.visibleLD.postValue(!(viewModel.visibleLD.value ?: true))
        }
    }

    override fun onStart() = with(binding) {
        super.onStart()
        ivCircle.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        messageReceiver = MessageReceiver()
        registerReceiver(messageReceiver, IntentFilter(Constants.ACTION_MSG_RECEIVED))
    }

    override fun onPause() {
        unregisterReceiver(messageReceiver)
        super.onPause()
    }


    fun osChanged(event: ChangeOSEvent?) = with(binding) {
        tvName.text = event!!.name
        when (event.colorVariant) {
            1 -> ivBackground.setImageResource(R.drawable.pastel_background)
            2 -> ivBackground.setImageResource(R.drawable.blue_fabric)
            else -> ivBackground.setImageResource(R.drawable.blue_fabric)
        }
    }

    fun onGCMConnected(event: GCMConnectedEvent) = with(binding) {
        if (event.id != null) {
            tvIP.text = Utils.getIPAddress(true) + "\n" + event.id.name
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.action == Constants.ACTION_MSG_RECEIVED) {
            intent.extras?.apply {
                when (getString(Constants.MSG_TYPE)) {
                    Constants.CHANGE_OS_REQUEST -> viewModel.eventLiveData.postValue(ChangeOSEvent(getString(Constants.NAME) ?: "", getString(Constants.COLOR_VARIANT)?.toInt() ?: 0))
                    Constants.OTHER_REQUEST -> viewModel.eventLiveData.postValue(OtherEvent(OtherEvent.EventType.valueOf(getString(Constants.OTHER_EVENT) ?: "")))
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            val contents = data?.getStringExtra("SCAN_RESULT") ?: ""
            var event: ChangeOSEvent? = null
            if (contents.contains("laura")) {
                event = ChangeOSEvent("Laura", 1)
            } else if (contents.contains("robert")) {
                event = ChangeOSEvent("Robert", 2)
            }
            if (event != null) {
                install(event)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        val serviceIntent = Intent(this, ServerService::class.java)
        stopService(serviceIntent)
    }

    private inner class ReverseInterpolator : Interpolator {

        override fun getInterpolation(paramFloat: Float): Float {
            return abs(paramFloat - 1f)
        }
    }

    companion object {

        private val TAG = OsActivity::class.java.simpleName
    }
}
