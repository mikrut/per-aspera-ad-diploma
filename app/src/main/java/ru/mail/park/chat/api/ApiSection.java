package ru.mail.park.chat.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Михаил on 19.03.2016.
 */
public class ApiSection {
    private final static String AUTH_TOKEN_PARAMETER_NAME = " auth_token";
    // FIXME: use SSL connection
    private final static String SERVER_URL = "http://p30480.lab1.stud.tech-mail.ru/";

    private final String AUTH_TOKEN;
    private Context context;

    public ApiSection(@NonNull Context context) {
        this.context = context;
        SharedPreferences preferences = context.getSharedPreferences(null, Context.MODE_PRIVATE);
        AUTH_TOKEN = preferences.getString("auth_token", null);
    }

    protected String executeRequest(@NonNull String requestURL, @NonNull String requestMethod,
                                    @Nullable List<Pair<String, String>> parameters) throws IOException {
        if (parameters == null)
            parameters = new ArrayList<>(1);
        parameters.add(new Pair<>(AUTH_TOKEN_PARAMETER_NAME, AUTH_TOKEN));

        if (requestMethod.equals("GET")) {
                requestURL += getQuery(parameters);
        }

        ServerConnection serverConnection = new ServerConnection(context, getUrlAddition() + requestURL);
        serverConnection.setRequestMethod(requestMethod);

        if (!requestMethod.equals("GET")) {
            serverConnection.setParameters(getQuery(parameters));
        }

        return serverConnection.getResponse();
    }

    protected String executeRequest(@NonNull String requestURL, @NonNull String requestMethod) throws IOException {
        return executeRequest(requestURL, requestMethod, null);
    }

    protected String getAuthToken() {
        return AUTH_TOKEN;
    }

    protected Context getContext() {
        return context;
    }

    protected String getUrlAddition() {
        return SERVER_URL;
    }

    private String getQuery(List<Pair<String, String>> params)
    {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (Pair<String, String> pair : params)
        {
            if (first)
                first = false;
            else
                result.append("&");

            try {
                result.append(URLEncoder.encode(String.valueOf(pair.first), "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(String.valueOf(pair.second), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        return result.toString();
    }
}
