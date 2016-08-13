package ru.mail.park.chat.activities;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import ru.mail.park.chat.R;
import ru.mail.park.chat.activities.tasks.UpdateProfileTask;
import ru.mail.park.chat.api.ApiSection;
import ru.mail.park.chat.api.HttpFileUpload;
import ru.mail.park.chat.activities.interfaces.IUploadListener;
import ru.mail.park.chat.loaders.images.ImageDownloadManager;
import ru.mail.park.chat.models.OwnerProfile;

public class ProfileEditActivity
        extends AImageDownloadServiceBindingActivity
        implements IUploadListener {
    private static final int REQUEST_WRITE_STORAGE = 112;

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int GET_FROM_GALLERY = 3;

    private ImageView imgCameraShot;
    private ImageView imgUploadPicture;
    private ImageView currentAvatar;
    private TextView  userTitle;

    private EditText userLogin;
    private EditText userEmail;
    private EditText firstName;
    private EditText lastName;
    private EditText userAbout;

    private Intent takePictureIntent;

    private static final String SELECTED_FILE_PATH_KEY = "selectedFilePath";
    private static final String M_IMAGE_URI = "mImageUri";
    private static final String CHANGED_FIELDS = "changedFields";

    private String selectedFilePath;
    private Uri mImageUri;
    private HashMap<String, Boolean> changedFields;

    private UpdateProfileTask updateProfileTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        changedFields = new HashMap<>();

        imgCameraShot = (ImageView) findViewById(R.id.user_camera_shot);
        imgUploadPicture = (ImageView) findViewById(R.id.user_upload_picture);
        currentAvatar = (ImageView) findViewById(R.id.user_picture_in_editor);
        userTitle = (TextView) findViewById(R.id.user_title);

        userLogin = (EditText) findViewById(R.id.user_login);
        userEmail = (EditText) findViewById(R.id.user_email);
        firstName = (EditText) findViewById(R.id.first_name);
        lastName  = (EditText) findViewById(R.id.last_name);
        userAbout = (EditText) findViewById(R.id.about_field);

        OwnerProfile ownerProfile = new OwnerProfile(this);
        userTitle.setText(ownerProfile.getContactTitle());
        userLogin.setText(ownerProfile.getLogin());
        userEmail.setText(ownerProfile.getEmail());
        firstName.setText(ownerProfile.getFirstName());
        lastName.setText(ownerProfile.getLastName());
        userAbout.setText(ownerProfile.getAbout());

        userLogin.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                // Прописываем то, что надо выполнить после изменения текста
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!changedFields.containsKey("userLogin")) {
                    changedFields.put("userLogin", true);
                    userLogin.removeTextChangedListener(this);
                }
            }
        });

        userEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                // Прописываем то, что надо выполнить после изменения текста
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!changedFields.containsKey("userEmail")) {
                    changedFields.put("userEmail", true);
                    userLogin.removeTextChangedListener(this);
                }
            }
        });

        firstName.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                // Прописываем то, что надо выполнить после изменения текста
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!changedFields.containsKey("firstName")) {
                    changedFields.put("firstName", true);
                    userLogin.removeTextChangedListener(this);
                }
            }
        });

        lastName.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                // Прописываем то, что надо выполнить после изменения текста
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!changedFields.containsKey("lastName")) {
                    changedFields.put("lastName", true);
                    userLogin.removeTextChangedListener(this);
                }
            }
        });

        userAbout.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                // Прописываем то, что надо выполнить после изменения текста
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!changedFields.containsKey("userAbout")) {
                    changedFields.put("userAbout", true);
                    userLogin.removeTextChangedListener(this);
                }
            }
        });

        imgCameraShot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                File photo = null;
                try {
                    // place where to store camera taken picture
                    Log.d("[TP-diploma]", "creating tmp file");
                    photo = ProfileEditActivity.this.createTemporaryFile("picture", ".jpg");
                    photo.delete();
                } catch (Exception e) {
                    Log.d("[TP-diploma]", "Can't create file to take picture!");
                }
                mImageUri = Uri.fromFile(photo);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
                //start camera intent

                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    Log.d("[TP-diploma]", "starting activity");
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
    protected void onSetImageManager(ImageDownloadManager mgr) {
        OwnerProfile owner = new OwnerProfile(this);
        if (selectedFilePath == null) {
            try {
                URL url = new URL(ApiSection.SERVER_URL + owner.getImg());
                mgr.setImage(currentAvatar, url);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }

    private File createTemporaryFile(String part, String ext) throws Exception
    {
        File tempDir= Environment.getExternalStorageDirectory();
        tempDir=new File(tempDir.getAbsolutePath()+"/.temp/");
        if(!tempDir.exists())
        {
            tempDir.mkdirs();
        }
        Log.d("[TP-diploma]", "inside createTemporaryFile");
        return File.createTempFile(part, ext, tempDir);
    }

    public synchronized void onActivityResult(final int requestCode, int resultCode, final Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            String filePath = "";
            FileInputStream fstrm = null;
            HttpFileUpload hfu = null;

            Log.d("[TP-diploma]", "onActivityResult");

            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                if(resultCode == Activity.RESULT_OK) {
                    Log.d("[TP-diploma]", "sending file started");
                    try {
                        selectedFilePath = mImageUri.getPath();
                        changedFields.put("img", true);
                        Toast.makeText(ProfileEditActivity.this, "camera shot: "+selectedFilePath, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else if(requestCode == GET_FROM_GALLERY) {
                if(resultCode == Activity.RESULT_OK) {
                    try {
                        Uri selectedImage = data.getData();
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};

                        Cursor cursor = getContentResolver().query(
                                selectedImage, filePathColumn, null, null, null);
                        cursor.moveToFirst();

                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        filePath = cursor.getString(columnIndex);
                        cursor.close();

                        selectedFilePath = filePath;
                        changedFields.put("img", true);
                        Toast.makeText(ProfileEditActivity.this, "from gallery: "+selectedFilePath, Toast.LENGTH_SHORT).show();
                    } catch(Exception e) {
                        Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            if(selectedFilePath != null)
                currentAvatar.setImageURI(Uri.parse(selectedFilePath));
        }
    }

    public void onUploadComplete(String name) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile_edit, menu);

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
                            ProfileEditActivity.super.onBackPressed();
                        }
                    });
            alertBuilder.setNegativeButton(getString(android.R.string.cancel), null);
            alertBuilder.show();
        } else {
            ProfileEditActivity.super.onBackPressed();
        }
    }

    private OwnerProfile getUpdatedProfile() {
        OwnerProfile profile = new OwnerProfile(this);
        if(changedFields.containsKey("userEmail"))
            profile.setEmail(userEmail.getText().toString());

        if(changedFields.containsKey("userLogin"))
            profile.setLogin(userLogin.getText().toString());

        if(changedFields.containsKey("firstName"))
            profile.setFirstName(firstName.getText().toString());

        if(changedFields.containsKey("lastName"))
            profile.setLastName(lastName.getText().toString());

        if(changedFields.containsKey("img")) {
            profile.setImg(selectedFilePath);
        } else {
            profile.setImg(null);
        }

        if(changedFields.containsKey("userAbout"))
            profile.setAbout(userAbout.getText().toString());

        return profile;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                OwnerProfile profile = getUpdatedProfile();
                OwnerProfile currentProfile = new OwnerProfile(this);

                if (!profile.equals(currentProfile)) {
                    ProgressDialog.Builder dialogBuilder = new android.app.AlertDialog.Builder(this);
                    dialogBuilder.setTitle("Saving user data");
                    dialogBuilder.setMessage("Sending data to server");
                    dialogBuilder.setCancelable(false);

                    if (updateProfileTask != null)
                        updateProfileTask.cancel(true);
                    updateProfileTask = new UpdateProfileTask(dialogBuilder.show(), getImageDownloadManager(), this);
                    updateProfileTask.execute(profile);
                } else {
                    onBackPressed();
                }

                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (updateProfileTask != null)
            updateProfileTask.cancel(true);
    }
}
