package ru.mail.park.chat.activities;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.rockerhieu.emojicon.EmojiconEditText;
import com.rockerhieu.emojicon.EmojiconGridFragment;
import com.rockerhieu.emojicon.EmojiconsFragment;
import com.rockerhieu.emojicon.emoji.Emojicon;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.mail.park.chat.R;
import ru.mail.park.chat.activities.adapters.MessagesAdapter;
import ru.mail.park.chat.activities.views.KeyboardDetectingLinearLayout;
import ru.mail.park.chat.api.Messages;
import ru.mail.park.chat.database.MessagesHelper;
import ru.mail.park.chat.database.PreferenceConstants;
import ru.mail.park.chat.loaders.MessagesLoader;
import ru.mail.park.chat.message_income.IMessageReaction;
import ru.mail.park.chat.models.Message;

// TODO: emoticons
// TODO: send message
public class DialogActivity
        extends AppCompatActivity
        implements IMessageReaction,
        EmojiconGridFragment.OnEmojiconClickedListener,
        EmojiconsFragment.OnEmojiconBackspaceClickedListener {
    public static final String CHAT_ID = DialogActivity.class.getCanonicalName() + ".CHAT_ID";
    public static final String USER_ID = DialogActivity.class.getCanonicalName() + ".USER_ID";

    private KeyboardDetectingLinearLayout globalLayout;
    private FrameLayout emojicons;
    private RecyclerView messagesList;
    private ImageButton insertEmoticon;
    private EmojiconEditText inputMessage;
    private ImageButton sendMessage;

    private String chatID;
    private String userID;

    private List<Message> receivedMessageList;
    private MessagesAdapter messagesAdapter;
    private Messages messages;

    private boolean isEmojiFragmentShown = false;
    private boolean isSoftKeyboardShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);

        globalLayout = (KeyboardDetectingLinearLayout) findViewById(R.id.main);
        messagesList = (RecyclerView) findViewById(R.id.messagesList);
        insertEmoticon = (ImageButton) findViewById(R.id.insertEmoticon);
        inputMessage = (EmojiconEditText) findViewById(R.id.inputMessage);
        sendMessage = (ImageButton) findViewById(R.id.sendMessage);
        emojicons = (FrameLayout) findViewById(R.id.emojicons);

        globalLayout.setOnKeyboardEventListener(new KeyboardDetectingLinearLayout.OnKeyboardEventListener() {
            @Override
            public void onSoftKeyboardShown() {
                isSoftKeyboardShown = true;
                isEmojiFragmentShown = false;
                emojicons.setVisibility(isEmojiFragmentShown ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onSoftKeyboardHidden() {
                isSoftKeyboardShown = false;
                emojicons.setVisibility(isEmojiFragmentShown ? View.VISIBLE : View.GONE);
            }
        });

        try {
            messages = new Messages(this, this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        chatID = getIntent().getStringExtra(CHAT_ID);
        userID = getIntent().getStringExtra(USER_ID);
        if (chatID != null) {
            MessagesHelper messagesHelper = new MessagesHelper(this);
            receivedMessageList = messagesHelper.getMessages(chatID);
            Collections.sort(receivedMessageList);

            SharedPreferences sharedPreferences =
                    getSharedPreferences(PreferenceConstants.PREFERENCE_NAME, MODE_PRIVATE);
            String ownerID = sharedPreferences.getString(PreferenceConstants.USER_UID_N, null);

            messagesAdapter = new MessagesAdapter(receivedMessageList, ownerID);
            messagesList.setAdapter(messagesAdapter);
            LinearLayoutManager llm = new LinearLayoutManager(this);
            llm.setStackFromEnd(true);
            messagesList.setLayoutManager(llm);
        }

        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(inputMessage.getText().toString());
            }
        });

        insertEmoticon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isSoftKeyboardShown) {
                    isEmojiFragmentShown = !isEmojiFragmentShown;
                    emojicons.setVisibility(isEmojiFragmentShown ? View.VISIBLE : View.GONE);
                } else {
                    View view = getCurrentFocus();
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                    isEmojiFragmentShown = true;
                }
            }
        });

        setEmojiconFragment(false);
        if (chatID != null)
            onUpdateChatID();
    }

    private void onUpdateChatID() {
        Bundle args = new Bundle();
        args.putString(MessagesLoader.CID_ARG, chatID);
        getLoaderManager().initLoader(0, args, listener);
    }

    private void sendMessage(@NonNull String message) {
        if (chatID != null) {
            messages.sendMessage(chatID, message);
        } else if (userID != null) {
            messages.sendFirstMessage(userID, message);
        }
    }

    private void addMessage(@NonNull Message message) {
        for (int position = 0; position < receivedMessageList.size(); position++) {
            if (message.compareTo(receivedMessageList.get(position)) < 0) {
                receivedMessageList.add(position, message);
                messagesAdapter.notifyItemInserted(position);
                return;
            }
        }

        receivedMessageList.add(receivedMessageList.size(), message);
        messagesAdapter.notifyItemInserted(receivedMessageList.size());
    }

    private void removeMessage(@NonNull String messageID) {
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
    public void onIncomeMessage(JSONObject message){
        try {
            if (message.has("idRoom")) {
                chatID = message.getString("idRoom");
                onUpdateChatID();
            }

            Message incomeMsg = new Message(message, this);
            addMessage(incomeMsg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActionDeleteMessage(int mid) {
        removeMessage(String.valueOf(mid));
    }

    @Override
    public void onActionSendMessage(JSONObject msg){
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

    private void setEmojiconFragment(boolean useSystemDefault) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.emojicons, EmojiconsFragment.newInstance(useSystemDefault))
                .commit();
    }

    @Override
    public void onEmojiconBackspaceClicked(View v) {
        EmojiconsFragment.backspace(inputMessage);
    }

    @Override
    public void onEmojiconClicked(Emojicon emojicon) {
        EmojiconsFragment.input(inputMessage, emojicon);
    }

    @Override
    protected void onPause() {
        super.onPause();
        messages.disconnect();
    }

    private final LoaderManager.LoaderCallbacks<List<Message>> listener = new LoaderManager.LoaderCallbacks<List<Message>>() {
        @Override
        public Loader<List<Message>> onCreateLoader(int id, Bundle args) {
            return new MessagesLoader(DialogActivity.this, args);
        }

        @Override
        public void onLoadFinished(Loader<List<Message>> loader, List<Message> data) {
            if (data != null) {
                for (Message message : data) {
                    addMessage(message);
                }
            }
        }

        @Override
        public void onLoaderReset(Loader<List<Message>> loader) {
            // TODO: something
        }
    };
}
