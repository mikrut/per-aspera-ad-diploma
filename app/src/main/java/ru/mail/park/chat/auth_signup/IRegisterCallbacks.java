package ru.mail.park.chat.auth_signup;

import java.util.Map;

import ru.mail.park.chat.models.OwnerProfile;

/**
 * Created by Михаил on 27.03.2016.
 */
public interface IRegisterCallbacks {
    enum ErrorType {
        LOGIN,
        FIRST_NAME,
        LAST_NAME,
        EMAIL,
        PASSWORD,
        IMG;

        @Override
        public String toString() {
            switch (this) {
                case LOGIN:
                    return "login";
                case FIRST_NAME:
                    return "firstName";
                case LAST_NAME:
                    return "lastName";
                case EMAIL:
                    return "email";
                case PASSWORD:
                    return "password";
                case IMG:
                    return "img";
                default:
                    return super.toString();
            }
        }
    }

    void onRegistrationStart();
    void onRegistrationSuccess(OwnerProfile contact);
    void onRegistrationFail(Map<ErrorType, String> errors);
    void onRegistrationFinish();
}
