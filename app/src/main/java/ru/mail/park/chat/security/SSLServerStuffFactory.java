package ru.mail.park.chat.security;

import android.view.KeyCharacterMap;

import org.spongycastle.asn1.ASN1EncodableVector;
import org.spongycastle.asn1.ASN1InputStream;
import org.spongycastle.asn1.ASN1Sequence;
import org.spongycastle.asn1.DERSequence;
import org.spongycastle.asn1.x500.X500Name;
import org.spongycastle.asn1.x509.BasicConstraints;
import org.spongycastle.asn1.x509.Extension;
import org.spongycastle.asn1.x509.KeyPurposeId;
import org.spongycastle.asn1.x509.KeyUsage;
import org.spongycastle.asn1.x509.SubjectKeyIdentifier;
import org.spongycastle.asn1.x509.SubjectPublicKeyInfo;
import org.spongycastle.cert.X509v3CertificateBuilder;
import org.spongycastle.cert.bc.BcX509ExtensionUtils;
import org.spongycastle.cert.jcajce.JcaX509CertificateConverter;
import org.spongycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.operator.ContentSigner;
import org.spongycastle.operator.jcajce.JcaContentSignerBuilder;
import org.spongycastle.pqc.jcajce.provider.BouncyCastlePQCProvider;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyManagementException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * Created by Михаил on 21.06.2016.
 */
public class SSLServerStuffFactory {
    private static final String SIGNATURE_ALGORITHM = "SHA512withECDSA";
    private static final String KEY_GENERATION_ALGORITHM = "ECDH";
    private static final String KEY_STORE_INSTANCE = "BKS";
    private static final String KMF_INSTANCE = "PKIX";
    private static final String SSL_CONTEXT = "TLSv1.2";

    private static final Date BEFORE = new Date(System.currentTimeMillis() - 5000);
    private static final Date AFTER = new Date(System.currentTimeMillis() + 600000);

    private static final String PROVIDER_NAME = BouncyCastleProvider.PROVIDER_NAME;
    static {
        Security.addProvider(new BouncyCastleProvider());
        Security.addProvider(new BouncyCastlePQCProvider());
    }

    /**
     * Generates a one time use secure random number to be used as the password
     * for a keystore
     *
     * Retrieved from http://codereview.stackexchange.com/questions/117944/bouncycastle-implementation-with-x509certificate-signing-keystore-generation-a
     *
     * @return Returns void on completion
     */
    public static byte[] generateNonce() {
        SecureRandom rand = new SecureRandom();
        byte[] nonce = new byte[2048];
        rand.nextBytes(nonce);
        return nonce;
    }

