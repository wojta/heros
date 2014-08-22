package cz.sazel.android.heros_control;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class MyActivity extends Activity {
    private String mId;
    private String mName;
    private SendRequest mSendRequest;
    private Handler mHandler;
    private boolean mStopped;
    private boolean mIp;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mHandler=new Handler();
        mStopped=false;
        findViewById(R.id.btPrvniOS).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeOs("Laura", 1);
            }
        });

        findViewById(R.id.btDruheOS).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeOs("Robert", 2);
            }
        });

        findViewById(R.id.btBlank).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                otherEvent("BLANK");
            }
        });


        findViewById(R.id.btInstall).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                otherEvent("INSTALL");
            }
        });

    }

    private void changeOs(final String name, final int colorVariant) {
        if (mSendRequest != null) {
            AsyncTask<Void, Void, Void> atask = new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        mSendRequest.changeOSEvent(name, colorVariant);
                    } catch (final IOException e) {
                        e.printStackTrace();
                        MyActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                Toast.makeText(MyActivity.this, "Chyba!!! " + e.toString(), Toast.LENGTH_LONG).show();

                            }
                        });
                    }
                    return null;
                }

                ;
            };
            atask.execute();
        }
    }

    private void otherEvent(final String type) {
        if (mSendRequest != null) {
            AsyncTask<Void, Void, Void> atask = new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        mSendRequest.otherEvent(type);
                    } catch (final IOException e) {
                        e.printStackTrace();
                        MyActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                Toast.makeText(MyActivity.this, "Chyba!!! " + e.toString(), Toast.LENGTH_LONG).show();

                            }
                        });
                    }
                    return null;
                }

                ;
            };
            atask.execute();
        }
    }

    private void keepAlive() {
        Thread t=new Thread(new Runnable() {
            @Override
            public void run() {
                while (!mStopped) {

                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        t.start();
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mnuSelectID:
                Intent intent = new Intent(this, SelectIdActivity.class);
                startActivityForResult(intent, 1);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    mId = data.getStringExtra("id");
                    mName = data.getStringExtra("name");
                    mIp=data.getBooleanExtra("ip",false);
                    ((TextView) findViewById(R.id.txId)).setText(mName);
                    mSendRequest = new SendRequest(new SelectIdActivity.Id(mId,mName,mIp));
                }
                break;
        }


        super.onActivityResult(requestCode, resultCode, data);
    }
}
