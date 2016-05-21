package ru.mail.park.chat.activities;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import ru.mail.park.chat.R;
import ru.mail.park.chat.activities.tasks.RegisterTask;
import ru.mail.park.chat.api.HttpFileUpload;
import ru.mail.park.chat.auth_signup.IRegisterCallbacks;
import ru.mail.park.chat.models.OwnerProfile;

public class RegisterActivity extends AppCompatActivity implements IRegisterCallbacks {
    private AutoCompleteTextView mLoginView;
    private AutoCompleteTextView mFirstNameView;
    private AutoCompleteTextView mLastNameView;
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private EditText mConfirmPassvordView;
    private ProgressBar mProgressView;
    private Button emailSignUpButton;

    //step two
    private RelativeLayout stepTwoLayout;
    private CircleImageView regImagePreview;
    private ImageButton regUserCameraShot;
    private ImageButton regUserUploadPicture;
    private Button regNextButton;
    private TextView noteIncorrectData;

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int GET_FROM_GALLERY = 3;
    private static final String FILE_UPLOAD_URL = "http://p30480.lab1.stud.tech-mail.ru/files/upload";
    private String selectedFilePath;
    private Uri mImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mPasswordView = (EditText) findViewById(R.id.register_password);
        mConfirmPassvordView = (EditText) findViewById(R.id.register_confirm_password);
        mEmailView = (AutoCompleteTextView) findViewById(R.id.register_email);
        mLoginView = (AutoCompleteTextView) findViewById(R.id.register_login);
        mFirstNameView = (AutoCompleteTextView) findViewById(R.id.register_first_name);
        mLastNameView = (AutoCompleteTextView) findViewById(R.id.register_last_name);

        mProgressView = (ProgressBar) findViewById(R.id.register_progress);
        emailSignUpButton = (Button) findViewById(R.id.email_sign_up_button);

        stepTwoLayout = (RelativeLayout) findViewById(R.id.layout_registration_step_two);
        regImagePreview = (CircleImageView) findViewById(R.id.reg_user_picture_in_editor);
        regUserCameraShot = (ImageButton) findViewById(R.id.reg_user_camera_shot);
        regUserUploadPicture = (ImageButton) findViewById(R.id.reg_user_upload_picture);
        regNextButton = (Button) findViewById(R.id.email_next_button);
        noteIncorrectData = (TextView) findViewById(R.id.note_error_input_data);

        noteIncorrectData.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        noteIncorrectData.setVisibility(View.GONE);

        emailSignUpButton.setOnClickListener(signUpListener);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null && toolbar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }

        regNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkRegistrationInfo(mLoginView.getText().toString(),
                                         mFirstNameView.getText().toString(),
                                         mLastNameView.getText().toString(),
                                         mPasswordView.getText().toString(),
                                         mConfirmPassvordView.getText().toString(),
                                         mEmailView.getText().toString())) {
                    noteIncorrectData.setVisibility(View.GONE);
                    switchSteps(true);
                    stepTwoLayout.setVisibility(View.VISIBLE);
                } else {
                    noteIncorrectData.setVisibility(View.VISIBLE);
                }
            }
        });

        regUserCameraShot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                File photo = null;
                try {
                    photo = RegisterActivity.this.createTemporaryFile("picture", ".jpg");
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

        regUserUploadPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent uploadPictureIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(uploadPictureIntent, GET_FROM_GALLERY);
            }
        });
    }

    private void switchSteps(boolean firstToSecond) {
        if(!firstToSecond) {
            stepTwoLayout.setVisibility(View.GONE);

            mPasswordView.setVisibility(View.VISIBLE);
            mConfirmPassvordView.setVisibility(View.VISIBLE);
            mEmailView.setVisibility(View.VISIBLE);
            mLoginView.setVisibility(View.VISIBLE);
            mFirstNameView.setVisibility(View.VISIBLE);
            mLastNameView.setVisibility(View.VISIBLE);
            regNextButton.setVisibility(View.VISIBLE);
        } else {
            mPasswordView.setVisibility(View.GONE);
            mConfirmPassvordView.setVisibility(View.GONE);
            mEmailView.setVisibility(View.GONE);
            mLoginView.setVisibility(View.GONE);
            mFirstNameView.setVisibility(View.GONE);
            mLastNameView.setVisibility(View.GONE);
            regNextButton.setVisibility(View.GONE);

            stepTwoLayout.setVisibility(View.VISIBLE);
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
            String filePath;

            Log.d("[TP-diploma]", "RegisterActivity onActivityResult");

            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                if(resultCode == Activity.RESULT_OK) {
                    Log.d("[TP-diploma]", "sending file started");
                    try {
                        selectedFilePath = mImageUri.getPath();
                        Toast.makeText(RegisterActivity.this, "camera shot: "+selectedFilePath, Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(RegisterActivity.this, "from gallery: "+selectedFilePath, Toast.LENGTH_SHORT).show();
                    } catch(Exception e) {
                        Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            if(selectedFilePath != null)
                regImagePreview.setImageBitmap(BitmapFactory.decodeFile(selectedFilePath));
        }
    }

    private final View.OnClickListener signUpListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            register();
        }
    };

    private void register() {
        RegisterTask task = new RegisterTask(this, this);
        String login = mLoginView.getText().toString();
        String firstName = mFirstNameView.getText().toString();
        String lastName = mLastNameView.getText().toString();
        String password = mPasswordView.getText().toString();
        String confirmPassword = mConfirmPassvordView.getText().toString();
        String email = mEmailView.getText().toString();
        boolean imageFound = true;

        File file = new File(selectedFilePath);
        if(!file.exists())
            imageFound = false;

        if(checkRegistrationInfo(login, firstName, lastName, password, confirmPassword, email))
            task.execute(login, firstName, lastName, password, email, imageFound ? selectedFilePath : null);
        else {
            noteIncorrectData.setVisibility(View.VISIBLE);
            switchSteps(false);
        }

    }

    private boolean checkRegistrationInfo(String login, String firstName, String lastName, String password, String confirmPassword, String email) {
        boolean result = true;

        if(!password.equals(confirmPassword))
            result = false;

        //add other data checks

        return result;
    }

    @Override
    public void onRegistrationStart() {
        mProgressView.setVisibility(View.VISIBLE);
        emailSignUpButton.setEnabled(false);
    }

    @Override
    public void onRegistrationSuccess(OwnerProfile contact) {
        contact.saveToPreferences(this);
        Intent intent = new Intent(this, ChatsActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onRegistrationFail(Map<ErrorType, String> errors) {
        mLoginView.setError(null);
        mFirstNameView.setError(null);
        mLastNameView.setError(null);
        mEmailView.setError(null);
        mPasswordView.setError(null);

        if (errors != null) {
            for (Map.Entry<ErrorType, String> error : errors.entrySet()) {
                switch (error.getKey()) {
                    case LOGIN:
                        mLoginView.setError(error.getValue());
                        break;
                    case FIRST_NAME:
                        mFirstNameView.setError(error.getValue());
                        break;
                    case LAST_NAME:
                        mLastNameView.setError(error.getValue());
                        break;
                    case EMAIL:
                        mEmailView.setError(error.getValue());
                        break;
                    case PASSWORD:
                        mPasswordView.setError(error.getValue());
                        break;
                }
            }
        } else {
            Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRegistrationFinish() {
        emailSignUpButton.setEnabled(true);
        mProgressView.setVisibility(View.GONE);
    }
}
