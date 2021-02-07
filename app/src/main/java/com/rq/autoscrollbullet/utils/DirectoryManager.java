package com.rq.autoscrollbullet.utils;

import android.content.Context;
import android.os.Environment;

import java.io.File;

public class DirectoryManager {

    private static final String DIR_CACHE = "app-cache";

    private DirectoryManager(){}

    /**
     * 获取该应用下的缓存路径
     *
     * @param fileName 目录名称
     * @return 返回APP
     */
    public static String getAPPFilesDir(Context context, String fileName) {
        String diskCache = getAvailableCacheDirectoryPath(context);
        File file = new File(diskCache, fileName);
        if (!file.exists()) {
            file.mkdirs();
        }
        diskCache = file.getAbsolutePath();
        return diskCache;
    }

    /**
     * 获取该应用的缓存路径
     * @param context
     * @return
     */
    public static File getAPPCacheDirectory(Context context){
        return new File(getAPPCacheDirectoryPath(context));
    }

    /**
     * 获取该应用的缓存路径
     * @param context
     * @return
     */
    public static String getAPPCacheDirectoryPath(Context context){
        return getAvailableCacheDirectoryPath(context);
    }

    /**
     * 获取可用的缓存路径，如果存在外部存储，返回外部存储路径，否则返回内部存储路径
     */
    private static String getAvailableCacheDirectoryPath(Context context) {
        String path = getExternalCacheDirectoryPath(context);
        if (path != null) {
            return path;
        }
        return getInternalCacheDirectoryPath(context);
    }

    /**
     * 获取外部存储的缓存路径
     *
     * @return
     */
    private static String getExternalCacheDirectoryPath(Context context) {
        if (FileTools.existSDCard()){
            File rootFile = Environment.getExternalStorageDirectory();
            if (rootFile != null && rootFile.exists()) {
                String externalPath = context.getExternalFilesDir(null).getAbsolutePath();
                File cacheFile = new File(externalPath, DIR_CACHE);
                String cacheDir = cacheFile.getAbsolutePath();
                rootFile = new File(cacheDir);
                if (!rootFile.exists()) {
                    rootFile.mkdirs();
                }
                return cacheDir;
            }
        }
        return null;
    }

    /**
     * 获取内部存储的缓存路径
     *
     * @return
     */
    private static String getInternalCacheDirectoryPath(Context context) {
        File cacheFile = context.getDir(DIR_CACHE, Context.MODE_PRIVATE);
        if (!cacheFile.exists()) {
            cacheFile.mkdirs();
        }
        return cacheFile.getPath();
    }
}
