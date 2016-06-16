package ru.mail.park.chat.activities;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import ru.mail.park.chat.R;
import ru.mail.park.chat.activities.fragments.ContactsFragment;
import ru.mail.park.chat.activities.fragments.ContactsGroupFragment;
import ru.mail.park.chat.activities.tasks.IOperationListener;
import ru.mail.park.chat.activities.tasks.UpdateChatImageTask;
import ru.mail.park.chat.api.Chats;
import ru.mail.park.chat.database.ChatsHelper;
import ru.mail.park.chat.database.ContactsHelper;
import ru.mail.park.chat.loaders.images.ImageDownloadManager;
import ru.mail.park.chat.models.Chat;

public class GroupDialogEditActivity extends AImageDownloadServiceBindingActivity {
    public static final String ARG_CID =
            GroupDialogEditActivity.class.getCanonicalName() + ".ARG_CID";

    private ImageView toolbarImage;
    private TextView toolbarTitle;
    private TextView toolbarSubtitle;

    private Button addMemberButton;
    private ImageButton setPictureButton;

    public static final String CONTACTS_TAG =
            GroupDialogEditActivity.class.getCanonicalName() + ".CONTACTS_TAG";
    private static final int PICK_IMAGE = 0;

    private Chat chat;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_dialog_edit);

        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
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

        setPictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pickIntent = new Intent();
                pickIntent.setType("image/*");
                pickIntent.setAction(Intent.ACTION_GET_CONTENT);
                Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                String pickTitle = "Take or select a photo";
                Intent chooserIntent = Intent.createChooser(pickIntent, pickTitle);
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] { takePhotoIntent });
                startActivityForResult(chooserIntent, PICK_IMAGE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            Cursor cursor = null;
            try {
                InputStream stream = getContentResolver().openInputStream(
                        data.getData());
                Bitmap photo = BitmapFactory.decodeStream(stream);
                Uri tempUri = getImageUri(this, photo);
                final File imageFile = new File(getRealPathFromURI(tempUri));
                UpdateChatImageTask task =
                        new UpdateChatImageTask(this, chat.getCid(), imageFile, listener);
                task.execute();
            } catch (FileNotFoundException | IllegalArgumentException | NullPointerException e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public String getRealPathFromURI(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);
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

    private IOperationListener<Chats.ImageUpdateResult, String> listener =
            new IOperationListener<Chats.ImageUpdateResult, String>() {
                @Override
                public void onOperationStart() {

                }

                @Override
                public void onOperationSuccess(Chats.ImageUpdateResult imageUpdateResult) {
                    ImageDownloadManager idm = getImageDownloadManager();
                    if (idm != null) {
                        idm.setImage(toolbarImage, imageUpdateResult.image, ImageDownloadManager.Size.NORMAL);
                    }
                }

                @Override
                public void onOperationFail(String s) {
                    Toast.makeText(GroupDialogEditActivity.this, s, Toast.LENGTH_SHORT).show();
                }
            };
}
