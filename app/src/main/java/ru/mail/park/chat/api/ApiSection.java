package ru.mail.park.chat.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ru.mail.park.chat.api.network.ServerConnection;
import ru.mail.park.chat.database.PreferenceConstants;

/**
 * Created by Михаил on 19.03.2016.
 */
public class ApiSection {
    public final static String AUTH_TOKEN_PARAMETER_NAME = "accessToken";
    // FIXME: use SSL connection
    public final static Uri SERVER_URL = new Uri.Builder()
            .scheme("http").authority("p30480.lab1.stud.tech-mail.ru").build();

    private final String AUTH_TOKEN;
    private final Context context;

    protected ApiSection(@NonNull Context context) {
        this.context = context;
        SharedPreferences preferences = context.getSharedPreferences(PreferenceConstants.PREFERENCE_NAME, Context.MODE_PRIVATE);
        AUTH_TOKEN = preferences.getString(PreferenceConstants.AUTH_TOKEN_N, null);
    }

    protected <T> String executeRequest(@NonNull String requestURL, @NonNull String requestMethod,
                          @Nullable List<Pair<String, T>> parameters, boolean addToken) throws IOException {

        if (parameters == null)
            parameters = new ArrayList<>(1);

        List<Pair<String, Object>> right = (List) parameters;
        if (addToken)
            right.add(new Pair<String, Object>(AUTH_TOKEN_PARAMETER_NAME, AUTH_TOKEN));

        ServerConnection serverConnection = new ServerConnection(context, getUrlAddition().buildUpon().appendPath(requestURL).build().toString());
        serverConnection.setParameters(right);
        serverConnection.setRequestMethod(requestMethod);

        return serverConnection.getResponse();
    }

    protected <T> String executeRequest(@NonNull String requestURL, @NonNull String requestMethod,
                          @Nullable List<Pair<String, T>> parameters) throws IOException {
        return executeRequest(requestURL, requestMethod, parameters, true);
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

    protected Uri getUrlAddition() {
        return SERVER_URL.buildUpon().appendPath("api").build();
    }
}
