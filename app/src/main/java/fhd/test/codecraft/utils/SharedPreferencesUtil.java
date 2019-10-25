package fhd.test.codecraft.utils;

import android.content.Context;

public class SharedPreferencesUtil {

    private static final String USER_ID = "userId";
    private static final String APP_NAME = "CodeCraft";
    private static final String LOC_LAT = "lat";
    private static final String LOC_LNG = "lng";

    private SharedPreferencesUtil()
    {
    }

    private static android.content.SharedPreferences getSharedPreferences(Context context)
    {
        return context.getSharedPreferences(APP_NAME , Context.MODE_PRIVATE );
    }


    public static double getLat(Context context)
    {
        return getSharedPreferences(context).getFloat(LOC_LAT, 0.0f);
    }

    public static void setLat(Context context, double lat)
    {
        getSharedPreferences(context).edit().putFloat(LOC_LAT, (float) lat).commit();
    }

    public static double getLng(Context context)
    {
        return getSharedPreferences(context).getFloat(LOC_LNG, 0.0f);
    }

    public static void setLng(Context context, double lng)
    {
        getSharedPreferences(context).edit().putFloat(LOC_LNG, (float) lng).commit();
    }
}
