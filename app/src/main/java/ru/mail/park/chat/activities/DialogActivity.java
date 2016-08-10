package ru.mail.park.chat.activities;

import android.Manifest;
import android.app.Activity;
import android.app.LoaderManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.neovisionaries.ws.client.WebSocketState;
import com.rockerhieu.emojicon.EmojiconEditText;
import com.rockerhieu.emojicon.EmojiconGridFragment;
import com.rockerhieu.emojicon.EmojiconsFragment;
import com.rockerhieu.emojicon.emoji.Emojicon;

import org.apache.commons.lang3.ObjectUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import ru.mail.park.chat.R;
import ru.mail.park.chat.activities.adapters.FilesAdapter;
import ru.mail.park.chat.activities.adapters.MessagesAdapter;
import ru.mail.park.chat.activities.views.DialogActionBar;
import ru.mail.park.chat.activities.views.KeyboardDetectingLinearLayout;
import ru.mail.park.chat.api.HttpFileUpload;
import ru.mail.park.chat.api.websocket.Messages;
import ru.mail.park.chat.api.websocket.IWSStatusListener;
import ru.mail.park.chat.api.websocket.NotificationService;
import ru.mail.park.chat.database.ChatsHelper;
import ru.mail.park.chat.file_dialog.FileDialog;
import ru.mail.park.chat.helpers.DialogEndlessPagination;
import ru.mail.park.chat.helpers.ScrollEndlessPagination;
import ru.mail.park.chat.loaders.ChatInfoLoader;
import ru.mail.park.chat.loaders.ChatInfoWebLoader;
import ru.mail.park.chat.loaders.MessagesDBLoader;
import ru.mail.park.chat.loaders.MessagesLoader;
import ru.mail.park.chat.loaders.images.ImageDownloadManager;
import ru.mail.park.chat.api.websocket.IChatListener;
import ru.mail.park.chat.api.websocket.IMessageSender;
import ru.mail.park.chat.models.AttachedFile;
import ru.mail.park.chat.models.Chat;
import ru.mail.park.chat.models.Contact;
import ru.mail.park.chat.models.Message;
import ru.mail.park.chat.models.OwnerProfile;

