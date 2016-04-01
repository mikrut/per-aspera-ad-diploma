package ru.mail.park.chat.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

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
    HttpURLConnection httpURLConnection;
    Context context;
    String parameters = null;

    public ServerConnection(Context context, String url) throws IOException {
        this(context, new URL(url));
    }

    public ServerConnection(Context context, URL url) throws IOException {
        this.context = context;
        setUrl(url);
    }

    protected TrustManager getTrustManager() {
        TrustManager tm = new X509TrustManager() {
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };
        return tm;
    }

    public void setUrl(String url) throws IOException {
        setUrl(new URL(url));
    }

    public void setUrl(URL url) throws IOException {
        boolean torStart = OrbotHelper.requestStartTor(context);

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
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public void setRequestMethod(String method) throws ProtocolException {
        httpURLConnection.setRequestMethod(method);
    }

    public String getResponse() {
        StringBuilder responseBuilder = new StringBuilder();

        try {
            // AFAIK everything except GET sends parameters the same way
            Log.w("url", httpURLConnection.getURL().toString());
            if (!httpURLConnection.getRequestMethod().equals("GET")) {
                byte[] postData = parameters.getBytes(Charset.forName("UTF-8"));
                Log.w("post", parameters);

                httpURLConnection.setDoOutput(true);
                httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                httpURLConnection.setRequestProperty("charset", "utf-8");
                httpURLConnection.setRequestProperty("Content-Length", Integer.toString(postData.length));

                OutputStream wr = httpURLConnection.getOutputStream();
                wr.write(postData);
                wr.flush();
                wr.close();
            }

            BufferedReader rd;
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
        } catch (IOException e) {
            e.printStackTrace();
        }
        String reply = responseBuilder.toString();
        Log.v("reply", reply);
        return reply;
    }
}
