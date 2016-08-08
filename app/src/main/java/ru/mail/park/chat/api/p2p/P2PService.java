package ru.mail.park.chat.api.p2p;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
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
import java.net.URI;
import java.net.URL;
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

import ru.mail.park.chat.api.rest.Contacts;
import ru.mail.park.chat.api.rest.P2P;
import ru.mail.park.chat.api.rest.Users;
import ru.mail.park.chat.api.websocket.IChatListener;
import ru.mail.park.chat.api.websocket.IMessageSender;
import ru.mail.park.chat.database.ContactsHelper;
import ru.mail.park.chat.database.PreferenceConstants;
import ru.mail.park.chat.models.Contact;
import ru.mail.park.chat.models.Message;
import ru.mail.park.chat.security.SSLServerStuffFactory;

// TODO: review flow and architecture in context of security
// TODO: consider using java NIO
public class P2PService extends Service {
    private static final String TAG = P2PService.class.getSimpleName();

    public static final String ACTION_START_SERVER = P2PService.class.getCanonicalName() + ".ACTION_START_SERVER";
    public static final String ACTION_START_CLIENT = P2PService.class.getCanonicalName() + ".ACTION_START_CLIENT";

    public static final String DESTINATION_URL = P2PService.class.getCanonicalName() + ".DESTINATION_URL";
    public static final String DESTINATION_PORT = P2PService.class.getCanonicalName() + ".DESTINATION_PORT";

    private static final int DEFAULT_LISTENING_PORT = 8275;

    private IP2PEventListener p2pEventListener;
    public void setP2PEventListener(IP2PEventListener p2pEventListener) {
        this.p2pEventListener = p2pEventListener;
        if (connection != null) {
            synchronized (connectionLocker) {
                if (connection != null) {
                    connection.setP2PEventListener(p2pEventListener);
                }
            }
        }
    }

    private ServerSocket serverSocket;
    private volatile boolean noStop = true;
    private final Object noStopLocker = new Object();

    private volatile P2PConnection connection;
    private final Object connectionLocker = new Object();

    public P2PConnection getConnection() {
        return connection;
    }

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

    public void startClient(final String destinationServerUID, final int port) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                handleActionStartClient(destinationServerUID, port);
            }
        }).start();
    }

    private void handleActionStartClient(String destinationServerUID, int port) {
        if (connection == null) {
            synchronized (connectionLocker) {
                if (connection == null) {
                    final Random rndForTorCircuits = new Random();
                    final String user = rndForTorCircuits.nextInt(100000) + "";
                    final String pass = rndForTorCircuits.nextInt(100000) + "";
                    final int proxyPort = 9050;
                    final String proxyHost = "127.0.0.1";

                    final ContactsHelper helper = new ContactsHelper(this);
                    final Contact contact = helper.getContact(destinationServerUID);
                    helper.close();

                    final URI onionAddress = contact != null ? contact.getOnionAddress() : null;
                    final String destination = onionAddress != null ? onionAddress.toString() : null;

                    Log.d(TAG, "Destination: " + destination);
                    Log.d(TAG, "Port: " + String.valueOf(port));

                    ProxyInfo proxyInfo = new ProxyInfo(ProxyInfo.ProxyType.SOCKS5, proxyHost, proxyPort, user, pass);
                    try {
                        Socket socket = proxyInfo.getSocketFactory().createSocket(destination, port);
                        connection = new P2PConnection(this, socket, destinationServerUID, p2pEventListener);

                        Log.i(TAG, "got a connection");
                        Log.i(TAG, destination);
                    } catch (IOException | NoSuchAlgorithmException | KeyManagementException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    // TODO: implement error dispatch
    // TODO: move generation methods to (?) subclass
    private void handleActionStartServer(int port) {
        try {
            Log.d(TAG, "handleActionStartServer");

            KeyStore ks = SSLServerStuffFactory.getKeyStore(this);
            Certificate cert = ks.getCertificate(SSLServerStuffFactory.CERT_ALIAS);
            byte[] pubKeyFingerprint = SSLServerStuffFactory.getPublicKeyFingerprint(cert.getPublicKey());

            SharedPreferences preferences = getSharedPreferences(PreferenceConstants.PREFERENCE_NAME, MODE_PRIVATE);
            String onionAddress = preferences.getString(PreferenceConstants.P2P_HOSTNAME, null);

            Users users = new Users(this);
            users.updateOnion(onionAddress, pubKeyFingerprint, port);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (serverSocket == null) {
                serverSocket = new ServerSocket(port);
                Log.i(TAG + " IP", serverSocket.getInetAddress().getCanonicalHostName());
                Server serverThread = new Server();
                serverThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class Server extends Thread {
        @Override
        public void run() {
            while(noStop) {
                try {
                    Log.v(TAG, "Listening for incoming sockets");
                    Socket socket = serverSocket.accept();
                    Log.v(TAG, "Accepted a socket " + socket.getInetAddress());

                    synchronized (noStopLocker) {
                        if (noStop && connection == null) {
                            synchronized (connectionLocker) {
                                if (connection == null) {
                                    Log.i(TAG + " IP", "Incoming: " + socket.getInetAddress().getCanonicalHostName());
                                    connection = new P2PConnection(P2PService.this, socket, p2pEventListener);

                                    Log.i(TAG, "connection finished!");
                                }
                            }
                        }
                    }
                } catch (IOException | NoSuchAlgorithmException | KeyManagementException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void closeConnection() {
        if (connection != null) {
            synchronized (connectionLocker) {
                if (connection != null) {
                    connection.closeStreams();
                    connection = null;
                }
            }
        }
    }

    private void closeStreams() {
        Log.v(TAG, "Closing streams");
        closeConnection();

        if (serverSocket != null) {
            try {
                serverSocket.close();
                serverSocket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        synchronized (noStopLocker) {
            closeStreams();
            noStop = false;
        }
        super.onDestroy();
    }
}
