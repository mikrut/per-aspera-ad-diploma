package ru.mail.park.chat.api;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;

import org.jivesoftware.smack.proxy.ProxyInfo;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

import ru.mail.park.chat.message_interfaces.IChatListener;
import ru.mail.park.chat.message_interfaces.IMessageSender;
import ru.mail.park.chat.message_interfaces.Jsonifier;
import ru.mail.park.chat.models.Message;
import ru.mail.park.chat.models.OwnerProfile;

/**
 * Created by 1запуск BeCompact on 29.02.2016.
 */
public class P2PServerListener extends Thread implements IMessageSender {
    ServerSocket serverSocket;
    DataInputStream input;
    DataOutputStream output;

    Activity activity;
    IChatListener messageListener;
    DestinationParams destination;

    int port = 0;

    public static class DestinationParams {
        public String destinationName;
        public int destinationPort;
    }

    public P2PServerListener(Activity activity, IChatListener messageListener, DestinationParams destination) {
        this.activity = activity;
        this.messageListener = messageListener;
        this.destination = destination;
    }

    @Override
    public void write(@NonNull String cid) {
        // TODO: something...
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void run() {
        try {
            if (destination != null) {
                final Random rndForTorCircuits = new Random();
                final String user = rndForTorCircuits.nextInt(100000) + "";
                final String pass = rndForTorCircuits.nextInt(100000) + "";
                final int proxyPort = 9050;
                final String proxyHost = "127.0.0.1";

                ProxyInfo proxyInfo = new ProxyInfo(ProxyInfo.ProxyType.SOCKS5, proxyHost, proxyPort, user, pass);
                Socket socket = proxyInfo.getSocketFactory().createSocket(destination.destinationName, destination.destinationPort);

                output = new DataOutputStream(socket.getOutputStream());
                input = new DataInputStream(socket.getInputStream());
                Log.i("P2P connection", "got a connection");
                Log.i("P2P connection", destination.destinationName);
            } else {
                serverSocket = new ServerSocket(port);
                Log.i("P2P Server IP", serverSocket.getInetAddress().getCanonicalHostName());
                Socket socket = serverSocket.accept();
                Log.i("P2P Socket IP", socket.getInetAddress().getCanonicalHostName());

                input = new DataInputStream(socket.getInputStream());
                output = new DataOutputStream(socket.getOutputStream());
                Log.i("P2P server", "connection finished!");
            }

            String message;
            while ((message = input.readUTF()) != null) {
                Log.i("P2P Server IN message", message);
                if (messageListener != null) {
                    publishProgress(message);
                }
            }

            input.close();
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void publishProgress(final String message) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                onProgressUpdate(message);
            }
        });
    }

    protected void onProgressUpdate(String... values) {
        try {
            messageListener.onIncomeMessage(new JSONObject(values[0]));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String chatID, Message message) {
        Log.i("P2P Server OUT message", message.getMessageBody());
        send(message);
    }

    public void sendFirstMessage(String userID, Message message) {
        Log.i("P2P Server OUT message", message.getMessageBody());
        send(message);
    }

    private void send(Message message) {
        if (output != null) {
            try {
                OwnerProfile owner = new OwnerProfile(activity);
                JSONObject msg = Jsonifier.jsonifyForRecieve(message, owner);
                msg.put("idMessage", String.valueOf(System.currentTimeMillis()));
                output.writeUTF(msg.toString());
                if (messageListener != null) {
                    messageListener.onAcknowledgeSendMessage(msg);
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void disconnect() {
        /*try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e){
            Log.e("P2P disconnect", e.getLocalizedMessage());
        }*/
    }

    public void reconnect() {

    }
}
