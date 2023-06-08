package ernesto.apksigner;

import com.android.apksigner.ApkSignerTool;
import com.android.apksigner.ParameterException;
import ernesto.apksigner.util.FileUtil;
import ernesto.apksigner.util.Utils;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class SignedController implements Initializable {
    @FXML
    private TextField tfKey;
    @FXML
    private TextField tfApk;
    @FXML
    private TextField tfSign;
    @FXML
    private Label tvMsg;
    @FXML
    private AnchorPane root;
    private boolean isKeyOk = false;
    private boolean isApkOk = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        File keyFile = new File(System.getProperty("user.dir") + "/config.xml");
        if (keyFile.exists()) {
            String path = keyFile.getPath();
            tfKey.setText(path);
            XMLHelper.xmlParser(path);
            isKeyOk = KeyConfig.getInstance().checkKey().getKey();
            tvMsg.setText(KeyConfig.getInstance().checkKey().getValue());
        } else {
            tvMsg.setText("Failed to load default key profile, please select manually!");
        }
    }

    public void refreshKey(ActionEvent actionEvent) {
        File keyFile = new File(tfKey.getText());
        if (keyFile.exists()) {
            String path = keyFile.getPath();
            tfKey.setText(path);
            XMLHelper.xmlParser(path);
            isKeyOk = KeyConfig.getInstance().checkKey().getKey();
            tvMsg.setText(KeyConfig.getInstance().checkKey().getValue());
        } else {
            tvMsg.setText("Failed to refresh key profile, please select manually!");
        }
    }

    public void editKey(ActionEvent actionEvent) {
        File file = new File(tfKey.getText());
        if (file.exists()) {
            Utils.openUrl(tfKey.getText());
            tvMsg.setText("Once the edit key has been configured, please refresh it!");
        } else {
            tvMsg.setText("Edit key profile failed, please select manually!");
        }
    }

    public void openKey(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select key configuration");
        File file = new File(tfKey.getText()).getParentFile();
        if (file != null && file.exists()) {
            fileChooser.setInitialDirectory(file);
        } else {
            fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        }
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("XML", "*.xml")

        );
        File keyFile = fileChooser.showOpenDialog(getStage());
        if (keyFile != null && keyFile.exists()) {
            String path = keyFile.getPath();
            tfKey.setText(path);
            XMLHelper.xmlParser(path);
            isKeyOk = KeyConfig.getInstance().checkKey().getKey();
            tvMsg.setText(KeyConfig.getInstance().checkKey().getValue());
        }
    }

    public void openApk(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select the Apk to be signed");
        File file = new File(tfApk.getText()).getParentFile();
        if (file != null && file.exists()) {
            fileChooser.setInitialDirectory(file);
        } else {
            fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        }
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("APK", "*.apk")

        );
        File apkFile = fileChooser.showOpenDialog(getStage());
        if (apkFile != null) {
            String path = apkFile.getPath();
            tfApk.setText(path);
            tfSign.setText("");
            isApkOk = true;
            try {
                String msg = Utils.exeCmd("java -jar apksigner.jar verify -v " + path);
                if (Utils.isEmpty(msg)) {
                    tvMsg.setText("Apk not yet signed so it's ready to start signing!");
                } else {
                    tvMsg.setText("Apk already signed!\n" + msg);
                }
            } catch (Exception e) {
                tvMsg.setText(e.getMessage());
            }
        }
    }

    public void signOld(ActionEvent actionEvent) {
        if (checkStatus()) {
            Utils.runOnIOThread(() -> {
                String apkPath = tfApk.getText();
                File apkFile = new File(apkPath);
                File signDirectory = new File(apkFile.getParentFile(), FileUtil.getFileName(apkFile) + "_signv1");
                signDirectory.mkdirs();
                try {
                    String msg = signV1(apkPath, signDirectory.getAbsolutePath());
                    Platform.runLater(() -> {
                        tvMsg.setText(msg);
                    });
                    tfSign.setText(signDirectory.getAbsolutePath());
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        tvMsg.setText(e.getMessage());
                    });
                }
            });
        }
    }

    private String signV1(String apkPath, String signDirectory) {
        String signApkPath = signDirectory + "\\" + new File(apkPath).getName();
        File signApk = new File(signApkPath);
        if (signApk.exists()) {
            signApk.delete();
        }
        String cmd = "jarsigner -verbose -keystore " + KeyConfig.getInstance().getPath()
                + " -storepass " + KeyConfig.getInstance().getStorePassword()
                + " -keypass " + KeyConfig.getInstance().getKeyPassword()
                + " -signedjar "
                + signApkPath + " "
                + apkPath + " "
                + KeyConfig.getInstance().getKeyAlias()
                + " -sigfile CERT";
        String msg = Utils.exeCmd(cmd);
        if (new File(signApkPath).exists() && msg.contains("META-INF/MANIFEST.MF")) {
            return apkPath + "- Sign in V1 mode successfully!\n";
        } else {
            return apkPath + "- Sign in V1 mode failed! Possible causes: \n1. Key alias OR password misconfigured! \n2. Apk already signed, cannot be re-signed using the old v1 signature. \n";
        }
    }

    public void signNew(ActionEvent actionEvent) {
        if (checkStatus()) {
            Utils.runOnIOThread(() -> {
                String apkPath = tfApk.getText();
                File apkFile = new File(apkPath);
                File alignDirectory = new File(apkFile.getParentFile(), FileUtil.getFileName(apkFile) + "_align");
                alignDirectory.mkdirs();
                File signDirectory = new File(apkFile.getParentFile(), FileUtil.getFileName(apkFile) + "_signv2");
                signDirectory.mkdirs();
                try {
                    String msg = signedV2(apkPath, alignDirectory.getAbsolutePath(), signDirectory.getAbsolutePath());
                    Platform.runLater(() -> tvMsg.setText(msg));
                    tfSign.setText(signDirectory.getAbsolutePath());
                } catch (Exception e) {
                    Platform.runLater(() -> tvMsg.setText(e.getMessage()));
                }
                FileUtil.deleteDir(alignDirectory);
            });
        }
    }

    private String signedV2(String apkPath, String alignDirectory, String signDirectory) throws Exception {
        String apkName = new File(apkPath).getName();
        StringBuilder sb = new StringBuilder();
        String alignapk = alignDirectory + "\\" + apkName;
        boolean alignSuc = Utils.zipAlign(apkPath, alignapk, 4, true);
        if (new File(alignapk).exists()) {
            sb.append(apkPath).append("- ApkAlign Successful\n");
        } else {
            sb.append(apkPath).append("- ApkAlign Failed\n");
            return sb.toString();
        }
        String signPath = signDirectory + "\\" + apkName;
        String[] params = {
                "--ks", KeyConfig.getInstance().getPath(),
                "--ks-key-alias", KeyConfig.getInstance().getKeyAlias(),
                "--ks-pass", "pass:" + KeyConfig.getInstance().getStorePassword(),
                "--key-pass", "pass:" + KeyConfig.getInstance().getKeyPassword(),
                "--out", signPath,
                alignapk
        };

        try {
            ApkSignerTool.sign(params);
        } catch (ParameterException pe) {
            sb.append(apkPath).append("- V1+V2 signature process error, caused by '").append(pe.getMessage()).append("'\n");
        }
        if (new File(signPath).exists()) {
            sb.append(apkPath).append("- Sign in V1+V2 mode successfully!\n");
        } else {
            sb.append(apkPath).append("- Signing in V1+V2 mode failed! Possible cause: key alias OR wrong password configuration!\n");
        }
        return sb.toString();
    }

    public void openSign(ActionEvent actionEvent) {
        if (!Utils.isEmpty(tfSign.getText())) {
            Utils.openUrl(new File(tfSign.getText()).getAbsolutePath());
        } else {
            tvMsg.setText("Please sign first to generate a new Apk!");
        }
    }

    public void verifyStatus(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("APK Files", "*.apk"));
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        File apkFile = fileChooser.showOpenDialog(getStage());
        Utils.runOnIOThread(() -> {
            try {
                if (apkFile != null) {
                    String[] params = new String[] {
                            "-v", apkFile.getAbsolutePath()
                    };
                    StringBuilder msg = new StringBuilder();
                    msg.append(ApkSignerTool.verify(params)).append("\n");
                    msg.append("Apk has ").append(Utils.isAligned(apkFile.getAbsolutePath(), 4) ? "" : "not ").append("been aligned");
                    Utils.runOnUiThread(() -> {
                        showMessageBox(msg.toString());
                    });
                }
            } catch (Exception e) {
                Utils.runOnUiThread(() -> {
                    showMessageBox(e.getMessage());
                });
            }
        });
    }

    private void showMessageBox(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Tips");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private Boolean checkStatus() {
        if (!isKeyOk) {
            tvMsg.setText("There are errors in key configuration profile, please reselect!");
            return false;
        }
        if (!isApkOk) {
            tvMsg.setText("Apk to be signed is corrupted, please reselect!");
            return false;
        }
        return true;
    }

    private Stage getStage() {
        return (Stage) root.getScene().getWindow();
    }
}