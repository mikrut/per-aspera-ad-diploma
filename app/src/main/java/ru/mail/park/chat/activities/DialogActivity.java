package ru.mail.park.chat.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.widget.EditText;
import android.widget.ImageButton;

import ru.mail.park.chat.R;

public class DialogActivity extends AppCompatActivity {
    public RecyclerView messagesList;
    public ImageButton insertEmoticon;
    public EditText inputMessage;
    public ImageButton sendMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);

        messagesList = (RecyclerView) findViewById(R.id.messagesList);
        insertEmoticon = (ImageButton) findViewById(R.id.insertEmoticon);
        inputMessage = (EditText) findViewById(R.id.inputMessage);
        sendMessage = (ImageButton) findViewById(R.id.sendMessage);
    }
}
