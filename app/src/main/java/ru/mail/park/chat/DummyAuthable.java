package ru.mail.park.chat;

/**
 * Created by Михаил on 23.02.2016.
 */
public class DummyAuthable implements IAuthable {
    private final String dummyLogin = "user";
    private final String dummyPassword = "1234";

    @Override
    public void auth(String login, String password, IAuthCallbacks listener) {
        listener.onStartAuth();

        if (login.equals(dummyLogin) && password.equals(dummyPassword)) {
            listener.onFinishAuth(true, "Success");
        } else {
            listener.onFinishAuth(false, "Wrong credentials specified");
        }
    }
}
