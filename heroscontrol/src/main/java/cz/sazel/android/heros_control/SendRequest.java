package cz.sazel.android.heros_control;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by wojta on 16.5.14.
 */
public class SendRequest {


    private final Id mId;

    public SendRequest(Id id) {
        mId = id;
    }

    private HttpsURLConnection connect() throws IOException {
        URL url = new URL(Constants.GCM_API_URL);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "key=" + Constants.GCM_API_KEY);
        connection.setChunkedStreamingMode(0);
        return connection;
    }

    public void changeOSEvent(String name, int colorVariant) throws IOException {
//        {
//            "registration_ids" : ["APA91bHOn9uAcJbnQTc_21JEkTlIlcJVgj4r3_twx4ItAIuEyu9aFjg8PJhqD8cJAffD0ZAGYkYooIKUdNIGjeOWEwq7x7P5lxA3BWKh3XMUkDIzHKJYt8nx6Y5s9jGn2j32ncF53rl0IQ4PY8jjf8eegFe9-se-FiP38qppD5TDbqxHLFF5BDM"],
//            "data" : {  "msgType":1,"name":"Petr","colorVariant":"2" },"time_to_live":0
//        }
        try {
            JSONObject json = new JSONObject();
            JSONArray ids = new JSONArray();
            ids.put(mId.id);
            json.put("registration_ids", ids);
            JSONObject data = new JSONObject();
            data.put("msgType", 1);
            data.put("name", name);
            data.put("colorVariant", colorVariant);
            json.put("data", data);
            sendRequest(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    ;

    private void sendRequest(JSONObject json) throws IOException, JSONException {
        OutputStreamWriter ow;
        HttpsURLConnection connection = null;
        Socket socket=null;
        if (!mId.isIp()) {
            connection = connect();
            ow = new OutputStreamWriter(connection.getOutputStream());
        } else {
            socket=new Socket(mId.id.trim(),12345);
            ow=new OutputStreamWriter(socket.getOutputStream());
        }
        ow.write(mId.isIp()?(json.getJSONObject("data").toString()+"\n"):json.toString());
        ow.flush();
        ow.close();

        if (connection!=null && connection.getResponseCode() != 200)
            throw new IOException("error " + connection.getResponseCode() + ":" + connection.getResponseMessage());
        if (socket!=null) socket.close();

    }

    public void otherEvent(String eventType) throws IOException {
        try {
            JSONObject json = new JSONObject();
            JSONArray ids = new JSONArray();
            ids.put(mId.id);
            json.put("registration_ids", ids);
            JSONObject data = new JSONObject();
            data.put("msgType", 2);
            data.put("otherEvent", eventType);
            json.put("data", data);
            sendRequest(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    ;

}