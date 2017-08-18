package za.co.woolworths.financial.services.android.ui.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.LocationResponse;
import za.co.woolworths.financial.services.android.models.dto.WGlobalState;
import za.co.woolworths.financial.services.android.util.FusedLocationSingleton;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.Utils;


public class LocateProductActivity extends AppCompatActivity {

	public static final int PERMS_REQUEST_CODE = 123;
	private Location mLocation;
	public WGlobalState mWGlobalState;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mWGlobalState = ((WoolworthsApplication) LocateProductActivity.this.getApplication()).getWGlobalState();
	}

	public void startLocationUpdates() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (checkLocationPermission()) {
				if (ContextCompat.checkSelfPermission(LocateProductActivity.this,
						Manifest.permission.ACCESS_FINE_LOCATION)
						== PackageManager.PERMISSION_GRANTED) {
					FusedLocationSingleton.getInstance().startLocationUpdates();
					// register observer for location updates
					LocalBroadcastManager.getInstance(LocateProductActivity.this).registerReceiver(mLocationUpdated,
							new IntentFilter(FusedLocationSingleton.INTENT_FILTER_LOCATION_UPDATE));
				}
			} else {
				checkLocationPermission();
			}
		}
	}

	public void stopLocationUpdate() {
		// stop location updates
		FusedLocationSingleton.getInstance().stopLocationUpdates();
		// unregister observer
		LocalBroadcastManager.getInstance(LocateProductActivity.this).unregisterReceiver(mLocationUpdated);
	}

	@RequiresApi(api = Build.VERSION_CODES.M)
	public boolean checkLocationPermission() {
		if (ContextCompat.checkSelfPermission(LocateProductActivity.this,
				Manifest.permission.ACCESS_FINE_LOCATION)
				!= PackageManager.PERMISSION_GRANTED) {
			if (shouldShowRequestPermissionRationale(
					Manifest.permission.ACCESS_FINE_LOCATION)) {
				requestPermissions(
						new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
						PERMS_REQUEST_CODE);
			} else {
				//we can request the permission.
				requestPermissions(
						new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
						PERMS_REQUEST_CODE);
			}
			return false;
		} else {
			return true;
		}
	}

	/**
	 * handle new location
	 */
	private BroadcastReceiver mLocationUpdated = new BroadcastReceiver() {
		@RequiresApi(api = Build.VERSION_CODES.M)
		@Override
		public void onReceive(Context context, final Intent intent) {
			try {
				mLocation = intent.getParcelableExtra(FusedLocationSingleton.LBM_EVENT_LOCATION_UPDATE);
				Utils.saveLastLocation(mLocation, LocateProductActivity.this);
				locationAPIRequest();
				stopLocationUpdate();
			} catch (Exception ignored) {
			}
		}
	};


	private void locationAPIRequest() {
		init().execute();
	}

	public HttpAsyncTask<String, String, LocationResponse> init() {
		return new HttpAsyncTask<String, String, LocationResponse>() {
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
			}

			@Override
			protected LocationResponse httpDoInBackground(String... params) {
				return ((WoolworthsApplication) LocateProductActivity.this.getApplication()).getApi().getLocationsItem("mSkuID", String.valueOf(mWGlobalState.getStartRadius()), String.valueOf(mWGlobalState.getEndRadius()));
			}

			@Override
			protected Class<LocationResponse> httpDoInBackgroundReturnType() {
				return LocationResponse.class;
			}

			@Override
			protected LocationResponse httpError(final String errorMessage, HttpErrorCode httpErrorCode) {
				return new LocationResponse();
			}

			@Override
			protected void onPostExecute(LocationResponse locationResponse) {
				super.onPostExecute(locationResponse);
//				storeDetailsList = locationResponse.Locations;
//				if (storeDetailsList != null) {
//					if (storeDetailsList.size() > 0) {
//						selectPage(currentPosition);
//					} else {
//					}
//				}
			}
		};
	}
}
