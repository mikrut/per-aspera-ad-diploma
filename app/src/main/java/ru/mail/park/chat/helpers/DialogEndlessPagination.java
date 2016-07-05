package ru.mail.park.chat.helpers;

import android.app.LoaderManager;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.util.Collection;
import java.util.List;

import ru.mail.park.chat.models.Message;

/**
 * Created by mikrut on 05.07.16.
 */
public class DialogEndlessPagination extends ScrollEndlessPagination<Message> {
    public static final String ARG_INDEX = DialogEndlessPagination.class.getCanonicalName() + ".ARG_INDEX";
    List<Message> data;

    public DialogEndlessPagination(LinearLayoutManager liman, EndlessLoaderListener<Message> loader, int loaderID, LoaderManager manager, List<Message> data) {
        super(liman, loader, loaderID, manager);
        this.data = data;
    }

    @Override
    protected void loadMore() {
        Log.d(DialogEndlessPagination.class.getSimpleName() + ".onScrolled", "Loading...");
        String lastID = data.size() > 0 ? data.get(0).getMid() : null;
        Bundle args = getLoader().getBundle();
        args.putString(ARG_INDEX, lastID);
        getManager().restartLoader(getLoaderID(), args, getLoader()).forceLoad();
    }
}
