package ru.mail.park.chat.message_interfaces;

import org.json.JSONObject;

import java.util.ArrayList;

import ru.mail.park.chat.models.Chat;
import ru.mail.park.chat.models.Contact;
import ru.mail.park.chat.models.Message;

/**
 * Created by 1запуск BeCompact on 27.03.2016.
 */
public interface IChatListener {
    void onIncomeMessage(Message message);
    void onAcknowledgeSendMessage(Message message);
    void onActionDeleteMessage(int mid);
    void onWrite(String cid, Contact user);
}
