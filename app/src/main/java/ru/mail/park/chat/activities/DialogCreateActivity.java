package ru.mail.park.chat.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.TreeSet;

import info.guardianproject.netcipher.proxy.OrbotHelper;
import ru.mail.park.chat.R;
import ru.mail.park.chat.activities.fragments.ContactsFragment;
import ru.mail.park.chat.activities.fragments.FSM;
import ru.mail.park.chat.activities.interfaces.ILoadOnionListener;
import ru.mail.park.chat.activities.tasks.LoadOnionTask;
import ru.mail.park.chat.database.ContactsToChatsHelper;
import ru.mail.park.chat.models.Chat;
import ru.mail.park.chat.models.Contact;

/**
 * Created by Михаил on 24.04.2016.
 */
public class DialogCreateActivity
        extends AImageDownloadServiceBindingActivity
        implements ContactsFragment.OnPickEventListener, ILoadOnionListener {
    TextView newGroupClickable;
    TextView newP2PClickable;

    private DialogCreateFSM fsm = new DialogCreateFSM();
    private Contact choosenContact = null;

    @Nullable
    private LoadOnionTask loadOnionTask;

    @Override
    public void onOnionLoaded(@NonNull Contact forContact, @Nullable String withResult) {
        choosenContact = forContact;
        fsm.handleEvent(UIEvent.CONTACT_ONION_RECEIVED);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog_create);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(fsm.createListener(UIEvent.BACK_PRESSED));

        newGroupClickable = (TextView) findViewById(R.id.new_group_dialog);
        newGroupClickable.setOnClickListener(fsm.createListener(UIEvent.NEW_GROUP_PRESSED));

        newP2PClickable = (TextView) findViewById(R.id.new_p2p_dialog);
        newP2PClickable.setOnClickListener(fsm.createListener(UIEvent.NEW_P2P_CHAT_PRESSED));
    }

    @Override
    public void onBackPressed() {
        fsm.handleEvent(UIEvent.BACK_PRESSED);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_dialog_create, menu);
        return true;
    }

    @Override
    public void onContactSetChanged(TreeSet<Contact> chosenContacts) {

    }

    @Override
    public void onContactClicked(Contact contact) {
        choosenContact = contact;
        fsm.handleEvent(UIEvent.CONTACT_PRESSED);
    }

    enum UIState {
        CHOOSE_DIALOG_TYPE,
        CREATE_P2P,
        OUT_BACK,
        OUT_NEW_GROUP,
        OUT_P2P,
        OUT_DIALOG
    }

    enum UIEvent {
        NEW_GROUP_PRESSED,
        NEW_P2P_CHAT_PRESSED,
        BACK_PRESSED,
        P2P_CREATE_PRESSED,
        CONTACT_PRESSED,
        VALIDATION_FAILED,
        CONTACT_ONION_RECEIVED
    }

    private class DialogCreateFSM extends FSM<UIEvent, UIState> {
        public DialogCreateFSM() {
            super(UIState.CHOOSE_DIALOG_TYPE);
        }

        @Override
        protected UIState nextState(UIEvent uiEvent, UIState currentState) {
            switch (uiEvent) {
                case NEW_GROUP_PRESSED:
                    return UIState.OUT_NEW_GROUP;
                case NEW_P2P_CHAT_PRESSED:
                    if (OrbotHelper.isOrbotInstalled(DialogCreateActivity.this)) {
                        return UIState.CREATE_P2P;
                    } else {
                        Toast.makeText(DialogCreateActivity.this, "Install OrbotHelper to use P2P!", Toast.LENGTH_SHORT).show();
                        return currentState;
                    }
                case BACK_PRESSED:
                    switch (currentState) {
                        case CREATE_P2P:
                            return UIState.CHOOSE_DIALOG_TYPE;
                        default:
                            return UIState.OUT_BACK;
                    }
                case P2P_CREATE_PRESSED:
                    return UIState.OUT_P2P;
                case CONTACT_PRESSED:
                    switch (currentState) {
                        case CREATE_P2P:
                            return UIState.OUT_P2P;
                        case CHOOSE_DIALOG_TYPE:
                            return UIState.OUT_DIALOG;
                        default:
                            return currentState;
                    }
                case VALIDATION_FAILED:
                    switch (currentState) {
                        case OUT_P2P:
                            return UIState.CREATE_P2P;
                        default:
                            return currentState;
                    }
                case CONTACT_ONION_RECEIVED:
                    return UIState.OUT_P2P;
                default:
                    return currentState;
            }
        }
    }

    FSMListener listener = new FSMListener();

    private class FSMListener implements DialogCreateFSM.FSMListener<UIState> {
        public FSMListener() {
            fsm.setListener(this);
        }

        @Override
        public void onStateChange(UIState newState) {
            switch (newState) {
                case CHOOSE_DIALOG_TYPE:
                    newGroupClickable.setVisibility(View.VISIBLE);
                    newP2PClickable.setVisibility(View.VISIBLE);
                    supportInvalidateOptionsMenu();
                    break;
                case CREATE_P2P:
                    newGroupClickable.setVisibility(View.GONE);
                    newP2PClickable.setVisibility(View.GONE);
                    supportInvalidateOptionsMenu();
                    break;
                case OUT_BACK:
                    DialogCreateActivity.super.onBackPressed();
                    break;
                case OUT_NEW_GROUP:
                    Intent newGroupIntent = new Intent(DialogCreateActivity.this, GroupDialogCreateActivity.class);
                    startActivity(newGroupIntent);
                    finish();
                    break;
                case OUT_P2P:
                    final String onionMatcherString = "[a-zA-Z\\d]{1,50}\u002Eonion";
                    if (choosenContact != null && choosenContact.getOnionAddress() != null &&
                            choosenContact.getOnionAddress().toString().matches(onionMatcherString)) {
                        String destinationUID = choosenContact.getUid();

                        Intent p2pIntent = new Intent(DialogCreateActivity.this, P2PDialogActivity.class);
                        p2pIntent.putExtra(P2PDialogActivity.HOST_ARG, destinationUID);
                        p2pIntent.putExtra(P2PDialogActivity.PORT_ARG, P2PDialogActivity.LISTENER_DEFAULT_PORT);
                        startActivity(p2pIntent);
                        finish();
                    } else {
                        if (loadOnionTask != null)
                            loadOnionTask.cancel(true);
                        loadOnionTask = new LoadOnionTask(DialogCreateActivity.this, DialogCreateActivity.this);
                        loadOnionTask.execute(choosenContact);

                        Toast.makeText(DialogCreateActivity.this, "No onion address", Toast.LENGTH_SHORT).show();
                        fsm.handleEvent(UIEvent.VALIDATION_FAILED);
                    }

                    choosenContact = null;

                    break;
                case OUT_DIALOG:
                    if (choosenContact != null) {
                        Intent intent = new Intent(DialogCreateActivity.this, DialogActivity.class);
                        ContactsToChatsHelper helper = new ContactsToChatsHelper(DialogCreateActivity.this);
                        Chat chat = helper.getChat(choosenContact.getUid());
                        if (chat != null) {
                            intent.putExtra(DialogActivity.CHAT_ID, chat.getCid());
                        } else {
                            intent.putExtra(DialogActivity.USER_ID, choosenContact.getUid());
                        }
                        startActivity(intent);
                        finish();
                    }
                    break;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (loadOnionTask != null)
            loadOnionTask.cancel(true);
        loadOnionTask = null;
    }
}
