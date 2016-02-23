package ru.mail.park.chat;

/**
 * Created by Михаил on 23.02.2016.
 */
public interface IAuthCallbacks {
    void onStartAuth();
    void onFinishAuth(boolean isSuccess, String message);
}
