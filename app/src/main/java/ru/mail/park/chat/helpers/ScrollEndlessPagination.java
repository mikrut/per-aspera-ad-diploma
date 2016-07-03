package ru.mail.park.chat.helpers;

import android.app.LoaderManager;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.util.List;

/**
 * Created by Михаил on 06.06.2016.
 */
public class ScrollEndlessPagination<T> extends RecyclerView.OnScrollListener {
    private static final String TAG = ScrollEndlessPagination.class.getSimpleName();

    private static final int PAGE_SIZE = 20;
    public static final String ARG_PAGE = ScrollEndlessPagination.class.getCanonicalName() + ".ARG_PAGE";

    private final LinearLayoutManager liman;
    private final EndlessLoaderListener<T> loader;
    private final int loaderID;
    private final LoaderManager manager;
    private int pageSize = PAGE_SIZE;

    public ScrollEndlessPagination(LinearLayoutManager liman, EndlessLoaderListener<T> loader,
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
        int lastVisible = liman.getStackFromEnd() ?
                liman.getItemCount() - liman.findFirstVisibleItemPosition() - 1 :
                liman.findLastVisibleItemPosition();
        Log.d(TAG + ".onScrolled", "Last visible: " + String.valueOf(lastVisible));
        Log.d(TAG + ".onScrolled", "Item count: " + String.valueOf(liman.getItemCount()));
        if (!loader.isEndReached() &&
                lastVisible == liman.getItemCount() - 1) {
            Log.d(TAG + ".onScrolled", "Loading...");
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
