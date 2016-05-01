package ru.mail.park.chat.api;

/**
 * Created by 1запуск BeCompact on 07.04.2016.
 */
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.os.AsyncTask;
import android.util.Log;
import android.webkit.MimeTypeMap;

import org.json.JSONException;
import org.json.JSONObject;

import ru.mail.park.chat.models.AttachedFile;

public class HttpFileUpload implements Runnable{
    URL connectURL;
    String responseString;
    String Title;
    String accessToken;
    byte[ ] dataToServer;
    FileInputStream fileInputStream = null;

    public HttpFileUpload(String urlString, String vTitle, String accessToken){
        try{
            connectURL = new URL(urlString);
            Title= vTitle.substring(vTitle.lastIndexOf('/') + 1);
            this.accessToken = accessToken;
        }catch(Exception ex){
            Log.i("HttpFileUpload","URL Malformatted");
        }
    }

    public void Send_Now(FileInputStream fStream, IUploadListener listener){
        fileInputStream = fStream;
        Log.d("[TP-diploma]", "send now");
        Sending(listener);
    }

    void Sending(IUploadListener listener){
        Log.d("[TP-diploma]", "sending started");
        new HttpUploadTask(listener).execute();
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
    }

    public interface IUploadListener {
        void onUploadComplete(AttachedFile file);
    }

    class HttpUploadTask extends AsyncTask<Void,Void,String>
    {
        IUploadListener listener;

        public HttpUploadTask(IUploadListener listener) {
            this.listener = listener;
        }

        protected void onPreExecute() {
            //display progress dialog.
        }

        protected String doInBackground(Void... params) {
            try {
                String iFileName = Title;
                String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(iFileName));
                String lineEnd = "\r\n";
                String twoHyphens = "--";
                String boundary = "===" + System.currentTimeMillis() + "===";
                String Tag = "[TP-diploma]";

                // Open a HTTP connection to the URL
                HttpURLConnection conn = (HttpURLConnection)connectURL.openConnection();
                Log.d("[TP-diploma]", "connection opened");

                // Allow Inputs
                conn.setDoInput(true);

                // Allow Outputs
                conn.setDoOutput(true);
                Log.d("[TP-diploma]", "IO has been set");

                // Don't use a cached copy.
                conn.setUseCaches(false);

                // Use a post method.
                conn.setRequestMethod("POST");

                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("Cache-Control", "no-cache");

                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                Log.d("[TP-diploma]", "POST settings set");

                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

                Log.d("[TP-diploma]", "outputstream created");

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"accessToken\"" + lineEnd);
                dos.writeBytes("Content-Type: text/plain; charset=UTF-8" + lineEnd);
                dos.writeBytes(lineEnd);
                dos.writeBytes(accessToken);
                dos.writeBytes(lineEnd);

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"" + iFileName +"\"" + lineEnd);
                dos.writeBytes("Content-Type: " + mime + lineEnd);
                Log.v("filename", iFileName);
                dos.writeBytes(lineEnd);

                Log.e(Tag, "Headers are written");
                Log.d("[TP-diploma]", "Headers are written");

                // create a buffer of maximum size
                int bytesAvailable = fileInputStream.available();

                int maxBufferSize = 1024;
                int bufferSize = Math.min(bytesAvailable, maxBufferSize);
                byte[ ] buffer = new byte[bufferSize];

                // read file and write it into form...
                int bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0)
                {
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable,maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0,bufferSize);
                }
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                Log.d("[TP-diploma]", "data is written");
                // close streams
                fileInputStream.close();

                Log.d("[TP-diploma]", "fileInputStream closed");

                dos.flush();

                Log.e("[TP-diploma]","File Sent, Response: "+String.valueOf(conn.getResponseCode()));

                InputStream is = conn.getInputStream();

                // retrieve the response from server
                int ch;

                StringBuffer b =new StringBuffer();
                while( ( ch = is.read() ) != -1 ){ b.append( (char)ch ); }
                String s=b.toString();

                dos.close();
                Log.d("[TP-diploma]", s);
                return s;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        protected void onPostExecute(String result) {
            if (result != null && listener != null) {
                try {
                    JSONObject object = new JSONObject(result);
                    AttachedFile attachedFile = new AttachedFile(object.getJSONObject("data"));
                    listener.onUploadComplete(attachedFile);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}