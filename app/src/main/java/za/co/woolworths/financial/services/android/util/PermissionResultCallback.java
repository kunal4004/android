package za.co.woolworths.financial.services.android.util;

import java.util.ArrayList;


public interface PermissionResultCallback {
	void permissionGranted(int request_code);

	default void partialPermissionGranted(int request_code, ArrayList<String> granted_permissions) {

	}

	default void permissionDenied(int request_code) {

	}

	default void neverAskAgain(int request_code) {

	}
}
