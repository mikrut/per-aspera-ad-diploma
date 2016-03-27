package ru.mail.park.chat.message_income;

import org.json.JSONException;

import java.text.ParseException;
import java.util.ArrayList;

import ru.mail.park.chat.models.Message;

/**
 * Created by 1запуск BeCompact on 27.03.2016.
 */
public interface IMessageReaction {
    void onIncomeMessage(String message);
    void onActionSendMessage(String msg);
    void onActionDeleteMessage(int mid);
    void onGetHistoryMessages(ArrayList<Message> msg_list);
}
