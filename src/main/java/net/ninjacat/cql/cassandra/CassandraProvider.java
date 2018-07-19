package net.ninjacat.cql.cassandra;

import com.datastax.driver.core.*;
import net.ninjacat.cql.Parameters;
import org.jline.terminal.Terminal;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.security.KeyStore;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class CassandraProvider {
    public Session createSession(Parameters connectionParameters, Terminal terminal) throws Exception {

        final InetAddress cassandraHost = InetAddress.getByName(connectionParameters.getHost());

        final PrintStream printer = new PrintStream(terminal.output());
        printer.println("Connecting to " + cassandraHost.getHostAddress());

        final Cluster.Builder builder = Cluster.builder()
                .addContactPoints(cassandraHost)
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
    private SSLContext createSslContext(Parameters parameters) throws Exception {
        // load keystore
        final KeyStore keyStore = KeyStore.getInstance("JKS");
        try (InputStream inputStream = new FileInputStream(parameters.getKeystore())) {
            keyStore.load(inputStream, parameters.getKeystorePassword().toCharArray());
        }
        final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
        keyManagerFactory.init(keyStore, parameters.getKeystorePassword().toCharArray());

        // setup trust manager
        final TrustManager[] trustManagers = parameters.isUsafeSsl() ? getAllTrustingManager() : null;

        // initialize ssl context
        final SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
        final Provider provider = Security.getProvider("SunJSSE");
        final SSLContext sslContext = SSLContext.getInstance("TLSv1.2", provider);

        sslContext.init(keyManagerFactory.getKeyManagers(), trustManagers, secureRandom);
        return sslContext;
    }

    private TrustManager[] getAllTrustingManager() {
        return new TrustManager[]{
                new X509TrustManager() {

                    @Override
                    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                }
        };
    }

}
