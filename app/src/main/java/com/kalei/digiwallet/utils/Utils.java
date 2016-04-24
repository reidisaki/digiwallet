package com.kalei.digiwallet.utils;

import android.os.Environment;

import java.io.File;

public class Utils {

    public static boolean isSDCardMounted() {
        boolean isMounted = false;
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            isMounted = true;
        } else if (Environment.MEDIA_BAD_REMOVAL.equals(state)) {
            isMounted = false;
        } else if (Environment.MEDIA_CHECKING.equals(state)) {
            isMounted = false;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            isMounted = false;
        } else if (Environment.MEDIA_NOFS.equals(state)) {
            isMounted = false;
        } else if (Environment.MEDIA_REMOVED.equals(state)) {
            isMounted = false;
        } else if (Environment.MEDIA_UNMOUNTABLE.equals(state)) {
            isMounted = false;
        } else if (Environment.MEDIA_UNMOUNTED.equals(state)) {
            isMounted = false;
        }
        return isMounted;
    }

    public static boolean isDirectoryExists(final String filePath) {
        boolean isDirectoryExists = false;
        File mFilePath = new File(filePath);
        if (mFilePath.exists()) {
            isDirectoryExists = true;
        } else {
            isDirectoryExists = mFilePath.mkdirs();
        }
        return isDirectoryExists;
    }

    public static boolean deleteFile(final String filePath) {
        boolean isFileExists = false;
        File mFilePath = new File(filePath);
        if (mFilePath.exists()) {
            mFilePath.delete();
            isFileExists = true;
        }
        return isFileExists;
    }

    public static String getDataPath() {
        String returnedPath = "";
        final String mDirName = "tesseract";
        final String mDataDirName = "tessdata";
        if (isSDCardMounted()) {
            final String mSDCardPath = Environment.getExternalStorageDirectory() + File.separator + mDirName;
            if (isDirectoryExists(mSDCardPath)) {
                final String mSDCardDataPath = Environment.getExternalStorageDirectory() + File.separator + mDirName +
                        File.separator + mDataDirName;
                isDirectoryExists(mSDCardDataPath);
                return mSDCardPath;
            }
        }
        return returnedPath;
    }
}