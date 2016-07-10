package ru.mail.park.chat.api.websocket;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.List;

import ru.mail.park.chat.api.ApiSection;
import ru.mail.park.chat.models.AttachedFile;

/**
 * Created by Михаил on 10.07.2016.
 */
public class WebSocketRequest {
    @NonNull
    private final JSONObject requestObject;
    @Nullable
    private JSONObject data;
    @Nullable
    private final String authToken;

    @NonNull
    private final String method;

    public WebSocketRequest(@NonNull String controller, @NonNull String method,
                            @Nullable String authToken) {
        requestObject = new JSONObject();
        try {
            requestObject.put("controller", controller);
            requestObject.put("method", method);
        } catch(JSONException e) {
            e.printStackTrace();
        }

        this.authToken = authToken;
        this.method = method;
    }

    public WebSocketRequest(@NonNull String controller, @NonNull String method,
                            @Nullable String authToken, @NonNull Object[][] keyValueParams) {
        this(controller, method, authToken);
        setData(keyValueParams);

    }

    public void setData(@NonNull Object[][] keyValueParams) {
        try {
            data = new JSONObject();
            data.put(ApiSection.AUTH_TOKEN_PARAMETER_NAME, authToken);

            for (Object[] pair : keyValueParams) {
                if (pair.length == 2 && pair[0] instanceof String) {
                    String key = (String) pair[0];
                    Object value = pair[1];
                    setDataParam(key, value);
                }
            }
            requestObject.put("data", data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setDataParam(String key, Object value) throws JSONException {
        if (data != null) {
            if (value instanceof Iterable) {
                Iterable valuesArray = (Iterable) value;
                JSONArray jsonArray = new JSONArray();
                for (Object arrayValue : valuesArray) {
                    jsonArray.put(arrayValue);
                }
                data.put(key, jsonArray);
            } else {
                data.put(key, value);
            }
        }
    }

    @Override
    public String toString() {
        return requestObject.toString();
    }

    @NonNull
    public String getMethod() {
        return method;
    }
}
