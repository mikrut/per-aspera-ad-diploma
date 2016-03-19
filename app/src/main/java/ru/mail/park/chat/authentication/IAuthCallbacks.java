package ru.mail.park.chat.authentication;

import ru.mail.park.chat.models.Contact;

/**
 * Created by Михаил on 23.02.2016.
 */
public interface IAuthCallbacks {
    void onStartAuth();
    void onLoginSuccess(Contact contact);
    void onLoginFail(String message);
}
