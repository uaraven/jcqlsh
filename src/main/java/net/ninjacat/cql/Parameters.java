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

    @Parameter(names = "--no-color", description = "Do not use colored output")
    private boolean noColor = false;

    public String getHost() {
        return this.host;
    }

    public boolean isUseSsl() {
        return this.useSsl;
    }

    public boolean isUsafeSsl() {
        return this.usafeSsl;
    }

    public File getKeystore() {
        return this.keystore;
    }

    public String getKeystorePassword() {
        return this.keystorePassword;
    }

    public boolean isNoColor() {
        return this.noColor;
    }
}
