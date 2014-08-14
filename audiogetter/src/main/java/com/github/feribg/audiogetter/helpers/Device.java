package com.github.feribg.audiogetter.helpers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.github.feribg.audiogetter.config.App;

/**
 * Helpers related to the device Android APIs
 */
public class Device {

    /**
     * Checks if internet access is available
     *
     * @return true if there is internet connectivity, false if not
     */
    public static Boolean hasInternetConnection() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) App.ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * Checks if the user is currently using mobile data
     *
     * @return true if currently on mobile data, false if not
     */
    public static Boolean hasMobileDataEnabled() {
        ConnectivityManager connManager = (ConnectivityManager) App.ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobile = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        return mobile != null && mobile.isConnected();
    }
}
