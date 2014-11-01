package avifro.com;

import javax.net.ssl.*;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;


/**
 * Created by avifro on 10/30/14.
 */
public class ClientHelper {

    private TrustManager[ ] certs = null;
    private SSLContext sslCtx = null;
    private HostnameVerifier hostnameVerifier;

    public ClientHelper() {

        certs = new TrustManager[]{
            new X509TrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType)
                        throws CertificateException {
                }

                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType)
                        throws CertificateException {
                }
            }
        };
        try {
            sslCtx = SSLContext.getInstance("TLS");
            sslCtx.init(null, certs, new SecureRandom());
        } catch (java.security.GeneralSecurityException ex) {
        }
        HttpsURLConnection.setDefaultSSLSocketFactory(sslCtx.getSocketFactory());

        hostnameVerifier = new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
    }

    public SSLContext getDefaultSSLContext() {
        return sslCtx;
    }

    public HostnameVerifier getDefualtHostnameVerifier() {
        return hostnameVerifier;
    }

}
