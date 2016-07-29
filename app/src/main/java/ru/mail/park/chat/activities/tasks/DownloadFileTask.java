package ru.mail.park.chat.activities.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.renderscript.ScriptGroup;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Pair;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.CountingOutputStream;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import ru.mail.park.chat.activities.adapters.MenuAdapter;
import ru.mail.park.chat.api.network.ServerConnection;
import ru.mail.park.chat.database.AttachmentsHelper;
import ru.mail.park.chat.models.AttachedFile;

/**
 * Created by Михаил on 29.07.2016.
 */
public class DownloadFileTask extends AsyncTask<URL, DownloadFileTask.DownloadProgress, Boolean> {
    private final Context context;
    private final File outputFile;
    private final AttachedFile attachedFile;

    @Nullable
    private IDownloadProgressListener downloadProgressListener;

    public DownloadFileTask(@NonNull Context context, @NonNull  File outputFile,
                            @NonNull AttachedFile attachedFile) {
        this.context = context;
        this.outputFile = outputFile;
        this.attachedFile = attachedFile;
    }

    public void setDownloadProgressListener(@Nullable IDownloadProgressListener downloadProgressListener) {
        this.downloadProgressListener = downloadProgressListener;
    }

    public class DownloadProgress {
        public final long totalWritten;
        public final long totalFileSize;

        public DownloadProgress(long totalWritten, long totalFileSize) {
            this.totalWritten = totalWritten;
            this.totalFileSize = totalFileSize;
        }
    }

    private class DownloadOutputStream extends CountingOutputStream {
        private long totalWritten = 0;
        private final long totalFileSize;

        public DownloadOutputStream(OutputStream out, long fileSize) {
            super(out);
            totalFileSize = fileSize;
        }

        @Override
        protected void afterWrite(int n) throws IOException {
            super.afterWrite(n);
            totalWritten += n;
            publishProgress(new DownloadProgress(totalWritten, totalFileSize));
        }
    }

    @Override
    protected Boolean doInBackground(URL... params) {
        final URL url = params[0];
        boolean success = false;
        try {
            final ServerConnection connection = new ServerConnection(context, url.toString());
            final HttpURLConnection httpURLConnection = connection.getConnection();

            if (httpURLConnection != null) {
                Log.d(DownloadFileTask.class.getSimpleName() + ".getResponse", "Result code: " + Integer.toString(httpURLConnection.getResponseCode()));

                final InputStream inputStream = ServerConnection.getResponseStream(httpURLConnection);
                if (inputStream != null) {
                    // final OutputStream outputStream = new DownloadOutputStream(new FileOutputStream(outputFile), httpURLConnection.getContentLengthLong());
                    final long contentLength = Long.valueOf(httpURLConnection.getHeaderField("content-Length"));
                    final OutputStream outputStream = new DownloadOutputStream(new FileOutputStream(outputFile), contentLength);
                    IOUtils.copy(inputStream, outputStream);

                    inputStream.close();
                    outputStream.close();
                    success = true;
                }
                httpURLConnection.disconnect();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return success;
    }

    @Override
    protected void onProgressUpdate(DownloadProgress... values) {
        if (downloadProgressListener != null) {
            downloadProgressListener.onProgressUpdate(values[0]);
        }
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if (success) {
            attachedFile.setDownloaded(true);
            attachedFile.setFileSystemPath(outputFile.getPath());

            AttachmentsHelper helper = new AttachmentsHelper(context);
            helper.saveAttachment(attachedFile);
            helper.close();
        }

        if (downloadProgressListener != null) {
            downloadProgressListener.onPostExecute(success);
        }
    }

    public interface IDownloadProgressListener {
        void onProgressUpdate(@NonNull DownloadProgress progress);
        void onPostExecute(boolean success);
    }
}
