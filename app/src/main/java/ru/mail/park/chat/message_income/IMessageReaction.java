package ru.mail.park.chat.message_income;

import java.util.ArrayList;

import ru.mail.park.chat.models.Message;

/**
 * Created by 1запуск BeCompact on 27.03.2016.
 */
public interface IMessageReaction {
    void onIncomeMessage(String message);
    void onActionSendMesssage();
    void onActionDeleteMessage(int mid);
    void onGetHistoryMessages(ArrayList<Message>);
}
