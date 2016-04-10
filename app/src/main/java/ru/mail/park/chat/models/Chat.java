package ru.mail.park.chat.models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ru.mail.park.chat.database.ChatsContract;

/**
 * Created by Михаил on 06.03.2016.
 */
public class Chat {
    @NonNull
    private String cid;
    @NonNull
    private String name;
    @Nullable
    private String description;

    public Chat(Cursor cursor) {
        cid = cursor.getString(ChatsContract.PROJECTION_CID_INDEX);
        name = cursor.getString(ChatsContract.PROJECTION_NAME_INDEX);
        description = cursor.getString(ChatsContract.PROJECTION_DESCRIPTION_INDEX);
    }

    private static final int GROUP_TYPE = 1;
    private static final int INDIVIDUAL_TYPE = 0;

    public Chat(JSONObject chat, Context context) throws JSONException {
        int type = Integer.valueOf(chat.getString("type"));
        String uid = new OwnerProfile(context).getUid();

        setCid(chat.getString("idRoom"));
        switch (type) {
            case GROUP_TYPE:
                setName(chat.getString("name"));
                break;
            case INDIVIDUAL_TYPE:
                JSONArray listUser = chat.getJSONArray("listUser");
                for (int i = 0; i < listUser.length(); i++) {
                    JSONObject user = listUser.getJSONObject(i);
                    String currentUID = user.getString("id");
                    if (!currentUID.equals(uid)) {
                        String firstName = user.getString("firstName");
                        String lastName = user.getString("lastName");
                        setName(firstName + " " + lastName);
                        break;
                    }
                }
                break;
        }
        if (chat.has("text"))
            setDescription(chat.getString("text"));
    }

    @NonNull
    public String getCid() {
        return cid;
    }

    private void setCid(@NonNull String cid) {
        this.cid = cid;
    }

    @NonNull
    public String getName() {
        return name;
    }

    private void setName(@NonNull String name) {
        this.name = name;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    private void setDescription(@Nullable String description) {
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
