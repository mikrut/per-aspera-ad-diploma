package ru.mail.park.chat.api;

import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Created by 1запуск BeCompact on 24.04.2016.
 */
public class MultipartProfileUpdater {
    URL connectURL;
    FileInputStream fileInputStream = null;
    List<Pair<String, String>> parameters;

    public MultipartProfileUpdater(String urlString, List<Pair<String, String>> parameters){
        try {
            this.parameters = parameters;
            connectURL = new URL(urlString);
        } catch(Exception ex) {
            Log.i("[TP-diploma]", "URL Malformatted");
        }
    }

    public boolean Send_Now(IUploadListener listener){
        return Sending(listener);
    }

    boolean Sending(IUploadListener listener){
        Boolean result = true;

        Log.d("[TP-diploma]", "sending started");
        HttpMultipartUpdateProfileTask hmupTask = new HttpMultipartUpdateProfileTask(listener);
        hmupTask.execute();

        /*try {
            result = hmupTask.get();
        } catch(Exception e) {
            return false;
        }*/

        return result;//result != null;
    }


    public void run() {
        // TODO Auto-generated method stub
    }

    public interface IUploadListener {
        void onUploadComplete(String name);
    }

    class HttpMultipartUpdateProfileTask extends AsyncTask<Void,Void,Boolean>
    {
        IUploadListener listener;

        public HttpMultipartUpdateProfileTask(IUploadListener listener) {
            this.listener = listener;
        }

        protected void onPreExecute() {
            //display progress dialog.
        }

        protected Boolean doInBackground(Void... params) {
            try {
                String lineEnd = "\r\n";
                String twoHyphens = "--";
                String boundary = "===" + System.currentTimeMillis() + "===";
                String Tag = "[TP-diploma]";

                Log.d(Tag, "doInBackground started");

                // Open a HTTP connection to the URL
                HttpURLConnection conn = (HttpURLConnection)connectURL.openConnection();
                Log.d(Tag, "connection opened");

                // Allow Inputs
                conn.setDoInput(true);

                // Allow Outputs
                conn.setDoOutput(true);

                // Don't use a cached copy.
                conn.setUseCaches(false);

                // Use a post method.
                conn.setRequestMethod("POST");

                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("Cache-Control", "no-cache");

                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

                Log.d("[TP-diploma]", "outputstream created");

                for(int i = 0; i < parameters.size(); i++) {
                    Pair<String, String> p = parameters.get(i);
                    Log.d("[TP-diploma]", p.first);

                    if(!p.first.equals("img") && p.second != null) {
                        Log.d("[TP-diploma]", "printing " + p.first + " = " + p.second);
                        dos.writeBytes(twoHyphens + boundary + lineEnd);
                        dos.writeBytes("Content-Disposition: form-data; name=\"" + p.first + "\""+ lineEnd);
                        dos.writeBytes("Content-Type: text/plain; charset=UTF-8" + lineEnd);
                        dos.writeBytes(lineEnd);
                        dos.writeBytes(p.second);
                        dos.writeBytes(lineEnd);
                    } else if (p.second != null) {
                        try {
                            fileInputStream = new FileInputStream(p.second);

                            Log.d("[TP-diploma]", "printing img = " + p.second);
                            String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(p.second));

                            dos.writeBytes(twoHyphens + boundary + lineEnd);

                            dos.writeBytes("Content-Disposition: form-data; name=\"img\"; filename=\"" + p.second.substring(p.second.lastIndexOf('/'), p.second.length()) + "\"" + lineEnd);
                            dos.writeBytes("Content-Type: " + mime + lineEnd);
                            dos.writeBytes(lineEnd);

                            // create a buffer of maximum size
                            int bytesAvailable = fileInputStream.available();

                            int maxBufferSize = 1024;
                            int bufferSize = Math.min(bytesAvailable, maxBufferSize);
                            byte[] buffer = new byte[bufferSize];

                            // read file and write it into form...
                            int bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                            while (bytesRead > 0) {
                                dos.write(buffer, 0, bufferSize);
                                bytesAvailable = fileInputStream.available();
                                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                            }
                            dos.writeBytes(lineEnd);
                            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                            Log.d("[TP-diploma]", "data is written");
                            // close streams
                            fileInputStream.close();

                            Log.d("[TP-diploma]", "fileInputStream closed\n\n");

                            Log.d(Tag, dos.toString());

                            Log.d(Tag, "\n\n");

                            dos.flush();
                        } catch (FileNotFoundException e) {
                            Log.e(MultipartProfileUpdater.class.getSimpleName() + ".doInBackground", e.getLocalizedMessage());
                        }
                    }
                }

                Log.d("[TP-diploma]","Form Sent, Response: "+String.valueOf(conn.getResponseCode()));

                InputStream is = conn.getInputStream();

                // retrieve the response from server
                int ch;

                StringBuffer b = new StringBuffer();
                while( ( ch = is.read() ) != -1 ){ b.append( (char)ch ); }

                dos.close();
                return conn.getResponseCode() == 200;
            } catch (IOException e) {
                Log.d("[TP-diploma]", "MultipartProfileUpdater exception: " + e.getMessage());
                return false;
            }
        }

        protected void onPostExecute(Boolean result) {
            if (result && listener != null) {
                try {
                    listener.onUploadComplete(String.valueOf(result));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}