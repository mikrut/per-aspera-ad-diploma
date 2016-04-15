package ru.mail.park.chat.security;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyAgreement;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.DHPublicKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Михаил on 02.04.2016.
 */
public class AuthKeyCreator {
    public interface ServerProxy {
        enum Method {
            REQ_PQ,
            RES_PQ,
            P_Q_INNER_DATA,
            REQ_DH_PARAMS,
            SERVER_DH_PARAMS_FAIL,
            SERVER_DH_PARAMS_OK,
            SERVER_DH_INNER_DATA,
            CLIENT_DH_INNER_DATA,
            SET_CLIENT_DH_PARAMS,
            DH_GEN_OK,
            DH_GEN_RETRY,
            DH_GEN_FAIL
        }

        // Response to PQ request
        class ResPQ {
            public Nonce serverNonce;
            public long pq; // pq = p * q, where p and q are prime numbers
            public long[] fingerprints; // array of last 64-bits of public keys
        }

        @NonNull
        ResPQ requestPQ(Nonce nonce, long messageID);

        @NonNull
        byte[] requestDHParams(long messageID, Nonce nonce, Nonce serverNonce,
                               long p, long q,
                               long publicKeyFingerprint, byte[] encryptedData);

        boolean setClientDHParams(Nonce nonce, Nonce serverNonce, byte[] encryptedData);
    }

    @NonNull
    byte[] getSerializedPQClientResponse(int p, int q, Nonce nonce, Nonce serverNonce, Nonce newNonce) {
        return ByteBuffer.allocate(4 * 2 + Nonce.LENGTH * 3)
                .putInt(p)
                .putInt(q)
                .put(nonce.getNonce())
                .put(serverNonce.getNonce())
                .put(newNonce.getNonce())
                .array();
    }

    private ServerProxy serverProxy;

    class ServerDHParams {
        Nonce nonce;
        Nonce serverNonce;
        BigInteger g;
        BigInteger gA;
        BigInteger dhPrime;

        public ServerDHParams(byte[] decryptedAnswer,
                              Nonce nonce,
                              Nonce serverNonce) {
            ByteBuffer buffer = ByteBuffer.wrap(decryptedAnswer);
            byte[] nonceB = new byte[Nonce.LENGTH];
            buffer.get(nonceB, 0, nonceB.length);
            this.nonce = new Nonce(nonceB);

            buffer.get(nonceB, 0, nonceB.length);
            this.serverNonce = new Nonce(nonceB);

            byte[] gB = new byte[4];
            buffer.get(gB, 0, gB.length);
            g = new BigInteger(gB);

            byte[] dhPrimeB = new byte[260];
            buffer.get(dhPrimeB, 0, dhPrimeB.length);
            dhPrime = new BigInteger(dhPrimeB);

            byte[] gAB = new byte[260];
            buffer.get(gAB, 0, gAB.length);
            gA = new BigInteger(gAB);

            check(nonce, serverNonce);
        }

        public void check(Nonce nonce, Nonce serverNonce) throws SecurityException {
            // TODO: check security of incoming parameters
            if (this.nonce.equals(nonce) && this.serverNonce.equals(serverNonce)){}
            else throw new SecurityException("Nonces don't match!");
        }
    }

    public AuthKeyCreator(ServerProxy proxy) {
        serverProxy = proxy;
    }

