package ru.mail.park.chat;

import android.support.annotation.NonNull;

import junit.framework.TestCase;

import org.junit.Test;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import ru.mail.park.chat.api.Auth;
import ru.mail.park.chat.security.AuthKeyCreator;
import ru.mail.park.chat.security.Nonce;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Created by Михаил on 08.04.2016.
 */
public class ClientSideCryptoAuthTest extends TestCase {

    static private RSAPublicKey rsaPublic = null;
    static private RSAPrivateKey rsaPrivate = null;
    static {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair rsaKeyPair = generator.generateKeyPair();
            rsaPublic = (RSAPublicKey) rsaKeyPair.getPublic();
            rsaPrivate = (RSAPrivateKey) rsaKeyPair.getPrivate();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
    
    AuthKeyCreator.ServerProxy dummyServerProxy = new DummyProxy();

    static class DummyProxy implements AuthKeyCreator.ServerProxy {
        final static private Nonce serverNonce = new Nonce();
        final static private long pq = Long.valueOf("17ED48941A08F981", 16);

        @NonNull
        @Override
        public AuthKeyCreator.ServerProxy.ResPQ requestPQ(Nonce nonce, long messageID) {
            ResPQ res = new ResPQ();
            res.serverNonce = serverNonce;
            res.pq = pq;
            res.fingerprints = new long[]{rsaPublic.getPublicExponent().longValue()};
            return res;
        }

        @NonNull
        @Override
        public byte[] requestDHParams(long messageID, Nonce nonce, Nonce serverNonce, long p, long q, long publicKeyFingerprint, byte[] encryptedData) {
            assertNotEquals(1, p);
            assertNotEquals(1, q);
            assertEquals(pq, p * q);
            assertEquals(rsaPublic.getPublicExponent().longValue(), publicKeyFingerprint);

            Cipher cipher = null;
            try {
                cipher = Cipher.getInstance("RSA");
                cipher.init(Cipher.DECRYPT_MODE, rsaPrivate);
                byte[] decryptedData = cipher.doFinal(encryptedData);

            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            } catch (BadPaddingException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public boolean setClientDHParams(Nonce nonce, Nonce serverNonce, byte[] encryptedData) {
            return false;
        }
    };

    @Test
    public void testCrypto() throws NoSuchPaddingException {
        AuthKeyCreator trueCreator = new AuthKeyCreator(dummyServerProxy);
        trueCreator.keys = new RSAPublicKey[]{rsaPublic};

        byte[] authKey = trueCreator.createAuthKey();
        assertNotNull(authKey);
    }
}
