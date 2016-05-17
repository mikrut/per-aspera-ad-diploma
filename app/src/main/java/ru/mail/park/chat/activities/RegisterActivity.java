package ru.mail.park.chat.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.Map;

import ru.mail.park.chat.R;
import ru.mail.park.chat.activities.tasks.RegisterTask;
import ru.mail.park.chat.auth_signup.IRegisterCallbacks;
import ru.mail.park.chat.models.OwnerProfile;

public class RegisterActivity extends AppCompatActivity implements IRegisterCallbacks {

    private AutoCompleteTextView mLoginView;
    private AutoCompleteTextView mFirstNameView;
    private AutoCompleteTextView mLastNameView;
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private ProgressBar mProgressView;
    private Button emailSignUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mPasswordView = (EditText) findViewById(R.id.register_password);
        mEmailView = (AutoCompleteTextView) findViewById(R.id.register_email);
        mLoginView = (AutoCompleteTextView) findViewById(R.id.register_login);
        mFirstNameView = (AutoCompleteTextView) findViewById(R.id.register_first_name);
        mLastNameView = (AutoCompleteTextView) findViewById(R.id.register_last_name);

        mProgressView = (ProgressBar) findViewById(R.id.register_progress);
        emailSignUpButton = (Button) findViewById(R.id.email_sign_up_button);

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
        String email = mEmailView.getText().toString();

        task.execute(login, firstName, lastName, password, email);
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
