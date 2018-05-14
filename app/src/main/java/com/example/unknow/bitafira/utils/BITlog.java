package com.example.unknow.bitafira.utils;

import android.app.Application;
import android.content.Context;

/**
 *
 * @author bgamecho
 *
 */
public class BITlog extends Application {

    public static String MAC="20:17:09:18:58:37";
    public static int SamplingRate=1000;
    public static int frameRate= 100;
    public static int[] channels = {1};

    public static boolean digitalOutputs = false;

    private static Context mContext;

    public static Context getContext() {
        return mContext;
    }

    public static void setContext(Context mContext) {
        BITlog.mContext = mContext;
    }

}

