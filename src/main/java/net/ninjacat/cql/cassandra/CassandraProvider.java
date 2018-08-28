package net.ninjacat.cql.cassandra;

import com.datastax.driver.core.*;
import net.ninjacat.cql.Parameters;
import org.jline.terminal.Terminal;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * Creates a session to a KeyspaceTable cluster
 */
public final class CassandraProvider {
    private CassandraProvider() {
    }

    public static Session createSession(final Parameters connectionParameters, final Terminal terminal) throws Exception {

        final PrintStream printer = new PrintStream(terminal.output());
        printer.println("Connecting to " + connectionParameters.getHost());

        final Cluster.Builder builder = Cluster.builder()
                .addContactPoints(connectionParameters.getHost())
                .withProtocolVersion(ProtocolVersion.V4)
                .withoutJMXReporting()
                .withQueryOptions(
                        new QueryOptions()
                                .setConsistencyLevel(ConsistencyLevel.LOCAL_ONE)
                                .setSerialConsistencyLevel(ConsistencyLevel.LOCAL_SERIAL)
                );

        if (connectionParameters.isUseSsl()) {
            builder.withSSL(JdkSSLOptions.builder()
                    .withSSLContext(createSslContext(connectionParameters))
                    .build());
        }

        final Cluster cluster = builder.build();
        final Session session = cluster.connect();
        printer.println("Connected");
        return session;
    }

    /**
     * Sets up SSL connection to Cassandra
     *
     * @param parameters Command line parameters
     * @return {@link SSLContext}
     */
    private static SSLContext createSslContext(final Parameters parameters) throws Exception {
        // load keystore
        final KeyStore keyStore;
        if (parameters.getKeystore().getName().endsWith("jks")) {
            keyStore = KeyStore.getInstance("JKS");
        } else {
            keyStore = KeyStore.getInstance("PKCS12");
        }
        try (final InputStream inputStream = new FileInputStream(parameters.getKeystore())) {
            keyStore.load(inputStream, parameters.getKeystorePassword().toCharArray());
        }
        final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
        keyManagerFactory.init(keyStore, parameters.getKeystorePassword().toCharArray());

        // setup trust manager
        final TrustManager[] trustManagers = parameters.isUsafeSsl() ? getAllTrustingManager() : createTrustManager(keyStore);

        // initialize ssl context
        final SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
        final Provider provider = Security.getProvider("SunJSSE");
        final SSLContext sslContext = SSLContext.getInstance("TLSv1.2", provider);

        sslContext.init(keyManagerFactory.getKeyManagers(), trustManagers, secureRandom);
        return sslContext;
    }

    private static TrustManager[] createTrustManager(final KeyStore keyStore) throws NoSuchAlgorithmException, KeyStoreException {
        final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("PKIX");
        trustManagerFactory.init(keyStore);
        return trustManagerFactory.getTrustManagers();
    }

    private static TrustManager[] getAllTrustingManager() {
        return new TrustManager[]{
                new X509TrustManager() {

                    @Override
                    public void checkClientTrusted(final X509Certificate[] x509Certificates, final String s) throws CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(final X509Certificate[] x509Certificates, final String s) throws CertificateException {
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                }
        };
    }

}
