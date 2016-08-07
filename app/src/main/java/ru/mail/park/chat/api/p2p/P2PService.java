package ru.mail.park.chat.api.p2p;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.jivesoftware.smack.proxy.ProxyInfo;
import org.spongycastle.operator.OperatorCreationException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Random;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;

import ru.mail.park.chat.api.websocket.IChatListener;
import ru.mail.park.chat.api.websocket.IMessageSender;
import ru.mail.park.chat.models.Message;
import ru.mail.park.chat.security.SSLServerStuffFactory;

// TODO: review flow and architecture in context of security
// TODO: consider using java NIO
public class P2PService extends Service {
    public static final String ACTION_START_SERVER = P2PService.class.getCanonicalName() + ".ACTION_START_SERVER";
    public static final String ACTION_START_CLIENT = P2PService.class.getCanonicalName() + ".ACTION_START_CLIENT";

    public static final String DESTINATION_URL = P2PService.class.getCanonicalName() + ".DESTINATION_URL";
    public static final String DESTINATION_PORT = P2PService.class.getCanonicalName() + ".DESTINATION_PORT";

    private static final int DEFAULT_LISTENING_PORT = 8275;

    private ServerSocket serverSocket;
    private P2PConnection connection;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(final Intent intent) {
        new Thread() {
            @Override
            public void run() {
                onHandleIntent(intent);
            }
        }.start();

        return mBinder;
    }

    private final IBinder mBinder = new P2PServiceSingletonBinder();

    public class P2PServiceSingletonBinder extends Binder {
        public P2PService getService() {
            return P2PService.this;
        }
    }

    protected synchronized void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (action.equals(ACTION_START_CLIENT)) {
                String destination = intent.getStringExtra(DESTINATION_URL);
                int port = intent.getIntExtra(DESTINATION_PORT, DEFAULT_LISTENING_PORT);
                handleActionStartClient(destination, port);
            } else if (action.equals(ACTION_START_SERVER)) {
                int port = DEFAULT_LISTENING_PORT;
                handleActionStartServer(port);
            }
        }
    }

    public void handleActionStartClient(String destination, int port) {
        final Random rndForTorCircuits = new Random();
        final String user = rndForTorCircuits.nextInt(100000) + "";
        final String pass = rndForTorCircuits.nextInt(100000) + "";
        final int proxyPort = 9050;
        final String proxyHost = "127.0.0.1";

        Log.d(P2PService.class.getSimpleName(), "Destination " + destination);
        Log.d(P2PService.class.getSimpleName(), "Port " + String.valueOf(port));

        ProxyInfo proxyInfo = new ProxyInfo(ProxyInfo.ProxyType.SOCKS5, proxyHost, proxyPort, user, pass);
        try {
            Socket socket = proxyInfo.getSocketFactory().createSocket(destination, port);
            connection = new P2PConnection(this, socket, true);

            Log.i(P2PService.class.getSimpleName(), "got a connection");
            Log.i(P2PService.class.getSimpleName(), destination);
        } catch (IOException | NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }
    }

    // TODO: implement error dispatch
    // TODO: move generation methods to (?) subclass
    public void handleActionStartServer(int port) {
        try {
            serverSocket = new ServerSocket(port);
            Log.i(P2PService.class.getSimpleName() + " IP", serverSocket.getInetAddress().getCanonicalHostName());
            Socket socket = serverSocket.accept();
            Log.i(P2PService.class.getSimpleName() + " IP", "Incoming: " + socket.getInetAddress().getCanonicalHostName());
            connection = new P2PConnection(this, socket, false);

            Log.i(P2PService.class.getSimpleName(), "connection finished!");
        } catch (IOException | NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        Log.d(P2PService.class.getSimpleName(), "onDestroy");
        super.onDestroy();
    }
}
