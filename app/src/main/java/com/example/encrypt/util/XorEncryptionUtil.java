package com.example.encrypt.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by ruipan.dong on 2017/9/13.
 */

public class XorEncryptionUtil {

    private static int REVERSE_LENGTH = 5;

    /**
     * 大文件 加、解密的一种方案：通过内存映射文件MappedByteBuffer对文件的前REVERSE_LENGTH长度的字节与下标做异或运算
     * 加密完成后，将私密文件copy到私密目录下
     *
     * @param sourceFilePath 原文件绝对路径
     * @param destFilePath   目标文件绝对路径
     * @return
     */
    public static boolean encrypt(String sourceFilePath, String destFilePath) {
        int len = REVERSE_LENGTH;
        try {
            File f = new File(sourceFilePath);
            RandomAccessFile raf = new RandomAccessFile(f, "rw");
            long totalLen = raf.length();

            if (totalLen < REVERSE_LENGTH)
                len = (int) totalLen;

            FileChannel channel = raf.getChannel();
            MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, REVERSE_LENGTH);
            byte tmp;
            for (int i = 0; i < len; ++i) {
                byte rawByte = buffer.get(i);
                tmp = (byte) (rawByte ^ i);
                buffer.put(i, tmp);
            }
            buffer.force();
            buffer.clear();
            channel.close();
            raf.close();
            //上面的加密步骤完成后，将加密后的文件copy到私密目录下
            if (null != destFilePath) {//如果目标路径不为空，那么表示需要copyFile
                return copyFile(sourceFilePath, destFilePath);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static File encryptToFile(String sourceFilePath, String destFilePath) {
        File destFile = new File(destFilePath);
        int len = REVERSE_LENGTH;
        try {
            File f = new File(sourceFilePath);
            RandomAccessFile raf = new RandomAccessFile(f, "rw");
            long totalLen = raf.length();

            if (totalLen < REVERSE_LENGTH)
                len = (int) totalLen;

            FileChannel channel = raf.getChannel();
            MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, REVERSE_LENGTH);
            byte tmp;
            for (int i = 0; i < len; ++i) {
                byte rawByte = buffer.get(i);
                tmp = (byte) (rawByte ^ i);
                buffer.put(i, tmp);
            }
            buffer.force();
            buffer.clear();
            channel.close();
            raf.close();
            if (null != destFilePath) {//如果目标路径不为空，那么表示需要copyFile
                copyFile(sourceFilePath, destFilePath);
                return destFile;
            }
            return destFile;
        } catch (Exception e) {
            e.printStackTrace();
            return destFile;
        }
    }

    /**
     * 复制文件(FileChannel的transferTo()方法比一般的文件复制速度快很多)
     *
     * @return 实际复制的字节数，如果文件、目录不存在、文件为null或者发生IO异常，返回-1
     */
    public static boolean copyFile(String srcFilePath, String destFilePath) {
        //long l = System.currentTimeMillis();
        long size = 0;//真实复制的字节长度
        long length = -1;//原文件的字节长度
        File srcFile = new File(srcFilePath);
        File destFile = new File(destFilePath);
        File destDir = new File(destFile.getParent());//根据destFile
        try {
            RandomAccessFile raf = new RandomAccessFile(srcFile, "rw");
            length = raf.length();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!srcFile.exists()) {
            return false;
        } else if (!destDir.exists()) {
            destDir.mkdirs();
        } else {
            try {
                FileChannel fcin = new FileInputStream(srcFile).getChannel();
                FileChannel fcout = new FileOutputStream(new File(destDir, srcFile.getName())).getChannel();
                size = fcin.size();
                fcin.transferTo(0, fcin.size(), fcout);
                fcin.close();
                fcout.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //Log.d("XorEncryptionUtil", "length%%%%%%%%%:" + length);
        //Log.d("XorEncryptionUtil", "size%%%%%%%%%%%:" + size);
        //long l1 = System.currentTimeMillis();
        //Log.d("XorEncryptionUtil", "copy 文件耗时(l1-l):" + (l1 - l));
        return length == size;
    }



}
