package ernesto.apksigner;

import javafx.util.Pair;
import ernesto.apksigner.util.Utils;

import java.io.File;

public class KeyConfig {
    private String path;
    private String keyAlias;
    private String keyPassword;
    private String storePassword;

    private static KeyConfig instance;

    private KeyConfig() {
    }

    public static KeyConfig getInstance() {
        if (instance == null) {
            instance = new KeyConfig();
        }
        return instance;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getKeyAlias() {
        return keyAlias;
    }

    public void setKeyAlias(String keyAlias) {
        this.keyAlias = keyAlias;
    }

    public String getKeyPassword() {
        return keyPassword;
    }

    public void setKeyPassword(String keyPassword) {
        this.keyPassword = keyPassword;
    }

    public String getStorePassword() {
        return storePassword;
    }

    public void setStorePassword(String storePassword) {
        this.storePassword = storePassword;
    }

    public Pair<Boolean, String> checkKey() {
        if (Utils.isEmpty(path) || Utils.isEmpty(storePassword) || Utils.isEmpty(keyAlias) || Utils.isEmpty(keyPassword)) {
            return new Pair(false, "Keystore configuration profile malformated, please check it out!");
        }

        if (!new File(path).exists()) {
            return new Pair(false, "Keystore configuration file does not exist, please check it out!");
        }

        if (!path.endsWith(".keystore") && !path.endsWith(".jks")) {
            return new Pair(false, "The path of keystore configuration file is in the wrong format, please check it out!");
        }

        return new Pair(true, "The initial check of the key profile passes and you can proceed to the next step!");
    }
}