    public byte[] createAuthKey() throws NoSuchPaddingException {
        // Step 1: Request for authorization
        Nonce nonce = new Nonce();
        long messageID = generateMessageID();

        // Step 2: getting server response and RSA public key
        ServerProxy.ResPQ resPQ = serverProxy.requestPQ(nonce, messageID);
        RSAPublicKey publicKey = getRSAPublicKey(resPQ.fingerprints);

        // Step 3: decomposing pq into 2 prime cofactors
        Pair<Integer, Integer> pq = decomposePQ(resPQ.pq);

        // Step 4: encrypting pq response data
        Nonce newNonce = new Nonce();
        byte[] serializedResponse =
                getSerializedPQClientResponse(pq.first, pq.second,
                        nonce, resPQ.serverNonce, newNonce);
        try {
            byte[] encryptedResponse = encrypt(serializedResponse, publicKey);

            // Step 5: getting DH parameters from server
            byte[] encryptedServerDHParams =
                    serverProxy.requestDHParams(messageID, nonce, resPQ.serverNonce,
                            pq.first, pq.second,
                            publicKey.getPublicExponent().longValue(), encryptedResponse);
            MessageDigest sha1Encoder = MessageDigest.getInstance("SHA-1");
            byte[] noncesHash = getNoncesHash(newNonce, resPQ.serverNonce, sha1Encoder);
            byte[] tmpAesKey = getTmpAesKey(noncesHash);
            byte[] tmpAesIV  = getTmpAesIV(newNonce, noncesHash, sha1Encoder);
            byte[] decryptedServerDHParams = decryptServerDHParams(tmpAesKey, tmpAesIV,
                    encryptedServerDHParams);

            ServerDHParams serverDHParams =
                    new ServerDHParams(decryptedServerDHParams, nonce, resPQ.serverNonce);

            // Step 6: generating private key (random number B), shared key (g_ab)
            Pair<KeyPair, byte[]> result = getMyKeyPairAndSecret(serverDHParams);
            KeyPair myKey = result.first;

            // Step 7: computing auth key
            // TODO: check if shared secret is auth key
            byte[] sharedSecret = result.second;

            // Step 8,9: sending DH response to server, getting ACK
            byte[] serializedDHResponse = serializeMyDHKey(nonce, resPQ.serverNonce, myKey.getPublic(), sha1Encoder);
            byte[] encryptedDHResponse = encryptDHKey(tmpAesKey, tmpAesIV, serializedDHResponse);
            boolean ack = serverProxy.setClientDHParams(nonce, resPQ.serverNonce, encryptedDHResponse);


            // Step PROFIT!!!! - ack received, saving shared secret
            if (ack) {
                return sharedSecret;
            }
        } catch (NoSuchAlgorithmException | InvalidKeyException | BadPaddingException |
                IllegalBlockSizeException | InvalidKeySpecException |
                InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }


        return null;
    }

