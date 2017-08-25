package com.efrobot.guest.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;

/**
 * 读取文件工具类
 */
public class FileUtils {
    public static String mVoicefilePath = Environment.getExternalStorageDirectory().getPath() + "/efrobot/timing";

    /**
     * 让Gallery上能马上看到该图片
     */
    public static void scanPhoto(Context ctx, String imgFileName) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File file = new File(imgFileName);
        Uri contentUri = Uri.fromFile(file);
        mediaScanIntent.setData(contentUri);
        ctx.sendBroadcast(mediaScanIntent);
    }


    /**
     * 获取网路文件在本地的存储位置
     *
     * @param voicePath 网路文件路径
     * @return 获取网路文件在本地的存储位置
     */
    public static String getVoiceLocalTimingPath(final String voicePath) {

        File f = new File(mVoicefilePath);
        if (!f.exists()) {
            f.mkdirs();
        }
        String end = voicePath.substring(voicePath.lastIndexOf("/") + 1, voicePath.length());
        String path = mVoicefilePath + "." + end;
        return path;
    }


    /**
     * 读取asset目录下文件。
     *
     * @return content
     */
    public static String readFile(Context mContext, String file, String code) {
        int len = 0;
        byte[] buf = null;
        String result = "";
        try {
            InputStream in = mContext.getAssets().open(file);
            len = in.available();
            buf = new byte[len];
            in.read(buf, 0, len);

            result = new String(buf, code);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 读取语音配置文件
     *
     * @param context
     * @return
     */
    public static ArrayList<String> getFile(Context context, String fileName) {
        try {
            ArrayList<String> list = new ArrayList<String>();
            InputStream in = context.getResources().getAssets().open(fileName);
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String str = null;

            while ((str = br.readLine()) != null) {
                if (!TextUtils.isEmpty(str)) {
                    if (str.startsWith("#")) {
                        list.set(list.size() - 1, list.get(list.size() - 1) + str);
                    } else {
                        list.add(str);
                    }
                }


            }

            return list;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 读取动作配置文件
     *
     * @param context
     * @return
     */
    public static String getActionFile(Context context, String fileName) {
        String value = "";
        try {
            ArrayList<String> list = new ArrayList<String>();
            InputStream in = context.getResources().getAssets().open(fileName);
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String str = null;
            while ((str = br.readLine()) != null) {
                if (!TextUtils.isEmpty(str)) {
                    value += str;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return value;
    }

//    /**
//     * 读取动作配置文件
//     *
//     * @param context
//     * @return
//     */
//    public static ArrayList<WordList> getLearnFile(Context context, String fileName) {
//
//        ArrayList<WordList> list = new ArrayList<WordList>();
//
//
//        try {
//            InputStream in = context.getResources().getAssets().open(fileName);
//            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
//            String str = null;
//            while ((str = br.readLine()) != null) {
//                if (!TextUtils.isEmpty(str)) {
//                    if (str.contains("#")) {
//                        WordList wordList = new WordList();
//                        ;
//                        wordList.category = str.replace("#", "");
//                        wordList.mWordArrayList = new ArrayList<Word>();
//                        list.add(wordList);
//                    } else {
//                        WordList wordList = list.get(list.size() - 1);
//
//                        String[] arr = str.split("@@");
//                        if (arr.length == 2) {
//                            wordList.mWordArrayList.add(new Word(arr[0], arr[1]));
//                        }
//
//
//                    }
//                }
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return list;
//    }


    /**
     * 获取本地文件路径
     *
     * @param parentPath 父文件夹路径
     * @param url        在线URL
     * @return 本地数据
     */
    public static String getLocalPathFromUrl(final String parentPath, final String url) {
        if (!TextUtils.isEmpty(parentPath) && !TextUtils.isEmpty(url) && url.contains("/")) {

            String end = url.substring(url.lastIndexOf("/") + 1, url.length());
            return parentPath + end;
        }
        return "";
    }

    /**
     * 判断文件夹是否存在-不存在-创建
     *
     * @param mVoicefilePath 文件路径
     */
    public static void isFile(String mVoicefilePath) throws IOException {
        File mFile = new File(mVoicefilePath);
        if (!mFile.exists()) {
            mFile.mkdirs();
        }
    }

    /**
     * 判断文件是否存在
     *
     * @param mVoicefilePath 文件路径
     */
    public static boolean isFiles(String mVoicefilePath) throws IOException {
        File mFile = new File(mVoicefilePath);
        if (!mFile.exists()) {
            return false;
        }
        return true;
    }

    /**
     * 英语跟读  删除之前的文件
     *
     * @param mFile  文件夹下的文件
     * @param string 当前文件夹的路径
     */
    public static void deleteFilePath(File mFile, String string) {
        if (mFile.exists())
            return;
        File[] files = mFile.listFiles();
        for (File f : files) {
            //删除子文件夹文件 — 不可删除有文件的文件夹
            if (!f.getPath().equals(string)) {
                for (File f_ : f.listFiles()) {
                    f_.delete();
                }
                //  删除文件夹
                f.delete();
            }
        }

    }


    public static void WriteTxtFile(String strcontent, String strFilePath) {
        //每次写入时，都换行写
        String strContent = strcontent + "\n";
        try {
            File file = new File(strFilePath);
            if (!file.exists()) {
                Log.d("TestFile", "Create the file:" + strFilePath);
                file.createNewFile();
            }
            RandomAccessFile raf = new RandomAccessFile(file, "rw");
            raf.seek(file.length());
            raf.write(strContent.getBytes());
            raf.close();
        } catch (Exception e) {
            Log.e("TestFile", "Error on write File.");
        }
    }

    /**
     * 获取指定文件大小
     *
     * @param file
     * @return
     * @throws Exception
     */
    public static long getFileSize(File file) throws Exception {
        long size = 0;
        if (file.exists()) {
            FileInputStream fis = null;
            fis = new FileInputStream(file);
            size = fis.available();
        } else {
            file.createNewFile();
            Log.e("获取文件大小", "文件不存在!");
        }
        return size;
    }


    /***
     * 删除文件
     *
     * @param filePath2 文件路径
     */
    public static void delete(String filePath2) {
        try {
            File mFile = new File(filePath2);
            if (mFile.exists()) {
                mFile.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
