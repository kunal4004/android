package za.co.woolworths.financial.services.android.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkManager {

	private static NetworkManager instance;
	public static NetworkManager getInstance(){
		if (instance == null){
			instance = new NetworkManager();
		}

		return instance;
	}


	public boolean isConnectedToNetwork(Context context) {
		boolean haveConnectedWifi = false;
		boolean haveConnectedMobile = false;

		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo[] netInfo = cm.getAllNetworkInfo();
		for (NetworkInfo ni : netInfo) {
			if ("WIFI".equalsIgnoreCase(ni.getTypeName()))
				if (ni.isConnected())
					haveConnectedWifi = true;
			if ("MOBILE".equalsIgnoreCase(ni.getTypeName()))
				if (ni.isConnected())
					haveConnectedMobile = true;
		}
		return haveConnectedWifi || haveConnectedMobile;
	}
}
