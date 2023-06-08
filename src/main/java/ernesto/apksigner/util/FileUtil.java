package ernesto.apksigner.util;

import java.io.*;

public class FileUtil {
    
    public static void writeTxt(String filePath, String content, boolean append) {
        try {
            File file = new File(filePath);
            FileWriter fw = new FileWriter(file.getAbsolutePath(), append);
            fw.write(content);
            fw.flush();
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean deleteDir(File dirFile) {
        if (!dirFile.exists()) {
            return false;
        }

        if (dirFile.isFile()) {
            return dirFile.delete();
        } else {

            for (File file : dirFile.listFiles()) {
                deleteDir(file);
            }
        }

        return dirFile.delete();
    }

    public static String getFileName(File file) {
        return file.getName().replaceAll("[.][^.]+$", "");
    }

}
