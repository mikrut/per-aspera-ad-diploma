package ru.mail.park.chat.api;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import ru.mail.park.chat.message_interfaces.IMessageReaction;
import ru.mail.park.chat.message_interfaces.IMessageSender;

/**
 * Created by 1запуск BeCompact on 29.02.2016.
 */
public class P2PServerListener extends AsyncTask<Integer,String,Void> implements IMessageSender {
    ServerSocket serverSocket;
    Socket socket;
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
                socket = new Socket(destination.destinationName, destination.destinationPort);
            } else {
                int port = params[0];
                serverSocket = new ServerSocket(port);
                socket = serverSocket.accept();
            }
            Log.i("P2P Server IP", socket.getInetAddress().getCanonicalHostName());

            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());

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
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e){
            Log.e("P2P disconnect", e.getLocalizedMessage());
        }
    }
}
