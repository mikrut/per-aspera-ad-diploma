package ru.mail.park.chat.api;

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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

import javax.net.ssl.SSLContext;

import ru.mail.park.chat.message_interfaces.IChatListener;
import ru.mail.park.chat.message_interfaces.IMessageSender;
import ru.mail.park.chat.message_interfaces.Jsonifier;
import ru.mail.park.chat.models.Message;
import ru.mail.park.chat.models.OwnerProfile;

// TODO: review flow and architecture in context of security
// TODO: consider using java NIO
public class P2PService extends Service implements IMessageSender {
    public static final String ACTION_START_SERVER = P2PService.class.getCanonicalName() + ".ACTION_START_SERVER";
    public static final String ACTION_START_CLIENT = P2PService.class.getCanonicalName() + ".ACTION_START_CLIENT";

    public static final String DESTINATION_URL = P2PService.class.getCanonicalName() + ".DESTINATION_URL";
    public static final String DESTINATION_PORT = P2PService.class.getCanonicalName() + ".DESTINATION_PORT";

    private static final int DEFAULT_LISTENING_PORT = 8275;

    private ServerSocket serverSocket;

    private final Object inputSynchronizer = new Object();
    private final Object outputSynchronizer = new Object();

    private volatile ObjectInputStream input;
    private volatile ObjectOutputStream output;

    private volatile IChatListener chatListener;
    private volatile boolean noStop = true;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(P2PService.class.getSimpleName(), "Starting thread");
        Server server = new Server();
        server.start();

        Log.d(P2PService.class.getSimpleName(), "Finishing onCreate");
    }

    public void sendMessage(String chatID, Message message) {
        send(message);
    }

    public void sendFirstMessage(String userID, Message message) {
        send(message);
    }

    private void send(Message message) {
        Log.i(P2PService.class.getSimpleName() + " OUT message", message.getMessageBody());
        if (output != null) {
            synchronized (outputSynchronizer) {
                if (output != null) {
                    try {
                        if (output != null) {
                            synchronized (outputSynchronizer) {
                                if (output != null) {
                                    output.writeObject(message);
                                    acknowledgeOutgoingMessage(message);
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void addListener(IChatListener chatListener) {
        this.chatListener = chatListener;
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
            closeStreams();
            Socket socket = proxyInfo.getSocketFactory().createSocket(destination, port);

            synchronized (outputSynchronizer) {
                output = new ObjectOutputStream(socket.getOutputStream());
            }
            synchronized (inputSynchronizer) {
                input = new ObjectInputStream(socket.getInputStream());
            }
            Log.i(P2PService.class.getSimpleName(), "got a connection");
            Log.i(P2PService.class.getSimpleName(), destination);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleActionStartServer(int port) {
        try {
            closeStreams();
            // FIXME: use SSLContext and security over TLS
            serverSocket = new ServerSocket(port);
            Log.i(P2PService.class.getSimpleName() + " IP", serverSocket.getInetAddress().getCanonicalHostName());
            Socket socket = serverSocket.accept();
            Log.i(P2PService.class.getSimpleName() + " IP", "Incoming: " + socket.getInetAddress().getCanonicalHostName());

            synchronized (inputSynchronizer) {
                input = new ObjectInputStream(socket.getInputStream());
            }
            synchronized (outputSynchronizer) {
                output = new ObjectOutputStream(socket.getOutputStream());
            }
            Log.i(P2PService.class.getSimpleName(), "connection finished!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleIncomingMessage(final Message message) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (chatListener != null) {
                    chatListener.onIncomeMessage(message);
                }
            }
        });
    }

    private void acknowledgeOutgoingMessage(final Message message) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (chatListener != null) {
                    chatListener.onAcknowledgeSendMessage(message);
                }
            }
        });
    }

    private class Server extends Thread {
        @Override
        public void run() {
            try {
                while (noStop) {
                    Message message = null;
                    if (input != null) {
                        synchronized (inputSynchronizer) {
                            if (input != null) {
                                Object object = input.readObject();
                                if (object instanceof Message) {
                                    message = (Message) object;
                                }
                            }
                        }
                    }

                    if (message != null) {
                        Log.i(P2PService.class.getSimpleName() + " IN message", message.toString());
                        handleIncomingMessage(message);
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void closeStreams() {
        Log.d(P2PService.class.getSimpleName(), "Closing streams");
        try {
            if (output != null) {
                synchronized (outputSynchronizer) {
                    if (output != null) {
                        output.flush();
                        output.close();
                        output = null;
                    }
                }
            }

            if (input != null) {
                synchronized (inputSynchronizer) {
                    if (input != null) {
                        input.close();
                        input = null;
                    }
                }
            }

            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isConnected() {
        return output != null && input != null;
    }

    @Override
    public void reconnect() {

    }

    @Override
    public void write(@NonNull String cid) {

    }

    @Override
    public void disconnect() {
        closeStreams();
        noStop = false;
    }

    @Override
    public void onDestroy() {
        Log.d(P2PService.class.getSimpleName(), "onDestroy");
        super.onDestroy();
        closeStreams();
        noStop = false;
    }
}
