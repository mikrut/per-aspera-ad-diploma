package ru.mail.park.chat.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

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
    }

    private View.OnClickListener signUpListener = new View.OnClickListener() {
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_register, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
    public void onRegistrationFail(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRegistrationFinish() {
        emailSignUpButton.setEnabled(true);
        mProgressView.setVisibility(View.GONE);
    }
}
