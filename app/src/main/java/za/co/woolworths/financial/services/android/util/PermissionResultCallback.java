package za.co.woolworths.financial.services.android.util;

import java.util.ArrayList;


public interface PermissionResultCallback {
	void permissionGranted(int requestCode);

	default void partialPermissionGranted(int requestCode, ArrayList<String> grantedPermissions) {

	}

	default void permissionDenied(int requestCode) {

	}

	default void neverAskAgain(int requestCode) {

	}
}
