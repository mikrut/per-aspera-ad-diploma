package ru.mail.park.chat.api;

import android.content.Context;

import ru.mail.park.chat.models.Contact;
import ru.mail.park.chat.models.OwnerProfile;

/**
 * Created by Михаил on 19.03.2016.
 */
public class AuthMock extends Auth {
    private String login;
    private String password;
    private String email;

    private boolean isLoggedIn = false;

    public AuthMock(Context context) {
        super(context);
    }

    @Override
    public OwnerProfile signUp(String login, String password, String email) {
        this.login = login;
        this.password = password;
        this.email = email;

        OwnerProfile user = new OwnerProfile();
        user.setLogin(login);
        user.setEmail(email);
        return user;
    }

    @Override
    public OwnerProfile signIn(String login, String password) {
        this.login = login;
        this.password = password;

        OwnerProfile user = new OwnerProfile();
        user.setLogin(login);
        user.setEmail(email);
        isLoggedIn = true;

        return user;
    }

    @Override
    public void logOut() {
        // logout via ServerConnection
        // check if logged out

        this.login = "";
        this.password = "";
        isLoggedIn = false;
    }

/*    public void showActiveSessions() {

    }

    public void closeSession() {

    }*/

    @Override
    public boolean isLogged() {
        return isLoggedIn;
    }
}
