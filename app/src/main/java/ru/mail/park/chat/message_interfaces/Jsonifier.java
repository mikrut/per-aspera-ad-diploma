package ru.mail.park.chat.message_interfaces;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import ru.mail.park.chat.api.ApiSection;
import ru.mail.park.chat.models.AttachedFile;

/**
 * Created by mikrut on 25.04.16.
 */
public class Jsonifier {
    public static JSONObject jsonifyForRecieve(String message) throws JSONException {
        JSONObject data = new JSONObject();
        data.put("textMessage", message);
        return data;
    }
}
