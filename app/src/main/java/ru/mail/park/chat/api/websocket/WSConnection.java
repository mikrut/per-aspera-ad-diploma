package ru.mail.park.chat.api.websocket;

import android.content.Context;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.neovisionaries.ws.client.ProxySettings;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.neovisionaries.ws.client.WebSocketState;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.Proxy;

import info.guardianproject.netcipher.NetCipher;
import info.guardianproject.netcipher.proxy.OrbotHelper;
import ru.mail.park.chat.api.ApiSection;
import ru.mail.park.chat.database.PreferenceConstants;
import ru.mail.park.chat.models.OwnerProfile;

/**
 * Created by Михаил on 27.06.2016.
 */
public class WSConnection extends ApiSection {
    private static final int TIMEOUT = 0; // Don't close the socket
    protected WebSocket ws;
    protected OwnerProfile profile;

    private static final String URL_ADDITION = "ws/";

    @Override
    public Uri getUrlAddition() {
        return SERVER_URL.buildUpon().appendPath(URL_ADDITION).build();
    }

    private Uri formUrl() {
        return getUrlAddition().buildUpon().scheme("http").appendQueryParameter(ApiSection.AUTH_TOKEN_PARAMETER_NAME, profile.getAuthToken()).build();
    }

    private WebSocket createWebSocket() throws IOException {
        WebSocketFactory wsFactory = new WebSocketFactory();

        ProxySettings proxySettings = wsFactory.getProxySettings();
        boolean torStart = OrbotHelper.requestStartTor(getContext());
        if (torStart) {
            NetCipher.setProxy(NetCipher.ORBOT_HTTP_PROXY);
            Proxy netCipherProxy = NetCipher.getProxy();
            Log.v(Messages.class.getSimpleName(), netCipherProxy.address().toString());

            String[] ipPort = netCipherProxy.address().toString().split(":");
            proxySettings.setHost(ipPort[0].substring(1));
            proxySettings.setPort(Integer.valueOf(ipPort[1]));
        } else {
            boolean onlyTorIsAllowed = PreferenceManager
                    .getDefaultSharedPreferences(getContext())
                    .getBoolean(PreferenceConstants.SECURITY_PARANOID_N, true);

            if (onlyTorIsAllowed) {
                throw new IOException("Cannot establish TOR connection");
            }
        }

        Log.w("formUrl", formUrl().toString());

        return wsFactory
                .setConnectionTimeout(TIMEOUT)
                .createSocket(formUrl().toString())
                .addListener(wsAdapter)
                .connectAsynchronously();
    }

    public WSConnection(@NonNull final Context context) throws IOException {
        super(context);

        profile = new OwnerProfile(context);
        ws = createWebSocket();
    }

    private static final String TAG = WSConnection.class.getSimpleName();

    WebSocketAdapter wsAdapter = new WebSocketAdapter() {
        @Override
        public void onConnectError(WebSocket websocket, WebSocketException exception) throws Exception {
            Log.e(TAG + ".onConnnectError", exception.getLocalizedMessage());
        }

        @Override
        public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
            Log.v(TAG + ".onDisconnected", closedByServer ? "by server" : "by itself");
        }

        @Override
        public void onSendError(WebSocket websocket, WebSocketException cause, WebSocketFrame frame) throws Exception {
            Log.e(TAG + ".onSendError", cause.getLocalizedMessage());
        }

        @Override
        public void onTextMessage(WebSocket websocket, final String message) {
            dispatchTextMessage(websocket, message);
        }

        @Override
        public void onStateChanged(WebSocket websocket, WebSocketState newState) throws Exception {
            Log.v(TAG + ".onStateChanged", newState.toString());
            dispatchNewState(newState);
        }
    };

    private void dispatchTextMessage(WebSocket webSocket, final String message) {
        try {
            Log.v(Messages.class.getSimpleName(), message);
            JSONObject jsonIncome = new JSONObject(message);

            String method = jsonIncome.getString(jsonIncome.has("method") ? "method" : "typePush");
            int status = jsonIncome.getInt("status");

            if (status != 200)
                throw new IOException("Wrong income status: " + String.valueOf(status));

            dispatchJSON(method, jsonIncome);
        } catch (IOException | JSONException e) {
            Log.w(TAG + ".dispatchTextMessage", e.getLocalizedMessage());
        }
    }

    protected void dispatchJSON(@NonNull final String method, JSONObject jsonIncome) {

    }

    protected void dispatchNewState(WebSocketState newState) {

    }
}
