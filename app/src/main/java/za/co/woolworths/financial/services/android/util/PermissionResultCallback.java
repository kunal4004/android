package za.co.woolworths.financial.services.android.util;

import java.util.ArrayList;


public interface PermissionResultCallback {
	void permissionGranted(int request_code);

	void partialPermissionGranted(int request_code, ArrayList<String> granted_permissions);

	void permissionDenied(int request_code);

	void neverAskAgain(int request_code);
}
