package com.numbyc.mywatcher.utils;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.Arrays;

/**
 * 文件处理工具类
 *
 * @author Numbyc
 * @version 1.0
 */

public class FileUtils {

    /**
     * 合并同一文件夹中的文件片段，用于处理分段上传文件
     *
     * @param packagePath     String 文件夹路径
     * @param destinctionPath String 合并成功后存储路径
     * @return boolean 返回是否合并成功
     */
    public static boolean saveFileTogether(String packagePath, String destinctionPath) {

        try {
            // 获取目标文件夹分片文件
            File file = new File(packagePath);
            File[] files = file.listFiles();


            // 按照文件名对文件进行排序
            Arrays.sort(files, (file1, file2) -> {
                if(file1 == null)
                    return -1;
                if(file2 == null)
                    return 1;
                return Integer.valueOf(file1.getName())-Integer.valueOf(file2.getName());
            });

            File resfile = new File(destinctionPath);
            FileChannel outChannel = new FileOutputStream(resfile).getChannel();

            // 合并文件分片
            FileChannel inChannel;
            for (File tempFile : files) {
                inChannel = new FileInputStream(tempFile).getChannel();
                inChannel.transferTo(0, inChannel.size(), outChannel);
                inChannel.close();
                tempFile.delete();
            }
            outChannel.close();
        } catch (Exception e) {
            return false;
        }
        return true;
    }


    /**
     * 计算指定文件的md5值
     *
     * @param filePath     String 文件路径
     * @return String 返回文件md5值
     */
    public static String getFileMd5(String filePath) {
        try {
            return DigestUtils.md5Hex(new FileInputStream(filePath));
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

}
