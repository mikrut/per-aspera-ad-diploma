package ru.mail.park.chat.auth_signup;

/**
 * Created by Михаил on 23.02.2016.
 */
public interface IAuthable {
    void auth(String login, String password, IAuthCallbacks listener);
}
