package ru.mail.park.chat.api;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.IOException;

/**
 * Created by 1запуск BeCompact on 29.02.2016.
 */
class ApiFacade {

    private final Auth auth;
    private final Chats chats;
    private final Contacts contacts;
    private Messages messages;
    private final P2P p2p;
    private final Users users;

    private final SharedPreferences sharedPreferences;
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
/*        try {
            messages = new Messages(context);
        } catch(Exception e) {
            e.printStackTrace();
        }*/
        p2p = new P2P(sConn);
        users = new Users(context);
    }
}
