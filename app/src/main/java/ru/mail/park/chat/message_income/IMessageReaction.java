package ru.mail.park.chat.message_income;

import org.json.JSONObject;

import java.util.ArrayList;

import ru.mail.park.chat.models.Chat;
import ru.mail.park.chat.models.Message;

/**
 * Created by 1запуск BeCompact on 27.03.2016.
 */
public interface IMessageReaction {
    void onIncomeMessage(JSONObject message);
    void onActionSendMessage(JSONObject message);
    void onActionDeleteMessage(int mid);
    void onGetHistoryMessages(ArrayList<Message> msg_list);
    void onChatCreated(Chat chat);
}
