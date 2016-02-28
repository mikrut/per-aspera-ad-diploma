package api;

/**
 * Created by 1запуск BeCompact on 29.02.2016.
 */
public class ApiFacade {

    private Auth auth;
    private Chats chats;
    private Contacts contacts;
    private Messages messages;
    private P2P p2p;
    private Users users;

    private ServerConnection sConn;

    public ApiFacade() {
        sConn = new ServerConnection();

        auth = new Auth(sConn);
        chats = new Chats(sConn);
        contacts = new Contacts(sConn);
        messages = new Messages(sConn);
        p2p = new P2P(sConn);
        users = new Users(sConn);
    }
}
