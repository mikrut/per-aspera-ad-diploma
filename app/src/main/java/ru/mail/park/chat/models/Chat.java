package ru.mail.park.chat.models;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Objects;

import ru.mail.park.chat.api.ApiSection;
import ru.mail.park.chat.database.ChatsContract;
import ru.mail.park.chat.database.MessengerDBHelper;
import ru.mail.park.chat.database.PreferenceConstants;
import ru.mail.park.chat.loaders.MessagesDBLoader;

/**
 * Created by Михаил on 06.03.2016.
 */
public class Chat implements Serializable {
    @NonNull
    private String cid;
    @NonNull
    private String name;
    @Nullable
    private Calendar lastSeen;
    @Nullable
    private String description;
    @Nullable
    private Calendar dateTime;
    private int membersCount;

    private int type;
    private List<Contact> chatUsers = new ArrayList<>();

    @Nullable
    private URL imagePath;

    public Chat(Cursor cursor) {
        cid = cursor.getString(ChatsContract.PROJECTION_CID_INDEX);
        name = cursor.getString(ChatsContract.PROJECTION_NAME_INDEX);
        if (!cursor.isNull(ChatsContract.PROJECTION_DESCRIPTION_INDEX))
            description = cursor.getString(ChatsContract.PROJECTION_DESCRIPTION_INDEX);
        type = cursor.getInt(ChatsContract.PROJECTION_TYPE_INDEX);
        if (!cursor.isNull(ChatsContract.PROJECTION_DATETIME_INDEX)) {
            dateTime = GregorianCalendar.getInstance();
            long timeInMillis = 1000L * cursor.getInt(ChatsContract.PROJECTION_DATETIME_INDEX);
            dateTime.setTimeInMillis(timeInMillis);
        } else {
            dateTime = null;
        }
        if (!cursor.isNull(ChatsContract.PROJECTION_IMAGE_URL_INDEX)) {
            try {
                imagePath = new URL(cursor.getString(ChatsContract.PROJECTION_IMAGE_URL_INDEX));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        membersCount = cursor.getInt(ChatsContract.PROJECTION_MEMBERS_COUNT_INDEX);
    }

    public static final int GROUP_TYPE = 1;
    public static final int INDIVIDUAL_TYPE = 0;

    public Chat(JSONObject chat, Context context) throws JSONException {
        type = GROUP_TYPE;
        if (chat.has("type"))
            type = Integer.valueOf(chat.getString("type"));
        String uid = new OwnerProfile(context).getUid();

        String cidParameterName = "idRoom";
        if (chat.has("id"))
            cidParameterName = "id";
        if (chat.has("idRoom"))
            cidParameterName = "idRoom";
        cid = chat.getString(cidParameterName);

        switch (type) {
            case GROUP_TYPE:
                name = chat.getString("name");
                break;
            case INDIVIDUAL_TYPE:
                JSONArray listUser = chat.getJSONArray("listUser");
                for (int i = 0; i < listUser.length(); i++) {
                    JSONObject user = listUser.getJSONObject(i);
                    String currentUID = user.getString("id");
                    if (!currentUID.equals(uid)) {
                        String firstName = user.getString("firstName");
                        String lastName = user.getString("lastName");
                        name = firstName + " " + lastName;
                        break;
                    }
                }
                if (name == null)
                    throw new JSONException("Inconsistent JSON data");
                membersCount = 2;
                break;
            default:
                throw new JSONException("Incorrect chat type");
        }
        if (chat.has("text")) {
            if (chat.isNull("text")) {
                setDescription(null);
            } else {
                setDescription(StringEscapeUtils.unescapeJava(chat.getString("text")));
            }
        }

        if (chat.has("dtCreate")) {
            setDateTime(chat.getString("dtCreate"));
        }

        if (chat.has("listUser")) {
            JSONArray listUser = chat.getJSONArray("listUser");
            for (int i = 0; i < listUser.length(); i++) {
                JSONObject user = listUser.getJSONObject(i);
                try {
                    Contact contact = new Contact(user, context);
                    chatUsers.add(contact);
                } catch (ParseException e) {
                    Log.e(Chat.class.getSimpleName() + ".new", "New Contact: " + e.getLocalizedMessage());
                }
            }
            membersCount = chatUsers.size();
        }

        String img = null;
        if (chat.has("img")) {
            img = chat.getString("img");
        }
        if (type == INDIVIDUAL_TYPE) {
            Contact user = chatUsers.get(0);
            if (user.getUid().equals(uid))
                user = chatUsers.get(1);
            img = user.getImg();
        }
        if (img != null && !img.equals("null") && !img.equals("false")) {
            try {
                imagePath = new URL(ApiSection.SERVER_URL + img);
            } catch (MalformedURLException e) {
                Log.w(Chat.class.getSimpleName() + ".new", e.getLocalizedMessage());
            }
        }
    }

    public List<Contact> getChatUsers() {
        return chatUsers;
    }

    public void setChatUsers(List<Contact> chatUsers) {
        this.chatUsers = chatUsers;
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

    public void setName(@NonNull String name) {
        this.name = name;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    private void setDescription(@Nullable String description) {
        this.description = description;
    }

    public int getType() {
        return type;
    }

    public int getMembersCount() {
        return membersCount;
    }

    public void setMembersCount(int membersCount) {
        this.membersCount = membersCount;
    }

    @Nullable
    public Calendar getDateTime() {
        return dateTime;
    }

    public void setDateTime(@Nullable Calendar dateTime) {
        this.dateTime = dateTime;
    }

    public void setDateTime(@Nullable String dateTime) {
        if (dateTime != null && !dateTime.equals("null")) {
            try {
                Calendar time = GregorianCalendar.getInstance();
                time.setTime(MessengerDBHelper.currentFormat.parse(dateTime));
                this.dateTime = time;
            } catch (ParseException e) {
                Log.w(Chat.class.getSimpleName() + ".setDateTime", e.getLocalizedMessage());
            }
        } else {
            this.dateTime = null;
        }
    }

    @Nullable
    public URL getImagePath() {
        return imagePath;
    }

    public void setImagePath(@Nullable URL imagePath) {
        this.imagePath = imagePath;
    }

    @NonNull
    public ContentValues getContentValues() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ChatsContract.ChatsEntry.COLUMN_NAME_CID, cid);
        contentValues.put(ChatsContract.ChatsEntry.COLUMN_NAME_NAME, name);
        contentValues.put(ChatsContract.ChatsEntry.COLUMN_NAME_DESCRIPTION, description);
        contentValues.put(ChatsContract.ChatsEntry.COLUMN_NAME_DATETIME, dateTime != null ? dateTime.getTimeInMillis() / 1000L : null);
        contentValues.put(ChatsContract.ChatsEntry.COLUMN_NAME_TYPE, type);
        contentValues.put(ChatsContract.ChatsEntry.COLUMN_NAME_IMAGE_URL, imagePath != null ? imagePath.toString() : null);
        contentValues.put(ChatsContract.ChatsEntry.COLUMN_NAME_MEMBERS_COUNT, membersCount);

        return contentValues;
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof Chat) {
            Chat c = (Chat) o;
            return ObjectUtils.equals(cid, c.cid) &&
                    ObjectUtils.equals(name, c.name) &&
                    ObjectUtils.equals(description, c.description) &&
                    ObjectUtils.equals(dateTime, c.dateTime) &&
                    ObjectUtils.equals(type, c.type) &&
                    ObjectUtils.equals(membersCount, c.membersCount) &&
                    ObjectUtils.equals(imagePath, c.imagePath);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(cid)
                .append(name)
                .append(description)
                .append(dateTime)
                .append(type)
                .append(membersCount)
                .append(imagePath).build();
    }

    public String getCompanionId(Context context) {
        if (type == INDIVIDUAL_TYPE) {
            for (Contact user : getChatUsers()) {
                String ownerUID = new OwnerProfile(context).getUid();
                if (!user.getUid().equals(ownerUID)) {
                    return user.getUid();
                }
            }
        }
        return null;
    }
}
