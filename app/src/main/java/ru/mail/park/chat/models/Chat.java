package ru.mail.park.chat.models;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import ru.mail.park.chat.database.ChatsContract;

/**
 * Created by Михаил on 06.03.2016.
 */
public class Chat {
    @NonNull String cid;
    @NonNull String name;
    @Nullable String description;

    public Chat(Cursor cursor) {
        cid = cursor.getString(ChatsContract.PROJECTION_CID_INDEX);
        name = cursor.getString(ChatsContract.PROJECTION_NAME_INDEX);
        description = cursor.getString(ChatsContract.PROJECTION_DESCRIPTION_INDEX);
    }

    public Chat(JSONObject chat) throws JSONException {
        setCid(chat.getString("idRoom"));
        if (chat.has("title"))
            setName(chat.getString("title"));
        if (chat.has("text"))
            setDescription(chat.getString("text"));
    }

    @NonNull
    public String getCid() {
        return cid;
    }

    public void setCid(@NonNull String cid) {
        this.cid = cid;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    public void setDescription(@Nullable String description) {
        this.description = description;
    }

    @NonNull
    public ContentValues getContentValues() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ChatsContract.ChatsEntry.COLUMN_NAME_CID, cid);
        contentValues.put(ChatsContract.ChatsEntry.COLUMN_NAME_NAME, name);
        contentValues.put(ChatsContract.ChatsEntry.COLUMN_NAME_DESCRIPTION, description);
        return contentValues;
    }
}
