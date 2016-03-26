package ru.mail.park.chat.auth_signup;

import ru.mail.park.chat.models.OwnerProfile;

/**
 * Created by Михаил on 27.03.2016.
 */
public interface IRegisterCallbacks {
    void onRegistrationStart();
    void onRegistrationSuccess(OwnerProfile contact);
    void onRegistrationFail(String message);
    void onRegistrationFinish();
}
