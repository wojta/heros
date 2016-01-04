package cz.sazel.android.heros_control;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import java.io.IOException;

public class MyActivity extends Activity {

	private String mId;
	private String mName;
	private SendRequest mSendRequest;
	private Handler mHandler;
	private boolean mStopped;
	private boolean mIp;
	private Thread mKeepThread;
	private TextView tvId;
	private CheckBox mCkInstallUnlocked;
	private Button mBtInstall;
	private CheckBox mCKWithoutQR;

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.control_main);
		mHandler = new Handler();
		mStopped = false;
		mCkInstallUnlocked = (CheckBox) findViewById(R.id.ckInstallUnlocked);
		findViewById(R.id.btPrvniOS).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mId != null) {
					changeOs("Laura", 1);
				} else {
					idWarningToast();
				}
			}
		});

		findViewById(R.id.btDruheOS).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mId != null) {
					changeOs("Robert", 2);

				} else {
					idWarningToast();
				}
			}
		});

		findViewById(R.id.btBlank).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mId != null) {
					otherEvent("BLANK");
				} else {
					idWarningToast();
				}
			}
		});

		mCKWithoutQR = (CheckBox) findViewById(R.id.ckWithoutQR);
		mBtInstall = (Button) findViewById(R.id.btInstall);
		mBtInstall.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mId != null) {
					mBtInstall.setEnabled(false);
					mCkInstallUnlocked.setChecked(false);
					if (!mCKWithoutQR.isChecked()) {
						otherEvent("INSTALL");
					} else {
						otherEvent("INSTALL_WITHOUT_QR");
					}

				} else {
					idWarningToast();
				}
			}
		});

		mCkInstallUnlocked.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mBtInstall.setEnabled(isChecked);
			}
		});
		tvId = ((TextView) findViewById(R.id.txId));
		if (savedInstanceState != null) {
			String id = savedInstanceState.getString("id");
			tvId.setText(id);
			mId = id;
			mName = savedInstanceState.getString("name");
			mIp = savedInstanceState.getBoolean("ip");
			((TextView) findViewById(R.id.txId)).setText(mName);
			mSendRequest = new SendRequest(new Id(mId, mName, mIp));
		}
	}

	private void setEnableButtons(boolean enableButtons) {
		findViewById(R.id.btPrvniOS).setEnabled(enableButtons);
		findViewById(R.id.btDruheOS).setEnabled(enableButtons);
		findViewById(R.id.btBlank).setEnabled(enableButtons);
		mBtInstall.setEnabled(enableButtons && mCkInstallUnlocked.isChecked());
	}

	private void changeOs(final String name, final int colorVariant) {
		setEnableButtons(false);
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

				@Override
				protected void onPostExecute(Void aVoid) {
					super.onPostExecute(aVoid);
					setEnableButtons(true);
				}
			};
			atask.execute();
		}
	}

	private void idWarningToast() {
		Toast.makeText(this, R.string.idMustBeSelected, Toast.LENGTH_LONG).show();
	}

	private void otherEvent(final String type) {
		setEnableButtons(false);
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

				@Override
				protected void onPostExecute(Void aVoid) {
					super.onPostExecute(aVoid);
					setEnableButtons(true);
				}
			};
			atask.execute();
		}
	}

	private void keepAlive() {
		mKeepThread = new Thread(new Runnable() {

			@Override
			public void run() {
				while (!mStopped) {

					try {
						if (mSendRequest != null) {
							mSendRequest.otherEvent("KEEPALIVE");
						}
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
		mKeepThread.start();
	}

	@Override
	public void onBackPressed() {
		AlertDialog.Builder builder=new AlertDialog.Builder(this);
		builder.setTitle(R.string.closeActivityTitle).setMessage(R.string.closeActivityMessage).setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		}).setNegativeButton(R.string.No, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

			}
		}).show();
	}

	@Override
	protected void onStart() {
		super.onStart();
		mStopped = false;
		keepAlive();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("id", tvId.getText().toString());
		outState.putString("name", mName);
		outState.putBoolean("ip", mIp);

	}

	@Override
	protected void onStop() {
		super.onStop();
		mStopped = true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final int itemId = item.getItemId();
		if (itemId == R.id.mnuSelectID) {
			Intent intent = new Intent(this, SelectIdActivity.class);
			startActivityForResult(intent, 1);

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
					mIp = data.getBooleanExtra("ip", false);
					((TextView) findViewById(R.id.txId)).setText(mName);
					mSendRequest = new SendRequest(new Id(mId, mName, mIp));
				}
				break;
		}

		super.onActivityResult(requestCode, resultCode, data);
	}
}
