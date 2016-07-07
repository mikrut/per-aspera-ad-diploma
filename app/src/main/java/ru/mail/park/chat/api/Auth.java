package ru.mail.park.chat.api;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.mail.park.chat.auth_signup.IRegisterCallbacks;
import ru.mail.park.chat.models.OwnerProfile;

/**
 * Created by 1запуск BeCompact on 29.02.2016.
 */
public class Auth extends ApiSection {
    private static final String URL_ADDITION = "auth";

    @Override
    protected Uri getUrlAddition() {
        return super.getUrlAddition().buildUpon().appendPath(URL_ADDITION).build();
    }

    private String login;
    private String password;
    private String email;

    public Auth(Context context) {
        super(context);
    }

    public class SignUpException extends Exception {

        public SignUpException(String detailMessage, Map<IRegisterCallbacks.ErrorType, String> errorTypeStringMap) {
            super(detailMessage);
            this.errorTypeStringMap = errorTypeStringMap;
        }

        private Map<IRegisterCallbacks.ErrorType, String> errorTypeStringMap;

        public Map<IRegisterCallbacks.ErrorType, String> getErrorTypeStringMap() {
            return errorTypeStringMap;
        }

        public void setErrorTypeStringMap(Map<IRegisterCallbacks.ErrorType, String> errorTypeStringMap) {
            this.errorTypeStringMap = errorTypeStringMap;
        }
    }

    @NonNull
    public OwnerProfile signUp(String login,
                               String firstName,
                               String lastName,
                               String password,
                               String email,
                               String imgPath) throws IOException, SignUpException {
        final String requestURL = "signUp";
        final String requestMethod = "PUT";

        List<Pair<String, String>> parameters = new ArrayList<>(4);
        parameters.add(new Pair<>("login", login));
        parameters.add(new Pair<>("firstName", firstName));
        parameters.add(new Pair<>("lastName", lastName));
        parameters.add(new Pair<>("password", password));
        parameters.add(new Pair<>("email", email));

        if(imgPath != null)
            parameters.add(new Pair<>("img", imgPath));

        OwnerProfile user;
        try {
            JSONObject result = executeSignUpRequest(ApiSection.SERVER_URL + "auth/signUp", parameters);//new JSONObject(executeRequest(requestURL, requestMethod, parameters));
            if (result != null) {
                final int status = result.getInt("status");
                if (status == 200) {
                    JSONObject data = result.getJSONObject("data");
                    user = new OwnerProfile(data);
                } else {
                    String message = result.getString("message");
                    Map<IRegisterCallbacks.ErrorType, String> errorsMap = new HashMap<>();
                    JSONObject errors = result.getJSONObject("errors");
                    for (IRegisterCallbacks.ErrorType errorType : IRegisterCallbacks.ErrorType.values()) {
                        if (errors.has(errorType.toString()))
                            errorsMap.put(errorType, errors.getJSONArray(errorType.toString()).getString(0));
                    }
                    throw new SignUpException(message, errorsMap);
                }
            } else {
                throw new IOException("Server error");
            }
        } catch (RuntimeException | JSONException | ParseException e) {
            throw new IOException("Server error");
        }

        return user;
    }

    @Nullable
    private JSONObject executeSignUpRequest(String uploadPath, List<Pair<String, String>> parameters) {
        URL connectURL = null;
        try {
            connectURL = new URL(uploadPath);
        } catch(MalformedURLException e) {
            try {
                JSONObject errorReport = new JSONObject();
                errorReport.put("status", 400);
                errorReport.put("message", "MalformedURLException");
                errorReport.put("errors", new JSONObject().put("IMG", "MalformedURLException"));

                return errorReport;
            } catch (JSONException ex) {

            }
        }

        FileInputStream fileInputStream = null;

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
            try {
                return new JSONObject(b.toString());
            } catch(JSONException e) {
                return null;
            }
        } catch (IOException e) {
            Log.d("[TP-diploma]", "MultipartProfileUpdater exception: " + e.getMessage());
            return null;
        }
    }

    @NonNull
    public OwnerProfile signIn(String login, String password) throws IOException {
        final String requestURL = "signIn";
        final String requestMethod = "POST";

        List<Pair<String, String>> parameters = new ArrayList<>(3);
        parameters.add(new Pair<>("login", login));
        parameters.add(new Pair<>("password", password));

        OwnerProfile user;
        try {
            JSONObject result = new JSONObject(executeRequest(requestURL, requestMethod, parameters));
            Log.d("[TP-diploma]", "Login task: " + result.toString());
            final int status = result.getInt("status");
            if(status == 200) {
                JSONObject data = result.getJSONObject("data");
                user = new OwnerProfile(data);
            } else {
                String message = result.getString("message");
                throw new IOException(message);
            }
        } catch (JSONException | ParseException e) {
            e.printStackTrace();
            throw new IOException("Server error");
        }

        return user;
    }

    public void logOut(String accessToken) throws IOException {
        final String requestURL = "logOut";
        final String requestMethod = "POST";

        List<Pair<String, String>> parameters = new ArrayList<>(1);
        parameters.add(new Pair<>(AUTH_TOKEN_PARAMETER_NAME, accessToken));

        try {
            JSONObject result = new JSONObject(executeRequest(requestURL, requestMethod, parameters, false));
            final int status = result.getInt("status");
            if(status != 200) {
                String message = result.getString("message");
                throw new IOException(message);
            }
        } catch (JSONException e) {
            throw new IOException("Server error");
        }
    }

/*    public void showActiveSessions() {

    }

    public void closeSession() {

    }*/

    public boolean isLogged() {
        return getAuthToken() != null;
    }
}
