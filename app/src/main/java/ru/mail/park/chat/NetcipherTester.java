package ru.mail.park.chat;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import info.guardianproject.netcipher.NetCipher;
import info.guardianproject.netcipher.proxy.OrbotHelper;

/**
 * Created by Михаил on 26.02.2016.
 */
public class NetcipherTester {

    // FIXME: do not trust everyone!
    public static String testNetcipher(Context context) {
        boolean torStart = OrbotHelper.requestStartTor(context);
        final String TEST_URL = "https://google.com";
        String result = null;

        try {
            HttpsURLConnection httpsURLConnection;
            if (torStart) {
                Log.v(NetcipherTester.class.getCanonicalName(), "Tor started");
                NetCipher.setProxy(NetCipher.ORBOT_HTTP_PROXY);
                httpsURLConnection = NetCipher.getHttpsURLConnection(TEST_URL);
            } else {
                Log.v(NetcipherTester.class.getCanonicalName(), "No Onion started");
                URL url = new URL(TEST_URL);
                httpsURLConnection = (HttpsURLConnection) url.openConnection();
            }

            // KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            // String algorithm = TrustManagerFactory.getDefaultAlgorithm();
            // TrustManagerFactory tmf = TrustManagerFactory.getInstance(algorithm);
            // tmf.init(keyStore);
            TrustManager tm = new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };

            SSLContext sslContext = SSLContext.getInstance("TLS");
            // sslContext.init(null, tmf.getTrustManagers(), null);
            sslContext.init(null, new TrustManager[] { tm }, null);

            httpsURLConnection.setSSLSocketFactory(sslContext.getSocketFactory());
            result = getMessageBody(httpsURLConnection);
        } catch (IOException | NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static String getMessageBody(HttpURLConnection connection) {
        StringBuilder responseBuilder = new StringBuilder();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                responseBuilder.append(line);
            }
            rd.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return responseBuilder.toString();
    }
}
