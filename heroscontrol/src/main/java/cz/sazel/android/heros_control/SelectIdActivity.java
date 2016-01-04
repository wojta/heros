package cz.sazel.android.heros_control;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by wojta on 16.5.14.
 */
public class SelectIdActivity extends Activity {

	private ArrayList<Id> mList;
	private ListView lvList;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.select_id);
		mList = new ArrayList<>();
		lvList = (ListView) findViewById(R.id.lvList);
		lvList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Id lid = (Id) parent.getAdapter().getItem(position);
				Intent intent = new Intent();
				intent.putExtra("id", lid.id);
				intent.putExtra("name", lid.name);
				intent.putExtra("ip", lid.isIp());
				setResult(RESULT_OK, intent);
				finish();

			}
		});
		load();
	}

	private void load() {
		AsyncTask<Void, Void, Void> atask = new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				try {
					URL url = new URL(Constants.GET_IDS_URL);
					HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
					try {
						urlConnection.setRequestMethod("GET");
						BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
						String line = "";
						while ((line = br.readLine()) != null) {
							String[] pola = line.split(",");
							if (pola.length == 2) {
								Id id=new Id(pola[0], pola[1]);
								if (mList.indexOf(id)==-1) mList.add(id);
							}
						}
					} catch (ProtocolException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						urlConnection.disconnect();
					}
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {

				}

				return null;
			}

			@Override
			protected void onPostExecute(Void aVoid) {
				super.onPostExecute(aVoid);
				lvList.setAdapter(new ArrayAdapter<Id>(SelectIdActivity.this, android.R.layout.simple_list_item_1, android.R.id.text1, mList));
			}
		};
		atask.execute();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_addip) {
			showEnterIpDialog();
		}
		return super.onOptionsItemSelected(item);
	}

	private void showEnterIpDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		final EditText edIp = new EditText(this);
		builder.setView(edIp).setMessage(getString(R.string.enter_ip)).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				String text = String.valueOf(edIp.getText());
				mList.add(new Id(text, text, true));
				((ArrayAdapter) lvList.getAdapter()).notifyDataSetChanged();
			}
		});
		builder.show();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.select_id, menu);
		return super.onCreateOptionsMenu(menu);
	}

}