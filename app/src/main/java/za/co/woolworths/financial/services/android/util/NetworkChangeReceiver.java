package za.co.woolworths.financial.services.android.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Observable;

/**
 * Created by W7099877 on 2017/06/06.
 */

public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        getObservable().connectionChanged();
    }

    public static class NetworkObservable extends Observable {
        private static NetworkObservable instance = null;

        private NetworkObservable() {
            // Exist to defeat instantiation.
        }

        public void connectionChanged(){
            setChanged();
            notifyObservers();
        }

        public static NetworkObservable getInstance(){
            if(instance == null){
                instance = new NetworkObservable();
            }
            return instance;
        }
    }

    public static NetworkObservable getObservable() {
        return NetworkObservable.getInstance();
    }
}