    /**
     *
     * Retrieved from http://codereview.stackexchange.com/questions/117944/bouncycastle-implementation-with-x509certificate-signing-keystore-generation-a
     *
     * @param ks
     * @param nonce
     * @return
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws IOException
     */
    public static byte[] generateKeyData(KeyStore ks, byte[] nonce) throws CertificateException,
            NoSuchAlgorithmException, KeyStoreException, IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ks.store(os, (new String(nonce)).toCharArray());
        byte[] keyData = os.toByteArray();
        os.close();
        return keyData;
    }

    /**
     *
     * Retrieved from http://codereview.stackexchange.com/questions/117944/bouncycastle-implementation-with-x509certificate-signing-keystore-generation-a
     *
     * @param nonce
     * @param keyPair
     * @return
     * @throws KeyStoreException
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    public static KeyStore generateKeyStore(byte[] nonce, KeyPair keyPair, Certificate cert) throws KeyStoreException,
            CertificateException, NoSuchAlgorithmException, IOException {
        KeyStore ks = KeyStore.getInstance(KEY_STORE_INSTANCE);
        ks.load(null, (new String(nonce)).toCharArray());
        byte[] tempPass = new byte[2048];
        new SecureRandom().nextBytes(tempPass);
        ks.setKeyEntry("foo.bar", keyPair.getPrivate(), new String(tempPass).toCharArray(), new java.security.cert.Certificate[] { cert });
        return ks;
    }

    /**
     *
     * Retrieved from http://codereview.stackexchange.com/questions/117944/bouncycastle-implementation-with-x509certificate-signing-keystore-generation-a
     *
     * @return
     * @throws NoSuchProviderException
     * @throws NoSuchAlgorithmException
     */
    public static KeyPair generateKeyPair() throws NoSuchProviderException, NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KEY_GENERATION_ALGORITHM, PROVIDER_NAME);
        return keyPairGenerator.generateKeyPair();
    }

    /**
     * Create a certificate to use by a Certificate Authority
     *
     * Retrieved from http://www.programcreek.com/java-api-examples/index.php?class=org.bouncycastle.cert.X509v3CertificateBuilder&method=addExtension
     *
     * @param publicKey Public key
     * @param privateKey Private key
     * @return Generated X509 Certificate
     */
    public static X509Certificate createCACert(PublicKey publicKey, PrivateKey privateKey) throws Exception {
        X500Name issuerName = new X500Name("CN=127.0.0.1, O=FOO, L=BAR, ST=BAZ, C=QUX");

        X500Name subjectName = issuerName;

        BigInteger serial = BigInteger.valueOf(new SecureRandom().nextInt());

        X509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(issuerName, serial, BEFORE, AFTER, subjectName, publicKey);
        builder.addExtension(Extension.subjectKeyIdentifier, false, createSubjectKeyIdentifier(publicKey));
        builder.addExtension(Extension.basicConstraints, true, new BasicConstraints(true));

        KeyUsage usage = new KeyUsage(KeyUsage.keyCertSign | KeyUsage.digitalSignature | KeyUsage.keyEncipherment | KeyUsage.dataEncipherment | KeyUsage.cRLSign);
        builder.addExtension(Extension.keyUsage, false, usage);

        ASN1EncodableVector purposes = new ASN1EncodableVector();
        purposes.add(KeyPurposeId.id_kp_serverAuth);
        purposes.add(KeyPurposeId.id_kp_clientAuth);
        purposes.add(KeyPurposeId.anyExtendedKeyUsage);
        builder.addExtension(Extension.extendedKeyUsage, false, new DERSequence(purposes));

        X509Certificate cert = signCertificate(builder, privateKey);
        cert.checkValidity(new Date());
        cert.verify(publicKey);

        return cert;
    }

    /**
     * Helper method
     *
     * Retrieved from http://www.programcreek.com/java-api-examples/index.php?api=org.bouncycastle.cert.bc.BcX509ExtensionUtils
     *
     * @param key
     * @return
     * @throws Exception
     */
    private static SubjectKeyIdentifier createSubjectKeyIdentifier(Key key) throws Exception {
        ASN1InputStream is = new ASN1InputStream(new ByteArrayInputStream(key.getEncoded()));
        ASN1Sequence seq = (ASN1Sequence) is.readObject();
        is.close();
        @SuppressWarnings("deprecation")
        SubjectPublicKeyInfo info = new SubjectPublicKeyInfo(seq);
        return new BcX509ExtensionUtils().createSubjectKeyIdentifier(info);
    }

    /**
     * Helper method
     *
     * Retrieved from http://www.programcreek.com/java-api-examples/index.php?source_dir=mockserver-master/mockserver-core/src/main/java/org/mockserver/socket/KeyStoreFactory.java
     *
     * @param certificateBuilder
     * @param signedWithPrivateKey
     * @return
     * @throws Exception
     */
    private static X509Certificate signCertificate(X509v3CertificateBuilder certificateBuilder, PrivateKey signedWithPrivateKey) throws Exception {
        ContentSigner signer = new JcaContentSignerBuilder(SIGNATURE_ALGORITHM).setProvider(PROVIDER_NAME).build(signedWithPrivateKey);
        return new JcaX509CertificateConverter().setProvider(PROVIDER_NAME).getCertificate(certificateBuilder.build(signer));
    }

    public static SSLServerSocket getServerSocket(SSLServerSocketFactory factory, int port, byte[] keyData, byte[] nonce) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, UnrecoverableKeyException, KeyManagementException {


        SSLServerSocket socket = (SSLServerSocket) factory.createServerSocket(port);
        socket.setEnabledCipherSuites(socket.getSupportedCipherSuites());
        socket.setEnabledProtocols(socket.getSupportedProtocols());

        return socket;
    }

    public static KeyStore generateFinalKeyStore(byte[] keyData, byte[] nonce) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        KeyStore ks = KeyStore.getInstance(KEY_STORE_INSTANCE);
        ks.load(new ByteArrayInputStream(keyData), (new String(nonce).toCharArray()));
        return ks;
    }

    public static KeyManager[] getKeyManagers(byte[] nonce, KeyStore ks) throws NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException {

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KMF_INSTANCE);
        kmf.init(ks, (new String(nonce).toCharArray()));
        return kmf.getKeyManagers();
    }
}
