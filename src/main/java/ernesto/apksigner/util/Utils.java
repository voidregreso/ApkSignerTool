package ernesto.apksigner.util;

import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Utils {

    static {
        System.loadLibrary("ZipAlignJNI");
    }

    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static String exeCmd(String commandStr) {
        System.out.println("exeCmd: " + commandStr);
        BufferedReader br = null;
        try {
            Process p = Runtime.getRuntime().exec(commandStr);
            br = new BufferedReader(new InputStreamReader(p.getInputStream(), "GBK"));
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            System.out.println("exeCmd: " + sb.toString());
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return "";
    }


    public static void openUrl(String url) {
        String cmd = "rundll32 url.dll,FileProtocolHandler " + url;
        try {
            Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static native boolean zipAlign(String fin, String fout, int alg_bytes, boolean force);
    public static native boolean isAligned(String path, int alg_bytes);

    public static void runOnUiThread(Runnable run) {
        Platform.runLater(run);
    }

    public static void runOnIOThread(Runnable run) {
        new Thread(run).start();
    }
}
