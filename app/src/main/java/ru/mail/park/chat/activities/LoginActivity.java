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

import ru.mail.park.chat.authentication.DummyAuthable;
import ru.mail.park.chat.authentication.IAuthCallbacks;
import ru.mail.park.chat.authentication.IAuthable;
import ru.mail.park.chat.R;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements IAuthCallbacks, OnClickListener  {

    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private Button btnSignUp;

    private final IAuthable authable = new DummyAuthable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        btnSignUp = (Button) findViewById(R.id.sign_up_button);
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    authable.auth(mEmailView.getText().toString(),
                            mPasswordView.getText().toString(), LoginActivity.this);
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                authable.auth(mEmailView.getText().toString(),
                        mPasswordView.getText().toString(), LoginActivity.this);
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        btnSignUp.setOnClickListener(this);
    }

    public void onStartAuth() {
        mProgressView.setVisibility(View.VISIBLE);
    }

    public void onFinishAuth(boolean isSuccess, String message) {
        mProgressView.setVisibility(View.GONE);
        if (isSuccess) {
            Intent intent = new Intent(this, ChatsActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_up_button:
                Intent intent = new Intent(this, RegisterActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }
}

