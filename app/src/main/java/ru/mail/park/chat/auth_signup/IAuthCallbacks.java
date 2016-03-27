package ru.mail.park.chat.auth_signup;

import ru.mail.park.chat.models.OwnerProfile;

/**
 * Created by Михаил on 23.02.2016.
 */
public interface IAuthCallbacks {
    void onStartAuth();
    void onLoginSuccess(OwnerProfile contact);
    void onLoginFail(String message);
}
