package ru.mail.park.chat.activities.interfaces;

import android.support.v7.app.ActionBar;

import ru.mail.park.chat.api.ChatInfo;

/**
 * Created by 1запуск BeCompact on 16.05.2016.
 */
public interface IActionBarListener {
    void onLoadInfoCompleted(ActionBar mActionBar, ChatInfo chatInfo);
}
