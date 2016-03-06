package api;

/**
 * Created by 1запуск BeCompact on 29.02.2016.
 */
public class Auth {
    private ServerConnection sConn;

    private String login;
    private String password;
    private String email;

    private boolean isLoggedIn = false;

    public Auth(ServerConnection sConn) {
        this.sConn = sConn;
    }

    public void signUp(String login, String password, String email) {
        this.login = login;
        this.password = password;
        this.email = email;
    }

    public void signIn(String login, String password) {
        this.login = login;
        this.password = password;

        // log in via ServerConnection
        // check if logged in

        isLoggedIn = true;
    }

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

    public boolean isLogged() {
        return isLoggedIn();
    }
}