// FIXME: god-class???
public class DialogActivity
        extends AImageDownloadServiceBindingActivity
        implements IChatListener,
        IWSStatusListener,
        EmojiconGridFragment.OnEmojiconClickedListener,
        EmojiconsFragment.OnEmojiconBackspaceClickedListener,
        HttpFileUpload.IUploadListener {
    public static final String CHAT_ID = DialogActivity.class.getCanonicalName() + ".CHAT_ID";
    private static final int REQUEST_WRITE_STORAGE = 112;
    private static final int CODE_FILE_SELECTED = 3;
    public static final String SERVER_URL = "http://p30480.lab1.stud.tech-mail.ru/";
    private static final String FILE_UPLOAD_URL = "http://p30480.lab1.stud.tech-mail.ru/file/upload";
    public static final String USER_ID = DialogActivity.class.getCanonicalName() + ".USER_ID";

    protected KeyboardDetectingLinearLayout globalLayout;
    protected FrameLayout emojicons;
    protected RecyclerView messagesList;
    protected ImageButton insertEmoticon, attachFile;
    protected EmojiconEditText inputMessage;
    protected ImageButton sendMessage;
    protected RecyclerView attachments;
    protected ImageButton buttonDown;

    protected ActionBar mActionBar;
    protected DialogActionBar dialogActionBar;

    private String chatID;
    private String userID;
    private String ownerID;
    private String accessToken;
    private Chat thisChat;

    private Timer schedulerTimer;
    private final Handler uiHandler = new Handler();
    private static final long WRITER_DISAPPEAR_DELAY_MILLIS = 2000;
    private static final long RETRY_DELAY_MILLIS = 5000;

    private List<Pair<Long, Contact>> writers;
    private static final int MAX_UNDELIVERED_MESSAGES = 50;
    private BlockingQueue<Message> undeliveredMessages = new ArrayBlockingQueue<Message>(MAX_UNDELIVERED_MESSAGES);

    private List<Message> receivedMessageList;
    private List<AttachedFile> attachemtsList;
    private MessagesAdapter messagesAdapter;
    private LinearLayoutManager layoutManager;
    private ScrollEndlessPagination<Message> pagination;

    private boolean receivedFromWeb = false;

    private boolean isEmojiFragmentShown = false;
    private boolean isSoftKeyboardShown = false;

    public static final int MESSAGES_DB_LOADER = 0;
    public static final int MESSAGES_WEB_LOADER = 1;
    public static final int CHAT_DB_LOADER = 2;
    public static final int CHAT_WEB_LOADER = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);

        initViews();
        initAttachments();

        chatID = getIntent().getStringExtra(CHAT_ID);
        userID = getIntent().getStringExtra(USER_ID);

        OwnerProfile ownerProfile = new OwnerProfile(this);
        ownerID = ownerProfile.getUid();
        accessToken = ownerProfile.getAuthToken();
        thisChat = null;

        initMessagesList();
        initActionListeners();

        setEmojiconFragment(false);

        if (chatID != null) {
            Log.d("[TP-diploma]", "calling onUpdateChatID");
            onUpdateChatID();
        }

        boolean hasPermission = (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        boolean hasWPermission = (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if (!hasPermission || !hasWPermission) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_WRITE_STORAGE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("[TP-diploma]", "DialogActivity.onResume");
        initWriters();
        initRetryTimeout();

        if (chatID != null) {
            ChatsHelper ch = new ChatsHelper(this);
            thisChat = (thisChat == null) ? ch.getChat(chatID) : thisChat;
            ch.close();
        }

        if(thisChat != null && thisChat.getImagePath() != null && getImageDownloadManager() != null) {
            dialogActionBar.setChatData(thisChat, getImageDownloadManager());
        }
    }

    private void initViews() {
        globalLayout = (KeyboardDetectingLinearLayout) findViewById(R.id.main);
        messagesList = (RecyclerView) findViewById(R.id.messagesList);
        insertEmoticon = (ImageButton) findViewById(R.id.insertEmoticon);
        attachFile = (ImageButton) findViewById(R.id.attachFile);
        inputMessage = (EmojiconEditText) findViewById(R.id.inputMessage);
        sendMessage = (ImageButton) findViewById(R.id.sendMessage);
        emojicons = (FrameLayout) findViewById(R.id.emojicons);
        attachments = (RecyclerView) findViewById(R.id.attachments_recycler_view);
        buttonDown = (ImageButton) findViewById(R.id.buttonDown);

        RecyclerView.ItemAnimator animator = messagesList.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }

        globalLayout.setOnKeyboardEventListener(new KeyboardDetectingLinearLayout.OnKeyboardEventListener() {
            @Override
            public void onSoftKeyboardShown() {
                isSoftKeyboardShown = true;
                isEmojiFragmentShown = false;
                emojicons.setVisibility(View.GONE);
            }

            @Override
            public void onSoftKeyboardHidden() {
                isSoftKeyboardShown = false;
                emojicons.setVisibility(isEmojiFragmentShown ? View.VISIBLE : View.GONE);
            }
        });

        mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayShowHomeEnabled(false);
            mActionBar.setDisplayShowTitleEnabled(false);
            dialogActionBar = new DialogActionBar(this);
            mActionBar.setCustomView(dialogActionBar);
            mActionBar.setDisplayShowCustomEnabled(true);
        }
    }

    private void initAttachments() {
        attachemtsList = new ArrayList<>();
        FilesAdapter filesAdapter = new FilesAdapter(attachemtsList);
        attachments.setAdapter(filesAdapter);
        LinearLayoutManager attachmentsManager = new LinearLayoutManager(this);
        attachmentsManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        attachments.setLayoutManager(attachmentsManager);
        attachments.setVisibility(View.GONE);
    }

    // FIXME: possible multithreading bugs
    private void initWriters() {
        writers = new LinkedList<>();
        final TextView writersView = (TextView) findViewById(R.id.writersTextView);
        final ImageView pencilView = (ImageView) findViewById(R.id.pencil_icon);

        schedulerTimer = new Timer();
        schedulerTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                final long currentTimeMillis = System.currentTimeMillis();
                Iterator<Pair<Long, Contact>> i = writers.iterator();
                while (i.hasNext()) {
                    Pair<Long, Contact> current = i.next();
                    if (Math.abs(current.first - currentTimeMillis) > WRITER_DISAPPEAR_DELAY_MILLIS) {
                        i.remove();
                    }
                }

                String writersString = null;
                String writersConcatenated = "";
                if (writers.size() > 0) {
                    writersString = "%s %s typing...";
                    if (writers.size() <= 2) {
                        for (Pair<Long, Contact> writer : writers) {
                            writersConcatenated = writersConcatenated +
                                    writer.second.getFirstName().charAt(0) + ". " + writer.second.getLastName() + ", ";
                        }

                        writersConcatenated =
                                writersConcatenated.substring(0, writersConcatenated.length() - 2);
                    } else {
                        writersConcatenated = String.valueOf(writers.size()) + " users";
                    }

                    writersString = String.format(writersString,
                            writersConcatenated,
                            writers.size() > 1 ? "are" : "is");
                }
                final String resultString = writersString;
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (writersView != null) {
                            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) messagesList.getLayoutParams();
                            int bottomMargin = 0;
                            if (resultString != null) {
                                Animation anim = AnimationUtils.loadAnimation(DialogActivity.this, R.anim.pencil_writing);
                                writersView.setText(resultString);
                                writersView.setVisibility(View.VISIBLE);
                                pencilView.setVisibility(View.VISIBLE);
                                pencilView.startAnimation(anim);
                                bottomMargin = writersView.getHeight() > pencilView.getHeight() ? writersView.getHeight() : pencilView.getHeight();
                                bottomMargin += 25;
                            } else {
                                writersView.setVisibility(View.GONE);
                                pencilView.setVisibility(View.GONE);
                                pencilView.clearAnimation();
                                bottomMargin = 0;
                            }
                            layoutParams.setMargins(layoutParams.leftMargin,
                                    layoutParams.topMargin, layoutParams.rightMargin, bottomMargin);

                            messagesList.setLayoutParams(layoutParams);
                        }
                    }
                });
            }
        }, WRITER_DISAPPEAR_DELAY_MILLIS, WRITER_DISAPPEAR_DELAY_MILLIS);
    }

    private void initRetryTimeout() {
        schedulerTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        IMessageSender sender = getMessageSender();
                        if (chatID != null && (sender == null || !sender.isConnected())) {
                            Bundle args = new Bundle();
                            args.putString(MessagesLoader.CID_ARG, chatID);
                            getLoaderManager().restartLoader(MESSAGES_WEB_LOADER, args, messagesLoaderListener).forceLoad();
                        }

                        for (Message message : undeliveredMessages) {
                            sendMessage(message);
                        }
                    }
                });
            }
        }, RETRY_DELAY_MILLIS, RETRY_DELAY_MILLIS);
    }

    private void initMessagesList() {
        receivedMessageList = new ArrayList<>();
        messagesAdapter = new MessagesAdapter(receivedMessageList, ownerID);
        messagesList.setAdapter(messagesAdapter);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        messagesList.setLayoutManager(layoutManager);

        pagination = new DialogEndlessPagination(layoutManager, messagesLoaderListener, MESSAGES_WEB_LOADER, getLoaderManager(), receivedMessageList);
        messagesList.addOnScrollListener(pagination);
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

    private void initActionListeners() {
        dialogActionBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (thisChat != null && thisChat.getType() == Chat.GROUP_TYPE) {
                    Intent intent = new Intent(DialogActivity.this, GroupDialogEditActivity.class);
                    intent.putExtra(GroupDialogEditActivity.ARG_CID, chatID);
                    startActivity(intent);
                }
            }
        });

        inputMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                IMessageSender sender = getMessageSender();
                if (chatID != null && sender != null)
                    sender.write(chatID);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        buttonDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messagesList.scrollToPosition(receivedMessageList.size() - 1);
            }
        });

        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
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
                intent.putExtra(FileDialog.START_PATH, Environment.getExternalStorageDirectory());
                intent.putExtra(FileDialog.CAN_SELECT_DIR, false);

                startActivityForResult(intent, CODE_FILE_SELECTED);
            }
        });
    }

    @Nullable
    protected IMessageSender getMessageSender() {
        if (getNotificationService() != null) {
            return getNotificationService().getMessages();
        }
        return null;
    }

    private void onUpdateChatID() {
        Bundle args = new Bundle();
        args.putString(MessagesLoader.CID_ARG, chatID);
        args.putString(ChatInfoLoader.CID_ARG, chatID);
        args.putString(MessagesLoader.UID_ARG, userID);
        getLoaderManager().initLoader(MESSAGES_DB_LOADER, args, messagesLoaderListener);
        getLoaderManager().initLoader(CHAT_DB_LOADER, args, chatLoaderListener);
    }

    private void sendMessage() {
        final String messageBody = inputMessage.getText().toString();
        if (!messageBody.equals("") || attachemtsList.size() > 0) {
            Message message = new Message(messageBody, this);
            message.setUniqueID(System.currentTimeMillis());
            message.setFiles(attachemtsList);
            sendMessage(message);

            initAttachments();
            inputMessage.setText("");
        }
    }

    protected void sendMessage(@NonNull Message message) {
        undeliveredMessages.add(message);
        addMessage(message);
        IMessageSender sender = getMessageSender();
        if (sender != null) {
            if (chatID != null) {
                sender.sendMessage(chatID, message);
            } else if (userID != null) {
                sender.sendFirstMessage(userID, message);
            }
        }
    }

    private void addMessage(@NonNull Message message) {
        int itemCount = receivedMessageList.size();
        boolean atBottom =
                (layoutManager.findLastVisibleItemPosition() == receivedMessageList.size() - 1);

        boolean isDifferent = true;
        Integer insertedPosition = null;

        for (int position = 0; position < receivedMessageList.size() && insertedPosition == null; position++) {
            int comp = message.compareTo(receivedMessageList.get(position));
            if (comp < 0) {
                receivedMessageList.add(position, message);
                messagesAdapter.notifyItemInserted(insertedPosition = position);
            } else if (comp == 0) {
                Message mess = receivedMessageList.get(position);
                if (mess.getFiles() != null && mess.getFiles().size() > 0) {
                    message.setFiles(mess.getFiles());
                }
                undeliveredMessages.remove(mess);
                if (mess.getMid() == null && mess.getMessageBody().equals(message.getMessageBody()) || mess.getMid() != null && mess.getMid().equals(message.getMid())) {
                    isDifferent = false;
                    Log.v(DialogActivity.class.getSimpleName(), "MID1: " + mess.getMid() + ", MID2: " + message.getMid());
                }
                receivedMessageList.set(position, message);
                messagesAdapter.notifyItemChanged(insertedPosition = position);
            }
        }

        if (insertedPosition == null) {
            receivedMessageList.add(message);
            messagesAdapter.notifyItemInserted(insertedPosition = receivedMessageList.size() - 1);
        }

        Log.v(DialogActivity.class.getSimpleName() + ".addMessage", "Position: " + insertedPosition + ", mid: " + message.getMid() + ", diff: " + isDifferent + ", size: " + receivedMessageList.size());

        if (atBottom) {
            messagesList.scrollToPosition(receivedMessageList.size() - 1);
        } else if (isDifferent && insertedPosition >= itemCount - 1) {
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

    /**
     *
     * @param message new message for dispatch
     * @return Returns whether the message belongs to this chat or not
     */
    @Nullable
    private boolean dispatchNewMessage(Message message) {
        String newChatID = message.getCid();
        if (chatID == null) {
            chatID = newChatID;
            onUpdateChatID();
        }

        if (chatID != null && !chatID.equals(newChatID)) {
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);

            notificationBuilder.setSmallIcon(R.drawable.ic_message_black_24dp)
                    .setContentTitle(message.getTitle())
                    .setContentText(message.getMessageBody());

            Intent intent = new Intent(this, DialogActivity.class);
            intent.putExtra(CHAT_ID, newChatID);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            notificationBuilder.setContentIntent(pendingIntent);

            Notification notification = notificationBuilder.build();
            notification.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.notify(0, notification);
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onIncomeMessage(Message message){
        boolean belongs = dispatchNewMessage(message);
        if (belongs)
            addMessage(message);
    }

    @Override
    public void onActionDeleteMessage(int mid) {
        removeMessage(String.valueOf(mid));
    }

    @Override
    public void onAcknowledgeSendMessage(Message message){
        boolean belongs = dispatchNewMessage(message);
        if (belongs)
            addMessage(message);
    }

    @Override
    public void onUpdateWSStatus(WebSocketState state) {
        // TODO: use Android string resources
        boolean isOnline = state.equals(WebSocketState.OPEN);
        if (isOnline) {
            if (thisChat != null) {
                if (thisChat.getType() == Chat.INDIVIDUAL_TYPE) {

                } else {
                    dialogActionBar.setSubtitle(thisChat.getMembersCount() + " members");
                }
            } else {
                dialogActionBar.setSubtitle("Connected");
            }
        } else {
            dialogActionBar.setSubtitle("Not connected");
        }
        // TODO: consider using another type of status indication (as proposed by Anton)
        dialogActionBar.setProgress(!isOnline);
    }

    @Override
    public void onWrite(String cid, Contact user) {
        if (ObjectUtils.compare(cid, chatID) == 0) {
            for (int i = 0; i < writers.size(); i++) {
                if (writers.get(i).second.equals(user)) {
                    writers.set(i, new Pair<>(System.currentTimeMillis(), user));
                    return;
                }
            }
            writers.add(new Pair<>(System.currentTimeMillis(), user));
        }
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
    public void onUploadComplete(AttachedFile file) {
        attachemtsList.add(file);
        attachments.getAdapter().notifyItemInserted(attachemtsList.size() - 1);
        attachments.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        schedulerTimer.cancel();
    }

    public synchronized void onActivityResult(final int requestCode, int resultCode, final Intent data) {
        Log.d("[TP-diploma]", "preparing to send file");
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CODE_FILE_SELECTED) {
                String filePath = data.getStringExtra(FileDialog.RESULT_PATH);
                FileInputStream fstrm = null;
                HttpFileUpload hfu = null;

                Log.d("[TP-diploma]", "sending file started");
                try {
                    fstrm = new FileInputStream(filePath);
                    hfu = new HttpFileUpload(FILE_UPLOAD_URL, filePath.substring(filePath.lastIndexOf('/'), filePath.length()), accessToken);
                    hfu.Send_Now(fstrm, this);
                } catch(Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error opening file", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case REQUEST_WRITE_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //reload my activity with permission granted or use the features what required the permission
                } else {
                    finish();
                }
            }
        }
    }

    private final MessagesLoaderListener messagesLoaderListener = new MessagesLoaderListener();
    private class MessagesLoaderListener implements ScrollEndlessPagination.EndlessLoaderListener<Message> {
        private boolean endReached = false;

        @Override
        public boolean isEndReached() {
            return endReached;
        }

        @Override
        public Bundle getBundle() {
            Bundle args = new Bundle();
            args.putString(MessagesLoader.CID_ARG, chatID);
            args.putString(MessagesLoader.UID_ARG, userID);
            return args;
        }

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
            endReached = false;
            if (data != null) {
                int oldSize = receivedMessageList.size();
                for (Message message : data) {
                    addMessage(message);
                }

                if (!receivedFromWeb && receivedMessageList.size() != oldSize)
                    messagesList.scrollToPosition(receivedMessageList.size() - 1);
                if (loader.getId() == MESSAGES_WEB_LOADER) {
                    receivedFromWeb = true;

                    if (data.size() < pagination.getPageSize()) {
                        endReached = true;

                        Log.d(MessagesLoaderListener.class.getSimpleName(), "Not full data set");
                        Log.d(MessagesLoaderListener.class.getSimpleName(), "Fetched: " + data.size());
                        Log.d(MessagesLoaderListener.class.getSimpleName(), "Required: " + pagination.getPageSize());
                    }
                }
            } else if (chatID != null) {
                Toast.makeText(DialogActivity.this, "Connection error", Toast.LENGTH_SHORT).show();
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

    private final LoaderManager.LoaderCallbacks<Chat> chatLoaderListener =
            new LoaderManager.LoaderCallbacks<Chat>() {
        @Override
        public Loader<Chat> onCreateLoader(int id, Bundle args) {
            switch (id) {
                case MESSAGES_WEB_LOADER:
                    return new ChatInfoWebLoader(DialogActivity.this, args);
                case MESSAGES_DB_LOADER:
                default:
                    return new ChatInfoLoader(DialogActivity.this, args);
            }
        }

        @Override
        public void onLoadFinished(Loader<Chat> loader, Chat data) {
            if (data != null) {
                dialogActionBar.setChatData(data, getImageDownloadManager());
                thisChat = data;
            }

            if (loader.getId() == CHAT_DB_LOADER && chatID != null) {
                Bundle args = new Bundle();
                args.putString(ChatInfoLoader.CID_ARG, chatID);
                getLoaderManager().restartLoader(CHAT_WEB_LOADER, args, this);
            }
        }

        @Override
        public void onLoaderReset(Loader<Chat> loader) {
            // TODO: something
        }
    };

    @Override
    protected void onSetImageManager(ImageDownloadManager mgr) {
        if (messagesAdapter != null) {
            messagesAdapter.setImageDownloadManager(mgr);
        }
    }

    @Override
    public void addDispatchers(NotificationService notificationService) {
        super.addDispatchers(notificationService);
        notificationService.getMessages().getWsStatusNotifier(uiHandler).setWsStatusListener(this);
        onUpdateWSStatus(notificationService.getMessages().getWsStatus());
    }

    @Override
    public void removeDispatchers(NotificationService notificationService) {
        super.removeDispatchers(notificationService);
        notificationService.getMessages().getWsStatusNotifier(uiHandler).setWsStatusListener(null);
    }
}
