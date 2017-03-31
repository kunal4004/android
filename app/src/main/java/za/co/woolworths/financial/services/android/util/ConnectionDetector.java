package za.co.woolworths.financial.services.android.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Handler;

import java.io.IOException;

/**
 * Created by dimitrij on 2016/12/13.
 * http://www.javased.com/?api=android.net.ConnectivityManager
 */

public class ConnectionDetector {
    // Ping google server to know if internet is available
    //http://stackoverflow.com/questions/1560788/how-to-check-internet-access-on-android-inetaddress-never-times-out
    public boolean isOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int     exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        } catch (IOException e)          { e.printStackTrace(); }
        catch (InterruptedException e) { e.printStackTrace(); }
        return false;
    }

    /**
     * We use this function before actual requests of Internet services Based on
     * http ://stackoverflow.com/questions/1560788/how-to-check-internet-access-on -android-inetaddress-never-timeouts
     */
    public boolean isOnline(Context context){
        ConnectivityManager cm=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected()) {
            return true;
        }
        else {
            return false;
        }
    }

}
