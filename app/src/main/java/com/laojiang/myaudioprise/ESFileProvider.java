package com.laojiang.myaudioprise;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;

import java.io.File;

/**
 *
 */

public class ESFileProvider extends android.support.v4.content.FileProvider {
    public static String getAuthorities(Context context) {
        return new StringBuffer()
                .append(context.getPackageName())
                .append(".provider")
                .toString();
    }
    public static Uri getUri(Context context, File tempFile){
        Uri uri = Build.VERSION.SDK_INT >= 24 ? FileProvider.getUriForFile(context, getAuthorities(context), tempFile) : Uri.fromFile(tempFile);
        return uri;
    }
}
