package ru.mail.park.chat.api;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.IOException;

/**
 * Created by 1запуск BeCompact on 29.02.2016.
 */
public class ApiFacade {

    private Auth auth;
    private Chats chats;
    private Contacts contacts;
    private Messages messages;
    private P2P p2p;
    private Users users;

    private SharedPreferences sharedPreferences;
    private ServerConnection sConn;

    public ApiFacade(Context context) {
        sharedPreferences = context.getSharedPreferences(null, Context.MODE_PRIVATE);

        try {
            sConn = new ServerConnection(context, "http://mail.ru");
        } catch (IOException e) {
            e.printStackTrace();
        }

        auth = new Auth(context);
        chats = new Chats(context);
        contacts = new Contacts(context);
        messages = new Messages(sConn);
        p2p = new P2P(sConn);
        users = new Users(sConn);
    }
}
