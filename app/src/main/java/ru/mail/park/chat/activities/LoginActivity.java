package ru.mail.park.chat.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import info.guardianproject.netcipher.NetCipher;
import info.guardianproject.netcipher.proxy.OrbotHelper;
import ru.mail.park.chat.activities.tasks.LoginTask;
import ru.mail.park.chat.auth_signup.IAuthCallbacks;
import ru.mail.park.chat.R;
import ru.mail.park.chat.database.MessengerDBHelper;
import ru.mail.park.chat.database.PreferenceConstants;
import ru.mail.park.chat.models.OwnerProfile;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements IAuthCallbacks  {

    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private ImageView appImage;
    private CheckBox withoutTorAllowedCheckBox;
    private TextView tvRegisterLink;
    private TextView appLogo;
    private Button mEmailSignInButton;

    private LoginTask loginTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        appImage = (ImageView) findViewById(R.id.app_picture);

        appLogo = (TextView) findViewById(R.id.app_logo_text);
        Typeface face= Typeface.createFromAsset(getAssets(),
                "fonts/MontserratAlternates-Bold.ttf");

        appLogo.setTypeface(face);

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(onPasswordListener);

        mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(onSignInListener);

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        withoutTorAllowedCheckBox = (CheckBox) findViewById(R.id.withoutTorAllowedCheckBox);
        withoutTorAllowedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences preferences =
                        PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(PreferenceConstants.SECURITY_PARANOID_N, !isChecked);
                editor.commit();
            }
        });

        tvRegisterLink = (TextView) findViewById(R.id.register_link);
        tvRegisterLink.setPaintFlags(tvRegisterLink.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);
        tvRegisterLink.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginActivity.this.startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        SharedPreferences sharedPreferences = getSharedPreferences(
                PreferenceConstants.PREFERENCE_NAME,
                Context.MODE_PRIVATE);
        if (sharedPreferences.getString(PreferenceConstants.AUTH_TOKEN_N, null) != null) {
            Intent intent = new Intent(this, ChatsActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
        boolean paranoidCheker = OrbotHelper.isOrbotInstalled(this);
        withoutTorAllowedCheckBox.setChecked(!preferences.getBoolean(PreferenceConstants.SECURITY_PARANOID_N, paranoidCheker));
    }

    private final TextView.OnEditorActionListener onPasswordListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
            if (id == R.id.login || id == EditorInfo.IME_NULL) {
                authenticate();
                return true;
            }
            return false;
        }
    };

    private final OnClickListener onSignInListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            authenticate();
        }
    };

    private void authenticate() {
        String login = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        if (loginTask != null)
            loginTask.cancel(true);
        loginTask = new LoginTask(this, this);
        loginTask.execute(login, password);
    }


    @Override
    public void onStartAuth() {
        mProgressView.setVisibility(View.VISIBLE);
        appImage.setVisibility(View.GONE);
    }

    @Override
    public void onLoginSuccess(OwnerProfile contact) {
        mProgressView.setVisibility(View.GONE);
        appImage.setVisibility(View.VISIBLE);

        contact.saveToPreferences(this);
        Intent intent = new Intent(this, ChatsActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onLoginFail(String message) {
        mProgressView.setVisibility(View.GONE);
        appImage.setVisibility(View.VISIBLE);

        mPasswordView.setOnEditorActionListener(onPasswordListener);
        mEmailSignInButton.setOnClickListener(onSignInListener);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (loginTask != null) {
            loginTask.cancel(true);
            loginTask = null;
        }
    }
}

