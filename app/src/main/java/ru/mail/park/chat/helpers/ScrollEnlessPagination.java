package ru.mail.park.chat.helpers;

import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import ru.mail.park.chat.activities.ChatsActivity;
import ru.mail.park.chat.activities.adapters.ChatsAdapter;
import ru.mail.park.chat.loaders.ChatWebLoader;

/**
 * Created by Михаил on 06.06.2016.
 */
public class ScrollEnlessPagination <T> extends RecyclerView.OnScrollListener {
    private static final int PAGE_SIZE = 20;
    public static final String ARG_PAGE = ScrollEnlessPagination.class.getCanonicalName() + ".ARG_PAGE";

    private final LinearLayoutManager liman;
    private final EndlessLoaderListener<T> loader;
    private final int loaderID;
    private final LoaderManager manager;
    private int pageSize = PAGE_SIZE;

    public ScrollEnlessPagination(LinearLayoutManager liman, EndlessLoaderListener<T> loader,
                                  int loaderID, LoaderManager manager) {
        this.liman = liman;
        this.loader = loader;
        this.loaderID = loaderID;
        this.manager = manager;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPageSize() {
        return pageSize;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        // we try to download more pages
        int lastVisible = liman.getStackFromEnd() ? (liman.getItemCount() - liman.findFirstVisibleItemPosition() - 1) : liman.findLastVisibleItemPosition();
        Log.d("lavi", String.valueOf(lastVisible));
        Log.d("itemcount", String.valueOf(liman.getItemCount()));
        if (!loader.isEndReached() &&
                lastVisible == liman.getItemCount() - 1) {
            int nextPage = (liman.getItemCount() / pageSize) + 1;
            Bundle args = loader.getBundle();
            args.putInt(ARG_PAGE, nextPage);
            manager.restartLoader(loaderID, args, loader).forceLoad();
        }
    }

    public interface EndlessLoaderListener <T> extends LoaderManager.LoaderCallbacks<List<T>> {
        boolean isEndReached();
        Bundle getBundle();
    }
}
