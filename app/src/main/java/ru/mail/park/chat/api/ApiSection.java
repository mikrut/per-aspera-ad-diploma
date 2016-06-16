package ru.mail.park.chat.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Pair;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import ru.mail.park.chat.database.PreferenceConstants;

/**
 * Created by Михаил on 19.03.2016.
 */
public class ApiSection {
    public final static String AUTH_TOKEN_PARAMETER_NAME = "accessToken";
    // FIXME: use SSL connection
    public final static String SERVER_URL = "http://p30480.lab1.stud.tech-mail.ru/";

    private final String AUTH_TOKEN;
    private final Context context;

    ApiSection(@NonNull Context context) {
        this.context = context;
        SharedPreferences preferences = context.getSharedPreferences(PreferenceConstants.PREFERENCE_NAME, Context.MODE_PRIVATE);
        AUTH_TOKEN = preferences.getString(PreferenceConstants.AUTH_TOKEN_N, null);
    }

    /*static <T> List<Pair<String, Object>> reformList(List<Pair<String, T>> parameters) {
        if (parameters.get(0).second.getClass().equals(String.class)) {

            List<Pair<String, Object>> list = new ArrayList<>(parameters.size());
            for (Pair<String, T> p : parameters) {
                list.add(new Pair<String, Object>(p.first, p.second));
            }

            return list;
        } else {
            return (List) parameters;
        }
    }*/

    <T> String executeRequest(@NonNull String requestURL, @NonNull String requestMethod,
                          @Nullable List<Pair<String, T>> parameters, boolean addToken) throws IOException {

        if (parameters == null)
            parameters = new ArrayList<>(1);

        List<Pair<String, Object>> right = (List) parameters;
        if (addToken)
            right.add(new Pair<String, Object>(AUTH_TOKEN_PARAMETER_NAME, AUTH_TOKEN));

        ServerConnection serverConnection = new ServerConnection(context, getUrlAddition() + requestURL);
        serverConnection.setParameters(right);
        serverConnection.setRequestMethod(requestMethod);

        return serverConnection.getResponse();
    }

    <T> String executeRequest(@NonNull String requestURL, @NonNull String requestMethod,
                          @Nullable List<Pair<String, T>> parameters) throws IOException {
        return executeRequest(requestURL, requestMethod, parameters, true);
    }

    String executeRequest(@NonNull String requestURL, @NonNull String requestMethod) throws IOException {
        return executeRequest(requestURL, requestMethod, null);
    }

    String getAuthToken() {
        return AUTH_TOKEN;
    }

    Context getContext() {
        return context;
    }

    String getUrlAddition() {
        return SERVER_URL;
    }
}
