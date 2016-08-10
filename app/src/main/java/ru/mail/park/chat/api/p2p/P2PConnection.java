package ru.mail.park.chat.api.p2p;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.spongycastle.asn1.x509.GeneralName;
import org.spongycastle.jcajce.provider.asymmetric.X509;
import org.spongycastle.operator.OperatorCreationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
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
import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

/**
 * Created by Михаил on 07.08.2016.
 */
public class P2PConnection implements IMessageSender {
    private static final String TAG = P2PConnection.class.getSimpleName();

    private P2PService context;
    private IP2PEventListener p2pEventListener;

    public void setP2PEventListener(IP2PEventListener p2pEventListener) {
        this.p2pEventListener = p2pEventListener;
        if (p2pEventListener == null && detachTimer == null) {
            synchronized (timerSynchronizer) {
                detachTimer = new Timer();
                detachTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        context.clearConnection();
                        synchronized (timerSynchronizer) {
                            if (detachTimer != null)
                                detachTimer.cancel();
                            detachTimer = null;
                        }
                    }
                }, 5000);
            }
        } else if (p2pEventListener != null && detachTimer != null) {
            synchronized (timerSynchronizer) {
                if (detachTimer != null)
                    detachTimer.cancel();
                detachTimer = null;
            }
        }
    }

    private final Object timerSynchronizer = new Object();
    private volatile Timer detachTimer;

    private final Object inputSynchronizer = new Object();
    private final Object outputSynchronizer = new Object();

    private String destinationUID;

    private volatile Socket socket;
    private volatile ObjectInputStream input;
    private volatile ObjectOutputStream output;

    private volatile IChatListener chatListener;
    private volatile boolean noStop = true;

    public P2PConnection(P2PService context, Socket socket, IP2PEventListener p2pEventListener) {
        this(context, socket, null, p2pEventListener);
    }

    public P2PConnection(P2PService context, Socket socket, @Nullable final String destinationUID,
                         final IP2PEventListener p2pEventListener) {
        this.context = context;
        this.destinationUID = destinationUID;
        this.p2pEventListener = p2pEventListener;

        this.socket = socket;
    }

    public void startConnecting() throws NoSuchAlgorithmException, IOException, KeyManagementException {
        Handler handler = new Handler(Looper.getMainLooper());

        Log.d(TAG, "Starting thread");
        MessagesThread messagesThread = new MessagesThread();
        messagesThread.start();

        Log.d(TAG, "Finishing onCreate");

        if (p2pEventListener instanceof IP2PConnectionStatusListener) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    ((IP2PConnectionStatusListener) p2pEventListener).onConnectionStatusChange("Applying SSL ciphers");
                }
            });
        }

        socket = wrapSocketWithSSL(socket, destinationUID != null);
        synchronized (outputSynchronizer) {
            output = new ObjectOutputStream(socket.getOutputStream());
        }

        synchronized (inputSynchronizer) {
            input = new ObjectInputStream(socket.getInputStream());
        }

        if (p2pEventListener != null) {
            final String ourDestinationUID = this.destinationUID;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    p2pEventListener.onConnectionEstablished(ourDestinationUID);
                }
            });
        }
    }

    public String getDestinationUID() {
        return destinationUID;
    }

    public void addListener(IChatListener chatListener) {
        this.chatListener = chatListener;
    }

    public void sendMessage(String chatID, Message message) {
        send(message);
    }

    public void sendFirstMessage(String userID, Message message) {
        send(message);
    }

    private void send(final Message message) {
        Log.i(TAG + " OUT message", message.getMessageBody());
        new Thread() {
            public void run() {
                if (output != null) {
                    synchronized (outputSynchronizer) {
                        if (output != null) {
                            try {
                                if (output != null) {
                                    synchronized (outputSynchronizer) {
                                        if (output != null) {
                                            output.writeObject(new Message.MessageContainer(message));
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
        }.start();
    }

    @NonNull
    private static SSLContext getSSLContext(KeyManager[] km, TrustManager[] trustManagers) throws KeyManagementException, NoSuchAlgorithmException {
        SecureRandom secureRandom = new SecureRandom();
        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        Log.d("KeyManager", Arrays.toString(km));
        Log.d("TrustManagers", Arrays.toString(trustManagers));
        sslContext.init(km, trustManagers, secureRandom);
        return sslContext;
    }

    /**
     * @return Insecure dummy TrustManager which trusts everyone
     */
    @Deprecated
    private TrustManager getTrustManager() {
        return new X509TrustManager() {
            String TAG = P2PConnection.TAG + "::TrustManager";

            private void checkAnyCertificate(X509Certificate[] chain, String authType) throws CertificateException {
                X509Certificate certificate = null;
                Pattern uidMatch = Pattern.compile("CN=(\\d+)");
                for (X509Certificate cert : chain) {
                    if (uidMatch.matcher(cert.getSubjectDN().getName()).find()) {
                        certificate = cert;
                        break;
                    }
                }

                if (certificate != null) {
                    Log.v(TAG, certificate.toString());

                    if (destinationUID == null) {
                        Matcher matcher = uidMatch.matcher(certificate.getSubjectDN().getName());
                        matcher.find();
                        destinationUID = matcher.group(1);

                        if (destinationUID == null) {
                            throw new CertificateException("No UID was found in the presented certificate");
                        }
                    }
                } else {
                    throw new CertificateException("No certificate was provided");
                }
            }

            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                Log.d(TAG, "Check client trusted");
                checkAnyCertificate(chain, authType);
            }

            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                Log.d(TAG, "Check server trusted");
                for (X509Certificate cert : chain) {
                    Log.d(TAG + ".cert", cert.toString());
                }
                checkAnyCertificate(chain, authType);
            }

            public X509Certificate[] getAcceptedIssuers() {
                Log.d(TAG, "getAcceptedIssuers");
                KeyStore commonKeyStore = SSLServerStuffFactory.getCommonKeyStore(context);
                X509Certificate cert = null;
                try {
                    cert = (X509Certificate) commonKeyStore.getCertificate(SSLServerStuffFactory.COMMON_KEY_STORE_ALIAS);
                } catch (KeyStoreException ignore ){
                    ignore.printStackTrace();
                }
                if (cert != null) {
                    Log.v(TAG, "ISSUER: " + cert.getIssuerDN().getName());
                    return new X509Certificate[]{cert};
                }
                Log.e(TAG, "No common certificate was found");
                return null;
            }
        };
    }

    /**
     * @return Insecure dummy KeyManager which trusts everyone
     */
    @Deprecated
    private KeyManager[] getKeyManagers() {
        try {
            KeyStore ks = SSLServerStuffFactory.getKeyStore(context);
            KeyManager[] kms = SSLServerStuffFactory.getKeyManagers(ks);

            Log.d(TAG + ".getKeyManagers", String.valueOf(kms.length));
            for (int i = 0; i < kms.length; i++) {
                kms[i] = new LogMan((X509KeyManager) kms[i]);
            }
            return kms;
        } catch (NoSuchAlgorithmException | CertificateException | IOException | KeyStoreException |
                UnrecoverableKeyException | NoSuchProviderException | OperatorCreationException |
                InvalidKeyException | SignatureException e) {
            e.printStackTrace();
        }
        Log.e("getKeyManagers", "Could not create..");
        return null;
    }

    private static class LogMan implements X509KeyManager {
        private static final String TAG = LogMan.class.getSimpleName();
        private final X509KeyManager km;

        public LogMan(X509KeyManager wrapped) {
            km = wrapped;
        }

        @Override
        public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket) {
            Log.d(TAG, "chooseClientAlias");
            return km.chooseClientAlias(keyType, issuers, socket);
        }

        @Override
        public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
            Log.d(TAG, "chooseServerAlias");
            return km.chooseServerAlias(keyType, issuers, socket);
        }

        @Override
        public X509Certificate[] getCertificateChain(String alias) {
            Log.d(TAG, "getCertificateChain");
            return km.getCertificateChain(alias);
        }

        @Override
        public String[] getClientAliases(String keyType, Principal[] issuers) {
            Log.d(TAG, "getClientAliases");
            return km.getClientAliases(keyType, issuers);
        }

        @Override
        public String[] getServerAliases(String keyType, Principal[] issuers) {
            Log.d(TAG, "getServerAliases");
            return km.getServerAliases(keyType, issuers);
        }

        @Override
        public PrivateKey getPrivateKey(String alias) {
            Log.d(TAG, "getPrivateKey");
            return km.getPrivateKey(alias);
        }
    }

    /**
     *
     * @param socket plain socket to wrap
     * @param isClient whether we're on a client side (needed for handshake)
     * @return SSL socket created over an existing plain socket
     * @throws IOException
     */
    private SSLSocket wrapSocketWithSSL(Socket socket, boolean isClient) throws IOException,
            NoSuchAlgorithmException, KeyManagementException {
        SSLContext sslContext;
        //if (!isClient) {
            sslContext = getSSLContext(getKeyManagers(), new TrustManager[]{getTrustManager()});
        //} else {
        //    sslContext = getSSLContext(null, new TrustManager[]{getTrustManager()});
        //}

        SSLSocketFactory factory = sslContext.getSocketFactory();
        SSLSocket sslSocket = (SSLSocket) factory.createSocket(socket,
                socket.getInetAddress().getHostAddress(),
                socket.getPort(),
                true);
        sslSocket.setUseClientMode(isClient);
        sslSocket.setNeedClientAuth(true);

        sslSocket.setEnabledProtocols(sslSocket.getSupportedProtocols());
        sslSocket.setEnabledCipherSuites(factory.getSupportedCipherSuites());

        sslSocket.getSession().invalidate(); // Invalidate session to ensure we check everything again
        sslSocket.startHandshake();

        return sslSocket;
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

    private class MessagesThread extends Thread {
        @Override
        public void run() {
            try {
                while (noStop) {
                    Message message = null;
                    if (input != null) {
                        ObjectInputStream is = null;
                        synchronized (inputSynchronizer) {
                            if (input != null) {
                                is = input;
                            }
                        }
                        if (is != null) {
                            Object object = is.readObject();
                            if (object instanceof Message.MessageContainer) {
                                message = ((Message.MessageContainer) object).toMessage();
                            }
                        }
                    }

                    if (message != null) {
                        Log.i(TAG + " IN message", message.toString());
                        handleIncomingMessage(message);
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                closeStreams();
            }
        }
    }

    public void closeStreams() {
        Log.d(TAG, "Closing streams");

        if (output != null) {
            synchronized (outputSynchronizer) {
                if (output != null) {
                    try {
                        output.flush();
                        output.close();
                        output = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        if (input != null) {
            synchronized (inputSynchronizer) {
                if (input != null) {
                    try {
                        input.close();
                        input = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        try {
            if (socket != null)
                socket.close();
            socket = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        noStop = false;

        if (p2pEventListener != null) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    p2pEventListener.onConnectionBreak();
                }
            });
        }
    }

    @Override
    public boolean isConnected() {
        return output != null && input != null;
    }

    @Override
    public void write(@NonNull String cid) {

    }

    @Override
    public void disconnect() {
        closeStreams();
        noStop = false;
    }
}
