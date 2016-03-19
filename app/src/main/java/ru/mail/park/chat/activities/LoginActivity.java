package ru.mail.park.chat.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import ru.mail.park.chat.activities.tasks.LoginTask;
import ru.mail.park.chat.authentication.DummyAuthable;
import ru.mail.park.chat.authentication.IAuthCallbacks;
import ru.mail.park.chat.authentication.IAuthable;
import ru.mail.park.chat.R;
import ru.mail.park.chat.models.Contact;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements IAuthCallbacks  {

    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private TextView tvRegisterLink;
    private Button mEmailSignInButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(onPasswordListener);

        mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(onSignInListener);

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        tvRegisterLink = (TextView) findViewById(R.id.register_link);
        tvRegisterLink.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginActivity.this.startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
    }

    TextView.OnEditorActionListener onPasswordListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
            if (id == R.id.login || id == EditorInfo.IME_NULL) {
                authenticate();
                return true;
            }
            return false;
        }
    };

    OnClickListener onSignInListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            authenticate();
        }
    };

    private void authenticate() {
        mPasswordView.setOnEditorActionListener(null);
        mEmailSignInButton.setOnClickListener(null);

        String login = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        new LoginTask(this, this).execute(login, password);
    }


    @Override
    public void onStartAuth() {
        mProgressView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoginSuccess(Contact contact) {
        mProgressView.setVisibility(View.GONE);
        Intent intent = new Intent(this, ChatsActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onLoginFail(String message) {
        mPasswordView.setOnEditorActionListener(onPasswordListener);
        mEmailSignInButton.setOnClickListener(onSignInListener);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}

