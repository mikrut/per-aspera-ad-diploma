package ru.mail.park.chat.api;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import info.guardianproject.netcipher.NetCipher;
import info.guardianproject.netcipher.proxy.OrbotHelper;
import ru.mail.park.chat.message_interfaces.IMessageReaction;
import ru.mail.park.chat.message_interfaces.IMessageSender;
import ru.mail.park.chat.models.AttachedFile;

/**
 * Created by 1запуск BeCompact on 29.02.2016.
 */
public class P2PServerListener extends AsyncTask<Integer,String,Void> implements IMessageSender {
    ServerSocket serverSocket;
    DataInputStream input;
    DataOutputStream output;

    IMessageReaction messageListener;
    DestinationParams destination;

    public static class DestinationParams {
        public String destinationName;
        public int destinationPort;
    }

    public P2PServerListener(IMessageReaction messageListener, DestinationParams destination) {
        this.messageListener = messageListener;
        this.destination = destination;
    }

    @Override
    public Void doInBackground(Integer... params) {
        try {
            if (destination != null) {
                NetCipher.setProxy(NetCipher.ORBOT_HTTP_PROXY);
                URL url = new URL("http", destination.destinationName, destination.destinationPort, "/");
                HttpURLConnection h = NetCipher.getHttpURLConnection(url);

                input = new DataInputStream(h.getInputStream());
                output = new DataOutputStream(h.getOutputStream());
                Log.i("P2P connection", "got a connection");
                Log.i("P2P connection", destination.destinationName);
            } else {
                int port = params[0];
                serverSocket = new ServerSocket(port);
                Socket socket = serverSocket.accept();
                Log.i("P2P Server IP", socket.getInetAddress().getCanonicalHostName());

                input = new DataInputStream(socket.getInputStream());
                output = new DataOutputStream(socket.getOutputStream());
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
        return null;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        try {
            messageListener.onIncomeMessage(new JSONObject(values[0]));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String chatID, String message, List<AttachedFile> files) {
        sendMessage(chatID, message);
    }

    public void sendMessage(String chatID, String message) {
        if (output != null) {
            Log.i("P2P Server OUT message", message);
            try {
                output.writeUTF(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendFirstMessage(String userID, String message, List<AttachedFile> files) {
        sendFirstMessage(userID, message);
    }

    public void sendFirstMessage(String userID, String message) {
        if (output != null) {
            Log.i("P2P Server OUT message", message);
            try {
                output.writeUTF(message);
            } catch (IOException e) {
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
}
