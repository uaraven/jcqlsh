package net.ninjacat.cql;

import com.beust.jcommander.Parameter;

import java.io.File;

/**
 * Command line parameters
 */
public class Parameters {
    @Parameter(description = "KeyspaceTable host to connect to in host[:port]")
    private String host = "localhost";

    @Parameter(names = "--ssl", description = "Use SSL to connect to server")
    private boolean useSsl = false;

    @Parameter(names = "--ssl-unsafe", description = "Trust any server certificate")
    private boolean usafeSsl = false;

    @Parameter(names = "--keystore", description = "Use certificate from keystore to authenticate to server")
    private File keystore;

    @Parameter(names = "--keystore-password", description = "Password to keystore")
    private String keystorePassword;

    public String getHost() {
        return host;
    }

    public boolean isUseSsl() {
        return useSsl;
    }

    public boolean isUsafeSsl() {
        return usafeSsl;
    }

    public File getKeystore() {
        return keystore;
    }

    public String getKeystorePassword() {
        return keystorePassword;
    }
}
