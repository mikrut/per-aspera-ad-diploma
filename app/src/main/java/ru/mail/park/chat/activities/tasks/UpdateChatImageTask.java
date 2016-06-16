package ru.mail.park.chat.activities.tasks;

import android.app.DownloadManager;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Pair;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;

import java.io.File;
import java.io.IOException;

import ru.mail.park.chat.api.Auth;
import ru.mail.park.chat.api.Chats;
import ru.mail.park.chat.database.ChatsHelper;
import ru.mail.park.chat.models.Chat;
import ru.mail.park.chat.models.OwnerProfile;

/**
 * Created by Михаил on 16.06.2016.
 */
public class UpdateChatImageTask extends AsyncTask<Void, Void, Chats.ImageUpdateResult> {
    private final IOperationListener<Chats.ImageUpdateResult, String> listener;
    private final Context context;
    private final String cid;
    private final File file;

    private String failMessage;

    public UpdateChatImageTask(Context context,
                               String cid, File file,
                               IOperationListener<Chats.ImageUpdateResult, String> listener) {
        this.context = context;
        this.cid = cid;
        this.file = file;
        this.listener = listener;
        listener.onOperationStart();
    }

    @Override
    protected Chats.ImageUpdateResult doInBackground(Void... params) {
        final File imageFile = file;

        try {
            Chats chats = new Chats(context);
            Chats.ImageUpdateResult iur = chats.updateImage(cid, imageFile);
            ChatsHelper helper = new ChatsHelper(context);
            Chat chat = helper.getChat(cid);
            if (chat != null) {
                chat.setImagePath(iur.image);
                helper.saveChat(chat);
            }
            return iur;
        } catch (IOException e) {
            failMessage = e.getLocalizedMessage();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Chats.ImageUpdateResult response) {
        if (response != null) {
            listener.onOperationSuccess(response);
        } else {
            listener.onOperationFail(failMessage);
        }
    }
}