    @Nullable
    private byte[] encrypt(byte[] serializedResponse,
                             RSAPublicKey publicKey) throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return cipher.doFinal(serializedResponse);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }

    private byte[] getNoncesHash(Nonce newNonce, Nonce serverNonce, MessageDigest sha1Encoder) {
        byte[] newNonceB = newNonce.getNonce();
        byte[] srvNonceB = serverNonce.getNonce();
        byte[] newPlusSrv = ByteBuffer.allocate(newNonceB.length + srvNonceB.length)
                .put(newNonceB)
                .put(srvNonceB)
                .array();
        return sha1Encoder.digest(newPlusSrv);
    }

    private byte[] getTmpAesKey(byte[] noncesHash) {
        return ByteBuffer.allocate(noncesHash.length + 12)
                .put(noncesHash)
                .put(noncesHash, 0, 12)
                .array();
    }

    private byte[] getTmpAesIV(Nonce newNonce, byte[] noncesHash, MessageDigest sha1Encoder) {
        byte[] newNonceB = newNonce.getNonce();

        byte[] doubleNonce = ByteBuffer.allocate(newNonceB.length * 2)
                .put(newNonceB)
                .put(newNonceB)
                .array();
        byte[] doubleNonceHash = sha1Encoder.digest(doubleNonce);

        return ByteBuffer.allocate(8 + doubleNonceHash.length + 4)
                .put(noncesHash, 12, 8)
                .put(doubleNonceHash)
                .put(newNonceB, 0, 4)
                .array();
    }

    private byte[] decryptServerDHParams(byte[] tmpAesKey, byte[] tmpAesIV, byte[] encryptedServerDHParams) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher eCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec ivSpec = new IvParameterSpec(tmpAesIV);
        SecretKeySpec secretKey = new SecretKeySpec(tmpAesKey, "AES");
        eCipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

        return eCipher.doFinal(encryptedServerDHParams);
    }

    private Pair<KeyPair, byte[]> getMyKeyPairAndSecret(ServerDHParams serverDHParams) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, InvalidKeySpecException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DH");
        DHParameterSpec dhParameterSpec = new DHParameterSpec(serverDHParams.dhPrime, serverDHParams.g);
        keyGen.initialize(dhParameterSpec);

        KeyAgreement myKeyAgreement = KeyAgreement.getInstance("DH");
        KeyPair myPair = keyGen.generateKeyPair();
        myKeyAgreement.init(myPair.getPrivate());

        KeyFactory myKeyFactory = KeyFactory.getInstance("DH");
        EncodedKeySpec gAKeySpec = new X509EncodedKeySpec(serverDHParams.gA.toByteArray());
        PublicKey gAKey = myKeyFactory.generatePublic(gAKeySpec);
        myKeyAgreement.doPhase(gAKey, true);

        byte[] secret = myKeyAgreement.generateSecret();
        return new Pair<>(myPair, secret);
    }

    private byte[] serializeMyDHKey(Nonce nonce, Nonce serverNonce, PublicKey gB,
                                    MessageDigest sha1Encoder) {
        return serializeMyDHKey(nonce, serverNonce, gB, 0, sha1Encoder);
    }

    private byte[] serializeMyDHKey(Nonce nonce, Nonce serverNonce, PublicKey gB, int retryID,
                                    MessageDigest sha1Encoder) {
        final int dataSize = nonce.getNonce().length + serverNonce.getNonce().length +
                gB.getEncoded().length + 4;
        byte[] data = ByteBuffer.allocate(dataSize)
                .put(nonce.getNonce())
                .put(serverNonce.getNonce())
                .put(gB.getEncoded())
                .putInt(retryID)
                .array();

        byte[] hash = sha1Encoder.digest(data);
        final int totalSize = hash.length + data.length;
        final int offset = 16 - (totalSize % 16);
        return ByteBuffer.allocate(totalSize + offset)
                .put(hash)
                .put(data)
                .array();
}

    private byte[] encryptDHKey(byte[] tmpAesKey, byte[] tmpAesIV, byte[] serializedKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher eCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec ivSpec = new IvParameterSpec(tmpAesIV);
        SecretKeySpec secretKey = new SecretKeySpec(tmpAesKey, "AES");
        eCipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

        return eCipher.doFinal(serializedKey);
    }

    private Pair<Integer, Integer> decomposePQ(long pq) {
        return PQDecomposer.decomposePQ(pq);
    }



    private long generateMessageID() {
        long unixTime = System.currentTimeMillis() / 1000L;
        unixTime <<= 32;
        SecureRandom sr = new SecureRandom();
        int addition = sr.nextInt() << 2;
        unixTime += addition;
        return unixTime;
    }

    // FIXME: sha1 instead of longValue
    private RSAPublicKey getRSAPublicKey(@NonNull long[] fingerprints) {
        RSAPublicKey[] publicKeys = getPublicKeys();

        List<RSAPublicKey> awailableKeys = new ArrayList<>(fingerprints.length);
        for (RSAPublicKey key : publicKeys) {
            long currentFingerprint = key.getPublicExponent().longValue();
            for (long fingerprint : fingerprints) {
                if (fingerprint == currentFingerprint) {
                    awailableKeys.add(key);
                    break;
                }
            }
        }

        SecureRandom sr = new SecureRandom();
        return awailableKeys.get(sr.nextInt() % awailableKeys.size());
    }

    private boolean checkMessageID(long messageID) {
        long unixTime = System.currentTimeMillis() / 1000L;
        long messageUnixTime = messageID >> 32;
        return messageUnixTime > unixTime - 300 &&
                messageUnixTime < unixTime + 30;
    }

    public RSAPublicKey[] keys;
    @NonNull
    private RSAPublicKey[] getPublicKeys() {
        return keys;
    }
}
