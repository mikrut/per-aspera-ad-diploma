package ru.mail.park.chat.message_interfaces;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.GregorianCalendar;
import java.util.List;

import ru.mail.park.chat.api.ApiSection;
import ru.mail.park.chat.database.MessagesHelper;
import ru.mail.park.chat.database.MessengerDBHelper;
import ru.mail.park.chat.models.AttachedFile;
import ru.mail.park.chat.models.Contact;
import ru.mail.park.chat.models.Message;
import ru.mail.park.chat.models.OwnerProfile;

/**
 * Created by mikrut on 25.04.16.
 */
public class Jsonifier {
    public static JSONObject jsonifyForRecieve(Message message, OwnerProfile owner) throws JSONException {
        JSONObject data = new JSONObject();
        data.put("textMessage", message.getMessageBody());
        data.put("user", jsonifyUser(owner));
        data.put("dtCreate", MessengerDBHelper.currentFormat.format(GregorianCalendar.getInstance().getTime()));
        return data;
    }

    public static JSONObject jsonifyUser(Contact contact) throws JSONException {
        JSONObject user = new JSONObject();
        user.put("login", contact.getLogin());
        user.put("firstName", contact.getFirstName());
        user.put("lastName", contact.getLastName());
        user.put("id", contact.getUid());
        user.put("idUser", contact.getUid());
        return user;
    }
}
