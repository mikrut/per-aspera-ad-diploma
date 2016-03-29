package ru.mail.park.chat.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import ru.mail.park.chat.R;
import ru.mail.park.chat.activities.tasks.UpdateProfileTask;
import ru.mail.park.chat.models.OwnerProfile;

public class ProfileActivity extends AppCompatActivity {

    private ImageView imgCameraShot;
    private ImageView imgUploadPicture;
    private TextView  userTitle;
    public static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final int GET_FROM_GALLERY = 3;

    private EditText userLogin;
    private EditText userEmail;
    private EditText firstName;
    private EditText lastName;
    private EditText userPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        imgCameraShot = (ImageView) findViewById(R.id.user_camera_shot);
        imgUploadPicture = (ImageView) findViewById(R.id.user_upload_picture);
        userTitle = (TextView) findViewById(R.id.user_title);

        userLogin = (EditText) findViewById(R.id.user_login);
        userEmail = (EditText) findViewById(R.id.user_email);
        userPhone = (EditText) findViewById(R.id.user_phone);
        firstName = (EditText) findViewById(R.id.first_name);
        lastName  = (EditText) findViewById(R.id.last_name);

        OwnerProfile ownerProfile = new OwnerProfile(this);
        userTitle.setText(ownerProfile.getContactTitle());
        userLogin.setText(ownerProfile.getLogin());
        userEmail.setText(ownerProfile.getEmail());
        userPhone.setText(ownerProfile.getPhone());
        firstName.setText(ownerProfile.getFirstName());
        lastName.setText(ownerProfile.getLastName());

        imgCameraShot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        });

        imgUploadPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent uploadPictureIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(uploadPictureIntent, GET_FROM_GALLERY);
            }
        });
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile, menu);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(android.R.drawable.ic_menu_close_clear_cancel);
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        OwnerProfile currentProfile = new OwnerProfile(this);
        OwnerProfile updatedProfile = getUpdatedProfile();

        if (!currentProfile.equals(updatedProfile)) {
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
            alertBuilder.setMessage("Cancel changes?");
            alertBuilder.setPositiveButton(getString(android.R.string.ok),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ProfileActivity.super.onBackPressed();
                        }
                    });
            alertBuilder.setNegativeButton(getString(android.R.string.cancel), null);
            alertBuilder.show();
        } else {
            ProfileActivity.super.onBackPressed();
        }
    }

    private OwnerProfile getUpdatedProfile() {
        OwnerProfile profile = new OwnerProfile(this);
        profile.setEmail(userEmail.getText().toString());
        profile.setLogin(userLogin.getText().toString());
        profile.setPhone(userPhone.getText().toString());
        profile.setFirstName(firstName.getText().toString());
        profile.setLastName(lastName.getText().toString());
        return profile;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                OwnerProfile profile = getUpdatedProfile();

                ProgressDialog.Builder dialogBuilder = new android.app.AlertDialog.Builder(this);
                dialogBuilder.setTitle("Saving user data");
                dialogBuilder.setMessage("Sending data to server");
                dialogBuilder.setCancelable(false);
                new UpdateProfileTask(dialogBuilder.show(), this).execute(profile);
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
