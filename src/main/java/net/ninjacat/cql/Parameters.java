package net.ninjacat.cql;

import com.beust.jcommander.Parameter;

import java.io.File;
import java.util.Optional;

/**
 * Command line parameters
 */
public class Parameters {
    @Parameter(description = "Host to connect to in host[:port] format")
    private String host = "localhost";

    @Parameter(names = "--ssl", description = "Use SSL to connect to server")
    private boolean useSsl = false;

    @Parameter(names = "--ssl-unsafe", description = "Trust any server certificate")
    private boolean usafeSsl = false;

    @Parameter(names = "--keystore", description = "Use certificate from keystore to authenticate to server", arity = 1)
    private File keystore;

    @Parameter(names = "--keystore-password", description = "Password to keystore", arity = 1)
    private String keystorePassword;

    @Parameter(names = "--no-color", description = "Do not use colored output")
    private boolean noColor = false;

    @Parameter(names = "--help", hidden = true)
    private boolean showHelp = false;

    @Parameter(names = "--debug", hidden = true)
    private boolean debug = false;

    @Parameter(names = "--file", description = "Executes CQL script without starting a shell session")
    private String sourceFile = null;

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

    public boolean isShowHelp() {
        return this.showHelp;
    }

    public boolean isDebug() {
        return this.debug;
    }

    public Optional<String> getSourceFile() {
        return Optional.ofNullable(this.sourceFile);
    }
}
