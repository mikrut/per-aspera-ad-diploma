package ru.mail.park.chat.activities;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.rockerhieu.emojicon.EmojiconEditText;
import com.rockerhieu.emojicon.EmojiconGridFragment;
import com.rockerhieu.emojicon.EmojiconsFragment;
import com.rockerhieu.emojicon.emoji.Emojicon;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import ru.mail.park.chat.R;
import ru.mail.park.chat.activities.adapters.MessagesAdapter;
import ru.mail.park.chat.activities.views.KeyboardDetectingLinearLayout;
import ru.mail.park.chat.api.HttpFileUpload;
import ru.mail.park.chat.api.Messages;
import ru.mail.park.chat.database.PreferenceConstants;
import ru.mail.park.chat.file_dialog.FileDialog;
import ru.mail.park.chat.loaders.MessagesDBLoader;
import ru.mail.park.chat.loaders.MessagesLoader;
import ru.mail.park.chat.message_income.IMessageReaction;
import ru.mail.park.chat.models.Chat;
import ru.mail.park.chat.models.Message;

// TODO: emoticons
// TODO: send message
public class DialogActivity
        extends AppCompatActivity
        implements IMessageReaction,
        EmojiconGridFragment.OnEmojiconClickedListener,
        EmojiconsFragment.OnEmojiconBackspaceClickedListener,
        HttpFileUpload.IUploadListener {
    public static final String CHAT_ID = DialogActivity.class.getCanonicalName() + ".CHAT_ID";
    private static final int CODE_FILE_SELECTED = 3;
    private static final String FILE_UPLOAD_URL = "http://p30480.lab1.stud.tech-mail.ru/files/upload";
    public static final String USER_ID = DialogActivity.class.getCanonicalName() + ".USER_ID";

    private KeyboardDetectingLinearLayout globalLayout;
    private FrameLayout emojicons;
    private RecyclerView messagesList;
    private ImageButton insertEmoticon, attachFile;
    private EmojiconEditText inputMessage;
    private ImageButton sendMessage;

    private String chatID;
    private String userID;
    private String accessToken;

    private List<Message> receivedMessageList;
    private MessagesAdapter messagesAdapter;
    private LinearLayoutManager layoutManager;
    private Messages messages;

    private ImageButton buttonDown;

    private boolean isEmojiFragmentShown = false;
    private boolean isSoftKeyboardShown = false;

    public static final int MESSAGES_DB_LOADER = 0;
    public static final int MESSAGES_WEB_LOADER = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);

        globalLayout = (KeyboardDetectingLinearLayout) findViewById(R.id.main);
        messagesList = (RecyclerView) findViewById(R.id.messagesList);
        insertEmoticon = (ImageButton) findViewById(R.id.insertEmoticon);
        attachFile = (ImageButton) findViewById(R.id.attachFile);
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
            SharedPreferences sharedPreferences =
                    getSharedPreferences(PreferenceConstants.PREFERENCE_NAME, MODE_PRIVATE);
            String ownerID = sharedPreferences.getString(PreferenceConstants.USER_UID_N, null);
            accessToken = sharedPreferences.getString(PreferenceConstants.AUTH_TOKEN_N, null);

            receivedMessageList = new ArrayList<>();
            messagesAdapter = new MessagesAdapter(receivedMessageList, ownerID);
            messagesList.setAdapter(messagesAdapter);
            layoutManager = new LinearLayoutManager(this);
            layoutManager.setStackFromEnd(true);
            messagesList.setLayoutManager(layoutManager);

            messagesList.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                }

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    boolean atBottom =
                            (layoutManager.findLastVisibleItemPosition() == receivedMessageList.size() - 1);
                    if (atBottom) {
                        buttonDown.setVisibility(View.GONE);
                    }
                }
            });
        }

        buttonDown = (ImageButton) findViewById(R.id.buttonDown);
        buttonDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messagesList.scrollToPosition(receivedMessageList.size() - 1);
            }
        });

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

        attachFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), FileDialog.class);
                intent.putExtra(FileDialog.START_PATH, "/sdcard");
                intent.putExtra(FileDialog.CAN_SELECT_DIR, false);

                startActivityForResult(intent, CODE_FILE_SELECTED);
            }
        });

        setEmojiconFragment(false);

        if (chatID != null) {
            Log.d("[TP-diploma]", "calling onUpdateChatID");
            onUpdateChatID();
        }
    }

    private void onUpdateChatID() {
        Bundle args = new Bundle();
        args.putString(MessagesLoader.CID_ARG, chatID);
        getLoaderManager().initLoader(MESSAGES_DB_LOADER, args, listener);
    }

    private void sendMessage(@NonNull String message) {
        if (chatID != null) {
            messages.sendMessage(chatID, message);
        } else if (userID != null) {
            messages.sendFirstMessage(userID, message);
        }
    }

    private void addMessage(@NonNull Message message) {
        boolean atBottom =
                (layoutManager.findLastVisibleItemPosition() == receivedMessageList.size() - 1);

        boolean inserted = false;
        for (int position = 0; position < receivedMessageList.size() && !inserted; position++) {
            int comp = message.compareTo(receivedMessageList.get(position));
            if (comp < 0) {
                receivedMessageList.add(position, message);
                messagesAdapter.notifyItemInserted(position);
                inserted = true;
            } else if (comp == 0) {
                receivedMessageList.set(position, message);
                messagesAdapter.notifyItemChanged(position);
                inserted = true;
            }
        }

        if (!inserted) {
            receivedMessageList.add(message);
            messagesAdapter.notifyItemInserted(receivedMessageList.size() - 1);
        }

        if (atBottom) {
            messagesList.scrollToPosition(receivedMessageList.size() - 1);
        } else {
            buttonDown.setVisibility(View.VISIBLE);
        }
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
                String newChatID = message.getString("idRoom");
                if (chatID == null || !chatID.equals(newChatID)) {
                    chatID = newChatID;
                    onUpdateChatID();
                }
            }

            Message incomeMsg = new Message(message, this, chatID);
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

        messagesAdapter.notifyDataSetChanged();
    }

    @Override
    public void onChatCreated(Chat chat) {
        // TODO: ???
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
    public void onUploadComplete(String name) {
        TextView linkable = ((TextView) findViewById(R.id.link));
        linkable.setMovementMethod(LinkMovementMethod.getInstance());
        linkable.setText(Html.fromHtml("<a href=\"http://p30480.lab1.stud.tech-mail.ru/"+name+"\">"+name+"</a>"));
        linkable.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        messages.disconnect();
    }

    public synchronized void onActivityResult(final int requestCode, int resultCode, final Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            String filePath = data.getStringExtra(FileDialog.RESULT_PATH);
            FileInputStream fstrm = null;
            HttpFileUpload hfu = null;

            if (requestCode == CODE_FILE_SELECTED) {
                Log.d("[TP-diploma]", "sending file started");
                try {
                    fstrm = new FileInputStream(filePath);
                    hfu = new HttpFileUpload(FILE_UPLOAD_URL, filePath.substring(filePath.lastIndexOf('/'), filePath.length()), accessToken);
                    hfu.Send_Now(fstrm, this);
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private final LoaderManager.LoaderCallbacks<List<Message>> listener = new LoaderManager.LoaderCallbacks<List<Message>>() {
        @Override
        public Loader<List<Message>> onCreateLoader(int id, Bundle args) {
            Log.d("[TP-diploma]", "creating MessagesListener");
            switch (id) {
                case MESSAGES_WEB_LOADER:
                    return new MessagesLoader(DialogActivity.this, args);
                case MESSAGES_DB_LOADER:
                default:
                        return new MessagesDBLoader(DialogActivity.this, args);
            }
        }

        @Override
        public void onLoadFinished(Loader<List<Message>> loader, List<Message> data) {
            if (data != null) {
                Log.d("[TP-diploma]", "messages count: " + data.size());
                for (Message message : data) {
                    addMessage(message);
                }
                messagesList.scrollToPosition(receivedMessageList.size() - 1);
            } else {
                Log.d("[TP-diploma]", "empty list");
            }

            if (loader.getId() == MESSAGES_DB_LOADER) {
                Bundle args = new Bundle();
                args.putString(MessagesLoader.CID_ARG, chatID);
                getLoaderManager().restartLoader(MESSAGES_WEB_LOADER, args, this);
            }
        }

        @Override
        public void onLoaderReset(Loader<List<Message>> loader) {
            // TODO: something
        }
    };
}
