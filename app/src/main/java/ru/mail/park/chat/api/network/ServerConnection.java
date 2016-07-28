package ru.mail.park.chat.api.network;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Pair;
import android.webkit.MimeTypeMap;

import org.apache.commons.io.FilenameUtils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import info.guardianproject.netcipher.NetCipher;
import info.guardianproject.netcipher.proxy.OrbotHelper;
import ru.mail.park.chat.database.PreferenceConstants;

/**
 * Created by 1запуск BeCompact on 29.02.2016.
 */

// FIXME: don't trust everyone!
// TODO: check security
public class ServerConnection {
    private final Context context;

    private String requestURL;
    private String method = "GET";
    @Nullable
    private List<Pair<String, Object>> parameters = null;

    public ServerConnection(Context context, String url) throws IOException {
        this.context = context;
        this.requestURL = url;
    }

    private TrustManager getTrustManager() {
        return new X509TrustManager() {
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };
    }

    private HttpURLConnection getUrl() throws IOException {
        String paramsString = "";
        if (method != null && method.equals("GET")) {
            paramsString = buildParameterString(parameters);
        }

        return getUrl(new URL(requestURL + "?" + paramsString));
    }

    @NonNull
    private HttpURLConnection getUrl(URL url) throws IOException {
        // TODO: check tor status properly
        boolean torStart = OrbotHelper.requestStartTor(context);
        HttpURLConnection httpURLConnection = null;

        try {
            if (torStart) {
                NetCipher.setProxy(NetCipher.ORBOT_HTTP_PROXY);
                if (url.getProtocol().equals("https")) {
                    httpURLConnection = NetCipher.getHttpsURLConnection(url);
                } else {
                    httpURLConnection = NetCipher.getHttpURLConnection(url);
                }
            } else {
                boolean onlyTorIsAllowed = PreferenceManager
                        .getDefaultSharedPreferences(context)
                        .getBoolean(PreferenceConstants.SECURITY_PARANOID_N, true);

                if (!onlyTorIsAllowed) {
                    httpURLConnection = (HttpURLConnection) url.openConnection();
                } else {
                    throw new IOException("Cannot establish TOR connection");
                }
            }

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{getTrustManager()}, null);

            if (httpURLConnection instanceof HttpsURLConnection) {
                ((HttpsURLConnection) httpURLConnection).setSSLSocketFactory(sslContext.getSocketFactory());
            }

            httpURLConnection.setRequestMethod(method);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }

