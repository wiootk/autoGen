package tk.wioo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;


/**
 * Created by Administrator on 2017-09-29.
 */
public class FileUtils {

    private static Logger logger = LoggerFactory.getLogger(FileUtils.class);
    public static ResourceBundle getResource(String fileName) {
        ResourceBundle rb = ResourceBundle.getBundle(fileName, Locale.getDefault());
        BufferedInputStream inputStream;
        String proFilePath = System.getProperty("user.dir") + "\\" + fileName + ".properties";
        try {
            inputStream = new BufferedInputStream(new FileInputStream(proFilePath));
            rb = new PropertyResourceBundle(inputStream);
            inputStream.close();
        } catch (FileNotFoundException e) {
            logger.warn("外部配置文件 :{} 没找到",(fileName + ".properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rb;
    }


    //删除文件和目录
    public static void clearFiles(String workspaceRootPath) {
        File file = new File(workspaceRootPath);
        if (file.exists()) {
            deleteFile(file);
        }
    }

    public static void deleteFile(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files.length > 0) {
                for (int i = 0; i < files.length; i++) {
                    deleteFile(files[i]);
                }
            } else {
                file.delete();
            }
        }
        if (file.isFile()) {
            logger.info("文件删除成功 ：{}",file.getName());
            file.delete();
        }
    }

    //移动文件和目录
    public static void copyDir(String oldPath, String newPath) throws IOException {
        File file = new File(oldPath);
        String[] filePath = file.list();
        if (filePath != null && filePath.length > 0) {
            for (int i = 0; i < filePath.length; i++) {
                if ((new File(oldPath + file.separator + filePath[i])).isDirectory()) {
                    copyDir(oldPath + file.separator + filePath[i], newPath + file.separator + filePath[i]);
                }
                if (new File(oldPath + file.separator + filePath[i]).isFile()) {
                    copyFile(oldPath + file.separator + filePath[i], newPath + file.separator + filePath[i]);
                }
            }
        }
    }

    public static void copyFile(String oldPath, String newPath) throws IOException {
        File oldFile = new File(oldPath);
        if (oldFile.exists()) {
            File file = new File(newPath);
            if (newPath.lastIndexOf(file.separator) > 0) {
                File newFlod = new File(newPath.substring(0, newPath.lastIndexOf(file.separator)));
                if (!newFlod.exists()) {
                    newFlod.mkdirs();
                }
            }
            oldFile.renameTo(file);
        }
    }
}
