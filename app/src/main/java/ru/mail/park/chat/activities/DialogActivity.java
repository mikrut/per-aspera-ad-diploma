package ru.mail.park.chat.activities;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.EditText;
import android.widget.ImageButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.mail.park.chat.R;
import ru.mail.park.chat.activities.adapters.MessagesAdapter;
import ru.mail.park.chat.database.MessagesHelper;
import ru.mail.park.chat.database.PreferenceConstants;
import ru.mail.park.chat.message_income.IMessageReaction;
import ru.mail.park.chat.models.Message;

// TODO: emoticons
// TODO: send message
public class DialogActivity extends AppCompatActivity implements IMessageReaction {
    public static final String CHAT_ID = DialogActivity.class.getCanonicalName() + ".CHAT_ID";

    private RecyclerView messagesList;
    private ImageButton insertEmoticon;
    private EditText inputMessage;
    private ImageButton sendMessage;

    private String chatID;
    private List<Message> receivedMessageList;
    private MessagesAdapter messagesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);

        messagesList = (RecyclerView) findViewById(R.id.messagesList);
        insertEmoticon = (ImageButton) findViewById(R.id.insertEmoticon);
        inputMessage = (EditText) findViewById(R.id.inputMessage);
        sendMessage = (ImageButton) findViewById(R.id.sendMessage);

        chatID = getIntent().getStringExtra(CHAT_ID);
        if (chatID != null) {
            MessagesHelper messagesHelper = new MessagesHelper(this);
            receivedMessageList = messagesHelper.getMessages(chatID);
            Collections.sort(receivedMessageList);

            SharedPreferences sharedPreferences =
                    getSharedPreferences(PreferenceConstants.PREFERENCE_NAME, MODE_PRIVATE);
            String ownerID = sharedPreferences.getString(PreferenceConstants.USER_UID_N, null);

            messagesAdapter = new MessagesAdapter(receivedMessageList, ownerID);
            messagesList.setAdapter(messagesAdapter);
            messagesList.setLayoutManager(new LinearLayoutManager(this));
        }
    }

    public void addMessage(@NonNull Message message) {
        for (int position = 0; position < receivedMessageList.size(); position++) {
            if (message.compareTo(receivedMessageList.get(position)) < 0) {
                receivedMessageList.add(position, message);
                messagesAdapter.notifyItemInserted(position);
                return;
            }
        }
    }

    public void removeMessage(@NonNull String messageID) {
        for (int position = 0; position < receivedMessageList.size(); position++) {
            Message message = receivedMessageList.get(position);
            if (message.getMid() != null && message.getMid().equals(messageID)) {
                receivedMessageList.remove(position);
                messagesAdapter.notifyItemRemoved(position);
                return;
            }
        }
    }

    @Override
    public void onIncomeMessage(String message){
        try {
            JSONObject msgJson = new JSONObject(message);
            Message incomeMsg = new Message(msgJson);
            addMessage(incomeMsg);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActionDeleteMessage(int mid) {
        removeMessage(String.valueOf(mid));
    }

    @Override
    public void onActionSendMessage(String msg){
        try {
            onIncomeMessage(msg);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onGetHistoryMessages(ArrayList<Message> msg_list) {
        receivedMessageList.clear();
        receivedMessageList.addAll(msg_list);
        Collections.sort(receivedMessageList);

        MessagesHelper messagesHelper = new MessagesHelper(this);
        messagesHelper.deleteMessages(msg_list.get(0).getCid());
        messagesAdapter.notifyDataSetChanged();
    }
}
