package ru.mail.park.chat.activities;

import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import org.w3c.dom.Text;

import ru.mail.park.chat.R;
import ru.mail.park.chat.activities.fragments.ContactsFragment;
import ru.mail.park.chat.activities.fragments.ContactsGroupFragment;
import ru.mail.park.chat.database.ChatsHelper;
import ru.mail.park.chat.database.ContactsHelper;
import ru.mail.park.chat.loaders.images.ImageDownloadManager;
import ru.mail.park.chat.models.Chat;

public class GroupDialogEditActivity extends AImageDownloadServiceBindingActivity {
    public static final String ARG_CID = GroupDialogEditActivity.class.getCanonicalName() + ".ARG_CID";

    private ImageView toolbarImage;
    private TextView toolbarTitle;
    private TextView toolbarSubtitle;

    private Button addMemberButton;
    private ImageButton setPictureButton;

    public static final String CONTACTS_TAG = GroupDialogEditActivity.class.getCanonicalName() + ".CONTACTS_TAG";
    private Chat chat;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_dialog_edit);

        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        collapsingToolbar.setTitleEnabled(false);

        toolbarImage = (ImageView) findViewById(R.id.toolbar_image);
        toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        toolbarSubtitle = (TextView) findViewById(R.id.toolbar_subtitle);

        addMemberButton = (Button) findViewById(R.id.add_member_button);
        setPictureButton = (FloatingActionButton) findViewById(R.id.button_set_image);

        String cid = getIntent().getExtras().getString(ARG_CID);
        ChatsHelper helper = new ChatsHelper(this);
        chat = helper.getChat(cid);
        toolbarTitle.setText(chat.getName());
        toolbarSubtitle.setText(chat.getChatUsers().size() + " user" + (chat.getChatUsers().size() > 1 ? "s" : ""));

        ContactsGroupFragment fragment = new ContactsGroupFragment();
        Bundle args = new Bundle();
        args.putString(ContactsGroupFragment.ARG_CID, cid);
        fragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.main_container, fragment, CONTACTS_TAG);
        transaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_group_dialog_edit, menu);
        return true;
    }

    @Override
    protected void onSetImageManager(ImageDownloadManager mgr) {
        if (chat.getImagePath() != null)
            mgr.setImage(toolbarImage, chat.getImagePath(), ImageDownloadManager.Size.NORMAL);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit_name:
                // TODO: Edit group chat name
                break;
            case R.id.action_delete:
                // TODO: Delete and leave group
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