        return httpURLConnection;
    }

    /**
     * @param parameters pairs {parameterName : parameter} to send in HTTP request.
     *                   Can be both usual objects (Strings, Integers etc.) and Files.
     *                   If a request contains a File parameter it automatically becomes multipart.
     */
    public void setParameters(@Nullable List<Pair<String, Object>> parameters) {
        this.parameters = parameters;
    }

    /**
     * @param method an HTTP request method to use (GET, POST, DELETE, UPDATE etc.)
     */
    public void setRequestMethod(String method) {
        this.method = method;
    }

    private static boolean isMultipart(List<Pair<String, Object>> parameters) {
        boolean haveFile = false;
        if (parameters != null) {
            for (int i = 0; i < parameters.size() && !haveFile; i++) {
                haveFile = parameters.get(i).second instanceof File;
            }
        }
        return haveFile;
    }

    /**
     * @return An HTTP response for the specified request.
     * Prior calling to this method you should set request method, parameters and destination URL.
     */
    public String getResponse() {
        final boolean multipart = isMultipart(parameters);
        if (!multipart) {
            return getSimpleResponse();
        } else {
            return getMultipartResponse();
        }
    }

    /**
     *
     * @return An HTTP response for our simple (non-multipart) request.
     */
    private String getSimpleResponse() {
        final String tag = ServerConnection.class.getSimpleName() + ".getResponse";

        String reply = "";
        try {
            // AFAIK everything except GET sends parameters the same way
            HttpURLConnection httpURLConnection = getUrl();
            Log.w(tag + "_url", httpURLConnection.getURL().toString());

            httpURLConnection.setInstanceFollowRedirects(true);
            httpURLConnection.setConnectTimeout(10000);
            httpURLConnection.setReadTimeout(10000);
            final String USER_AGENT = "Mozilla/5.0 (Linux; U; Android 1.6; en-us; GenericAndroidDevice) AppleWebKit/528.5+ (KHTML, like Gecko) Version/3.1.2 Mobile Safari/525.20.1";
            httpURLConnection.setRequestProperty("User-Agent", USER_AGENT);

            if (!httpURLConnection.getRequestMethod().equals("GET")) {
                final String postParameters = buildParameterString(parameters);

                Log.d(tag, "parameters in request: " + postParameters);
                byte[] postData = postParameters.getBytes(Charset.forName("UTF-8"));
                Log.w(tag, postParameters);

                httpURLConnection.setDoOutput(true);
                httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                httpURLConnection.setRequestProperty("charset", "utf-8");
                httpURLConnection.setRequestProperty("Content-Length", Integer.toString(postData.length));

                Log.d(tag, "going to write");
                OutputStream wr = httpURLConnection.getOutputStream();
                wr.write(postData);
                wr.flush();
                wr.close();
            }

            reply = getServerResponse(httpURLConnection);
        } catch (IOException e) {
            Log.v(ServerConnection.class.getSimpleName() + ".getResponse", "Exception: " + e.getMessage());
            e.printStackTrace();
        }

        Log.v(ServerConnection.class.getSimpleName() + ".getResponse", "Reply: " + reply);
        return reply;
    }
    
    private static String getServerResponse(HttpURLConnection httpURLConnection) throws IOException {
        StringBuilder responseBuilder = new StringBuilder();
        BufferedReader rd;
        Log.d(ServerConnection.class.getSimpleName() + ".getResponse", "Result code: " + Integer.toString(httpURLConnection.getResponseCode()));
        if (httpURLConnection.getResponseCode() == 200) {
            rd = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
        } else {
            rd = new BufferedReader(new InputStreamReader(httpURLConnection.getErrorStream()));
        }
        String line;
        while ((line = rd.readLine()) != null) {
            responseBuilder.append(line);
        }
        rd.close();
        
        return responseBuilder.toString();
    }

    /**
     *
     * @return A server response for multipart request.
     */
    private String getMultipartResponse() {
        final String tag = ServerConnection.class.getSimpleName() + ".getMultipartResponse";
        String response = "";
        try {
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "===" + System.currentTimeMillis() + "===";

            Log.v(tag, "Multipart for: " + requestURL);
            HttpURLConnection conn = getUrl(); // Open a HTTP connection to the URL
            Log.v(tag, "Opened a connection");

            conn.setDoInput(true);          // Allow Inputs
            conn.setDoOutput(true);         // Allow Outputs
            conn.setUseCaches(false);       // Don't use a cached copy.
            conn.setRequestMethod("POST");  // Use a post method.

            conn.setRequestProperty("Connection",       "Keep-Alive");
            conn.setRequestProperty("Cache-Control",    "no-cache");
            conn.setRequestProperty("Content-Type",     "multipart/form-data;boundary=" + boundary);

            DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

            Log.v(tag, "Created a data output stream");

            for (Pair<String, Object> parameter : parameters) {
                if (parameter.second == null || !(parameter.second instanceof File)) {
                    write(boundary, dos, parameter.first, parameter.second);
                } else {
                    write(boundary, dos, parameter.first, (File) parameter.second);
                }
            }
            
            // retrieve the response from server
            response = getServerResponse(conn);
        } catch (IOException e) {
            Log.e(tag, "Exception: " + e.getLocalizedMessage());
        }
        
        Log.v(tag, "Response: " + response);
        return response;
    }
    
    private static void write(String boundary, DataOutputStream dos, String key, Object value)
            throws IOException {
        final String tag = ServerConnection.class.getSimpleName() + ".getMultipartResponse";

        final String lineEnd = "\r\n";
        final String twoHyphens = "--";
        
        Log.v(tag, key + "=" + value);
        dos.writeBytes(twoHyphens + boundary + lineEnd);
        dos.writeBytes("Content-Disposition: form-data; name=\"" + key + "\""+ lineEnd);
        dos.writeBytes("Content-Type: text/plain; charset=UTF-8" + lineEnd);
        dos.writeBytes(lineEnd);
        dos.writeBytes(String.valueOf(value));
        dos.writeBytes(lineEnd);
    }

    private static void write(String boundary, DataOutputStream dos, String key, File file) throws IOException {
        final String tag = ServerConnection.class.getSimpleName() + ".getMultipartResponse";

        final String lineEnd = "\r\n";
        final String twoHyphens = "--";

        Log.v(tag, "Sending file: " + file.getName());
        final String extension = FilenameUtils.getExtension(file.getName());
        final String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);

        dos.writeBytes(twoHyphens + boundary + lineEnd);
        dos.writeBytes("Content-Disposition: form-data; name=\"" + key + "\"; filename=\"" + file.getName() + "\"" + lineEnd);
        dos.writeBytes("Content-Type: " + mime + lineEnd);
        dos.writeBytes(lineEnd);

        // create a buffer of maximum size
        FileInputStream fileInputStream = new FileInputStream(file);
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
        Log.v(tag, "file is written");

        fileInputStream.close();    // close streams
        Log.v(tag, "fileInputStream closed\n\n");

        dos.flush();
    }

    private static String buildParameterString(List<Pair<String, Object>> parameters) {
        final StringBuilder parametersBuilder = new StringBuilder();
        boolean first = true;
        for (Pair<String, Object> parameter : parameters) {
            if (first) {
                first = false;
            } else {
                parametersBuilder.append('&');
            }

            try {
                parametersBuilder.append(URLEncoder.encode(String.valueOf(parameter.first), "UTF-8"));
                parametersBuilder.append("=");
                parametersBuilder.append(URLEncoder.encode(String.valueOf(parameter.second), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        return parametersBuilder.toString();
    }
}
