package ru.mail.park.chat.api.rest;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Pair;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ru.mail.park.chat.api.ApiSection;
import ru.mail.park.chat.models.Contact;

/**
 * Created by mikrut on 07.08.16.
 */
public class P2P extends ApiSection {
    private static final String URL_ADDITION = "peer";

    @Override
    protected Uri getUrlAddition() {
        return super.getUrlAddition().buildUpon().appendPath(URL_ADDITION).build();
    }

    public P2P(@NonNull Context context) {
        super(context);
    }

    public static class OnionData {
        public String onionAddress;
        public int port;
        public byte[] publicKeyDigest;
    }

    public OnionData getOnionData(String uid) throws IOException {
        final String requestURL = "getInfo";
        final String requestMethod = "POST";

        List<Pair<String, String>> parameters = new ArrayList<>(4);
        parameters.add(new Pair<String, String>("idUser", uid));

        try {
            JSONObject response = new JSONObject(executeRequest(requestURL, requestMethod, parameters));
            final int status = response.getInt("status");
            if (status == 200) {
                JSONObject data = response.getJSONObject("data");
                JSONObject info = data.getJSONObject("infoPeer");

                OnionData result = new OnionData();
                result.onionAddress    = info.getString("onionAddress");
                result.port            = info.getInt("port");
                result.publicKeyDigest = Contact.pubkeyDigestToBlob(info.getString("pubKeyHash"));

                return result;
            } else {
                String message = response.getString("message");
                throw new IOException(message);
            }
        } catch (JSONException e) {
            throw new IOException("Server error", e);
        }
    }

}
