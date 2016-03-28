package ru.mail.park.chat.activities.auth_logout;

/**
 * Created by 1запуск BeCompact on 27.03.2016.
 */
public interface IAuthLogout {
    void onStartLogout();
    void onLogoutSuccess();
    void onLogoutFail();
}
