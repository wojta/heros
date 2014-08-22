package cz.sazel.android.heros;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import cz.sazel.android.heros.base.App;
import cz.sazel.android.heros.base.ServerService;
import cz.sazel.android.heros.event.ChangeOSEvent;
import cz.sazel.android.heros.event.OtherEvent;
import cz.sazel.android.heros.util.Utils;

public class MyActivity extends Activity {
    private static final String TAG = MyActivity.class.getSimpleName();
    private RelativeLayout rlOSMain;
    private TextView tvName;
    private ImageView ivBackground;
    private ImageView ivCircle;
    private RelativeLayout rlInstall;
    private ProgressBar pbProgress;
    private double mPreviousLevel;
    private boolean mBlank;
    private Handler mHandler;
    private TextView tvIp;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.incall);
        rlOSMain = (RelativeLayout) findViewById(R.id.rlOSMain);

        tvName = (TextView) findViewById(R.id.tvName);
        ivBackground = (ImageView) findViewById(R.id.ivBackground);
        ivCircle = (ImageView) findViewById(R.id.ivCircle);
        rlInstall=(RelativeLayout)findViewById(R.id.rlOSInstall);
        pbProgress=(ProgressBar)findViewById(R.id.progressBar);
        tvIp = (TextView) findViewById(R.id.tvIP);

        mHandler=new Handler();
        tvIp.setText(Utils.getIPAddress(true));

        Intent serviceIntent=new Intent(this, ServerService.class);
        serviceIntent.setAction(ServerService.ACTION_START);
        startService(serviceIntent);

        App.bus().register(this);
    }





    private void install(final ChangeOSEvent event) {
        rlOSMain.setVisibility(View.GONE);
        rlInstall.setVisibility(View.VISIBLE);
        pbProgress.setProgress(0);
        pbProgress.setMax(1000);
        Thread t=new Thread() {
            @Override
            public void run() {
                for (int i=0;i<1000;i++) {
                    final int finalI = i;
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            pbProgress.setProgress(finalI);
                        }
                    });
                    try {
                        sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        rlInstall.setVisibility(View.GONE);
                        rlOSMain.setVisibility(View.VISIBLE);
                        osChanged(event);
                    }
                });

            }
        };
        t.start();
    }


    @Subscribe
    public void otherEventReceived(OtherEvent event) {
        switch (event.eventType) {
            case BLANK:
                if (mBlank) {
                    rlOSMain.setVisibility(View.VISIBLE);
                } else {
                    rlOSMain.setVisibility(View.GONE);
                }
                mBlank = !mBlank;
                break;
            case INSTALL:
               //install();
                Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
//                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivityForResult(intent, 0);
                Log.v(TAG,"Scan activity started");
                break;
            default:
        }

    }


    @Subscribe
    public void osChanged(ChangeOSEvent event) {
        tvName.setText(event.name);
        rlOSMain.setVisibility(View.VISIBLE);
        switch (event.colorVariant) {
            case 1:
                ivBackground.setImageResource(R.drawable.pastel_background);
                break;
            case 2:
            default:
                ivBackground.setImageResource(R.drawable.blue_fabric);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode==RESULT_OK) {
            String contents = data.getStringExtra("SCAN_RESULT");
            ChangeOSEvent event=null;
            if (contents.contains("laura")) event=new ChangeOSEvent("Laura",1);
            else if (contents.contains("robert")) event=new ChangeOSEvent("Robert",2);
            if (event!=null) install(event);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent serviceIntent=new Intent(this, ServerService.class);
        stopService(serviceIntent);

    }
}
