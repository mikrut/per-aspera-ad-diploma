package ru.mail.park.chat.api.websocket;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.List;

import ru.mail.park.chat.database.ChatsHelper;
import ru.mail.park.chat.models.Chat;
import ru.mail.park.chat.models.Contact;

/**
 * Created by mikrut on 13.07.16.
 */
public class DispatcherOfGroupEdit implements IDispatcher {
    private static final String TAG = DispatcherOfGroupEdit.class.getSimpleName();

    @Nullable
    private IGroupEditListener groupEditListener;
    @NonNull
    private final Context context;

    public DispatcherOfGroupEdit(@NonNull Context context) {
        this.context = context;
    }

    @Override
    public void dispatchJSON(@NonNull String method, JSONObject jsonIncome) {
        try {
            switch (method) {
                case "addUser":
                    dispatchAddUser(jsonIncome);
                    break;
                case "updateName":
                    dispatchUpdateName(jsonIncome);
                    break;
            }
        } catch (JSONException e) {
            Log.e(TAG + ".dispatchJSON", e.getLocalizedMessage());
        }
    }

    public void dispatchAddUser(JSONObject income) throws JSONException {
        JSONObject data = income.getJSONObject("data");

        JSONObject user = data.getJSONObject("userNew");
        String cid = data.getString("idRoom");

        try {
            Contact contact = new Contact(user, getContext());

            ChatsHelper helper = new ChatsHelper(getContext());
            Chat chat = helper.getChat(cid);
            if (chat != null) {
                List<Contact> users = chat.getChatUsers();
                users.add(contact);
                chat.setMembersCount(users.size());
                helper.saveChat(chat);
            }
            helper.close();

            if (groupEditListener != null) {
                    groupEditListener.onAddUser(cid, contact);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void dispatchUpdateName(JSONObject income) throws JSONException {
        JSONObject data = income.getJSONObject("data");

        String cid = data.getString("idRoom");
        String name = data.getString("name");

        ChatsHelper helper = new ChatsHelper(getContext());
        Chat chat = helper.getChat(cid);
        if (chat != null) {
            chat.setName(name);
            helper.saveChat(chat);
        }
        helper.close();

        if (groupEditListener != null) {
            groupEditListener.onUpdateName(cid, name);
        }
    }

    @NonNull
    public Context getContext() {
        return context;
    }

    public void setGroupEditListener(@Nullable IGroupEditListener groupEditListener) {
        this.groupEditListener = groupEditListener;
    }
}
