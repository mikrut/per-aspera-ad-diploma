package ru.mail.park.chat.security;

import android.content.Context;
import android.util.Log;
import android.view.KeyCharacterMap;

import org.spongycastle.asn1.ASN1EncodableVector;
import org.spongycastle.asn1.ASN1InputStream;
import org.spongycastle.asn1.ASN1Sequence;
import org.spongycastle.asn1.DERSequence;
import org.spongycastle.asn1.x500.X500Name;
import org.spongycastle.asn1.x509.AlgorithmIdentifier;
import org.spongycastle.asn1.x509.BasicConstraints;
import org.spongycastle.asn1.x509.Extension;
import org.spongycastle.asn1.x509.KeyPurposeId;
import org.spongycastle.asn1.x509.KeyUsage;
import org.spongycastle.asn1.x509.SubjectKeyIdentifier;
import org.spongycastle.asn1.x509.SubjectPublicKeyInfo;
import org.spongycastle.cert.CertIOException;
import org.spongycastle.cert.X509CertificateHolder;
import org.spongycastle.cert.X509v3CertificateBuilder;
import org.spongycastle.cert.bc.BcX509ExtensionUtils;
import org.spongycastle.cert.jcajce.JcaX509CertificateConverter;
import org.spongycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.spongycastle.crypto.digests.SHA1Digest;
import org.spongycastle.jcajce.provider.asymmetric.rsa.DigestSignatureSpi;
import org.spongycastle.jcajce.provider.digest.SHA1;
import org.spongycastle.jce.X509Principal;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.operator.ContentSigner;
import org.spongycastle.operator.OperatorCreationException;
import org.spongycastle.operator.jcajce.JcaContentSignerBuilder;
import org.spongycastle.pqc.jcajce.provider.BouncyCastlePQCProvider;
import org.spongycastle.x509.X509V3CertificateGenerator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyManagementException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
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
    private static final String TAG = SSLServerStuffFactory.class.getSimpleName();

    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
    private static final String KEY_GENERATION_ALGORITHM = "RSA";
    private static final String KEY_STORE_INSTANCE = "BKS";
    private static final String KMF_INSTANCE = "PKIX";

    public static final String CERT_ALIAS = "certificate";
    private static final String SECRET = "mysecret";
    private static final String KEY_STORE_FILENAME = "keystore";

    private static final String PUBLIC_KEY_DIGEST = "SHA-1";

    private static final String PROVIDER_NAME = BouncyCastleProvider.PROVIDER_NAME;
    static {
        Security.addProvider(new BouncyCastleProvider());
        Security.addProvider(new BouncyCastlePQCProvider());
    }

    public static KeyStore getKeyStore(Context context)
            throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException,
            NoSuchProviderException, OperatorCreationException, InvalidKeyException,
            SignatureException {
        File keyStoreFile = new File(context.getFilesDir(), KEY_STORE_FILENAME);
        keyStoreFile.createNewFile();

        KeyStore ks;
        InputStream is = new FileInputStream(keyStoreFile);
        try {
            ks = KeyStore.getInstance(KEY_STORE_INSTANCE);
            try {
                ks.load(is, SECRET.toCharArray());
            } catch (IOException e) {
                ks.load(null, SECRET.toCharArray());
                Log.w(TAG, e.getMessage(), e);
            }
        } finally {
            is.close();
        }

        if (!ks.containsAlias(CERT_ALIAS)) {
            KeyPair keyPair = SSLServerStuffFactory.generateKeyPair(new SecureRandom());
            X509Certificate certificate = createCACert(keyPair.getPublic(), keyPair.getPrivate());
            Log.v("Certificate", certificate.toString());
            ks.setKeyEntry(CERT_ALIAS, keyPair.getPrivate(), SECRET.toCharArray(),
                    new X509Certificate[] { certificate });

            OutputStream os = new FileOutputStream(keyStoreFile);
            try {
                ks.store(os, SECRET.toCharArray());
            } finally {
                os.close();
            }
        }
        return ks;
    }

    /**
     * Retrieves key managers from a KeyStore
     *
     * @param ks
     * @return
     * @throws NoSuchAlgorithmException
     * @throws UnrecoverableKeyException
     * @throws KeyStoreException
     */
    public static KeyManager[] getKeyManagers(KeyStore ks) throws NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException {
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KMF_INSTANCE);
        kmf.init(ks, SECRET.toCharArray());
        return kmf.getKeyManagers();
    }

    public static byte[] getPublicKeyFingerprint(PublicKey key) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(PUBLIC_KEY_DIGEST);
        return digest.digest(key.getEncoded());
    }

    /**
     * First we create a public/private key pair for the new certificate.
     * Retrieved from http://codereview.stackexchange.com/questions/117944/bouncycastle-implementation-with-x509certificate-signing-keystore-generation-a
     * Retrieved from https://www.mayrhofer.eu.org/create-x509-certs-in-java
     *
     * @param sr secure random to use by generator
     * @return a public/private key pair for the new certificate
     * @throws NoSuchProviderException
     * @throws NoSuchAlgorithmException
     */
    private static KeyPair generateKeyPair(SecureRandom sr) throws NoSuchProviderException, NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KEY_GENERATION_ALGORITHM, PROVIDER_NAME);
        keyPairGenerator.initialize(2048, sr);
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
    private static X509Certificate createCACert(PublicKey publicKey, PrivateKey privateKey) throws CertIOException, CertificateException, OperatorCreationException, NoSuchProviderException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        final Date BEFORE = new Date(System.currentTimeMillis() - 5000);
        final Date AFTER = new Date(System.currentTimeMillis() + 24L*60*60*1000);

        // signers name
        X500Name issuerName = new X500Name("CN=127.0.0.1, O=TorChat, L=World, ST=Universe, C=RU");
        // subjects name - the same as we are self signed.
        X500Name subjectName = issuerName;

        // serial
        BigInteger serial = BigInteger.valueOf(new SecureRandom().nextInt());

        X509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(issuerName, serial, BEFORE, AFTER, subjectName, publicKey);
        // builder.addExtension(Extension.subjectKeyIdentifier, false, createSubjectKeyIdentifier(publicKey));
        // builder.addExtension(Extension.basicConstraints, true, new BasicConstraints(true));

        KeyUsage usage = new KeyUsage(KeyUsage.digitalSignature |
                KeyUsage.keyEncipherment |
                KeyUsage.keyAgreement
        );
        builder.addExtension(Extension.keyUsage, false, usage);

//        ASN1EncodableVector purposes = new ASN1EncodableVector();
//        purposes.add(KeyPurposeId.id_kp_serverAuth);
//        purposes.add(KeyPurposeId.id_kp_clientAuth);
//        purposes.add(KeyPurposeId.anyExtendedKeyUsage);
//        builder.addExtension(Extension.extendedKeyUsage, false, new DERSequence(purposes));

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
    private static X509Certificate signCertificate(X509v3CertificateBuilder certificateBuilder, PrivateKey signedWithPrivateKey) throws OperatorCreationException, CertificateException {
        ContentSigner signer = new JcaContentSignerBuilder(SIGNATURE_ALGORITHM).setProvider(PROVIDER_NAME).build(signedWithPrivateKey);
        X509CertificateHolder holder = certificateBuilder.build(signer);
        Log.d("Holder    issuer", holder.getIssuer().toString());
        Log.d("Holder algorithm", holder.getSignatureAlgorithm().getAlgorithm().getId());
        Log.d("Holder   subject", holder.getSubject().toString());

        return new JcaX509CertificateConverter().setProvider(PROVIDER_NAME).getCertificate(holder);
    }
}
