package cz.sazel.android.heros.base;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import cz.sazel.android.heros.receiver.GcmBroadcastReceiver;

public class ServerService extends Service {

    public final static String ACTION_START = Constants.PACKAGE + ".ACTION_START";
    private static final String TAG = ServerService.class.getSimpleName();
    private ServerThread mServerThread;

    public ServerService() {

    }

    private class ServerThread extends Thread {
        private ServerSocket mSocket;
        private boolean mRunning;

        public void close() {
            if (mSocket != null) {
                try {
                    mSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void run() {
            super.run();
            mRunning = true;
            try {
                mSocket = new ServerSocket(12345);
                while (mRunning) {
                    final Socket mClientSocket = mSocket.accept();
                    Thread t = new Thread(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                BufferedReader br =
                                        new BufferedReader(new InputStreamReader(mClientSocket.getInputStream()));
                                String line;

                                while (mRunning && (line = br.readLine()) != null) {
                                    try {
                                        parseLine(line);
                                    } catch (JSONException e) {
                                        Log.e(TAG, "invalid JSON: " + line);
                                    }
                                }
                                br.close();
                                mClientSocket.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    t.start();
                }
                mSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void parseLine(String line) throws JSONException {
            JSONObject jsonObject = new JSONObject(line);
            Intent intent = new Intent(ServerService.this, GcmBroadcastReceiver.class);
            String msgType = jsonObject.getString(GcmBroadcastReceiver.MSG_TYPE);
            intent.putExtra(GcmBroadcastReceiver.MSG_TYPE, msgType);
            if (GcmBroadcastReceiver.CHANGE_OS_REQUEST.equals(msgType)) {
                intent.putExtra(GcmBroadcastReceiver.NAME, jsonObject.getString(GcmBroadcastReceiver.NAME));
                intent.putExtra(GcmBroadcastReceiver.COLOR_VARIANT,
                        jsonObject.getString(GcmBroadcastReceiver.COLOR_VARIANT));
            } else if (GcmBroadcastReceiver.OTHER_REQUEST.equals(msgType)) {
                intent.putExtra(GcmBroadcastReceiver.OTHER_EVENT,
                        jsonObject.getString(GcmBroadcastReceiver.OTHER_EVENT));
            }
            sendBroadcast(intent);
        }

        public void stopListening() {
            mRunning = false;
            close();
        }

    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "service created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (ACTION_START.equals(intent.getAction())) {
            if (mServerThread == null) {
                mServerThread = new ServerThread();
            }
            mServerThread.start();
            Log.d(TAG, "listening thread started");
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        mServerThread.stopListening();
        mServerThread = null;
        Log.d(TAG, "service destroyed");
        super.onDestroy();
    }
}
