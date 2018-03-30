package com.neuifo.appkit.websocket;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;



public class HttpsHelper {

    // keytool -printcert -rfc -file uwca.crt
    // 0c0890ba:ASN.1 encoding routines:asn1_check_tlen:WRONG_TAG
    private static InputStream loasInputStream() {
        String c = "-----BEGIN CERTIFICATE-----\n" +
                "-----END CERTIFICATE-----";

        // convert String into InputStream
        InputStream is = new ByteArrayInputStream(c.getBytes());
        return is;

    }


    public static Object[] loadCertificate() throws Exception {

        // Load CAs from an InputStream// (could be from a resource or ByteArrayInputStream or ...)
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        // From https://www.washington.edu/itconnect/security/ca/load-der.crt
        InputStream caInput = new BufferedInputStream(loasInputStream()/*new FileInputStream("load-der.crt")*/);
        Certificate ca;
        try {
            ca = cf.generateCertificate(caInput);
            //System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
        } finally {
            caInput.close();
        }

        // Create a KeyStore containing our trusted CAs
        String keyStoreType = KeyStore.getDefaultType();
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        keyStore.load(null, null);
        keyStore.setCertificateEntry("ca", ca);

        // Create a TrustManager that trusts the CAs in our KeyStore
        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init(keyStore);

        TrustManager trustManager = tmf.getTrustManagers()[0];

        // Create an SSLContext that uses our TrustManager
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, tmf.getTrustManagers(), null);

        SSLSocketFactory sslSocketFactory = context.getSocketFactory();

        return new Object[] {sslSocketFactory, trustManager};

        // Tell the URLConnection to use a SocketFactory from our SSLContext
        // URL url = new URL("https://certs.cac.washington.edu/CAtest/");
        // HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
        // urlConnection.setSSLSocketFactory(context.getSocketFactory());
        // InputStream in = urlConnection.getInputStream();
        // copyInputStreamToOutputStream(in, System.out);
    }
}
