package ru.mail.park.chat.api.network;

import com.android.volley.Request;
import com.android.volley.Response;

import java.io.File;

/**
 * Created by Михаил on 16.06.2016.
 */

// TODO: think about using Volley for network requests
public abstract class MultipartRequest <T> extends Request<T> {
    public MultipartRequest(String url, Response.ErrorListener listener) {
        super(Method.POST, url, listener);
    }
}
