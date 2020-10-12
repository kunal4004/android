package za.co.woolworths.financial.services.android.util;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PermissionUtils {

	Context context;
	Activity current_activity;

	PermissionResultCallback permissionResultCallback;

	ArrayList<String> permission_list = new ArrayList<>();
	ArrayList<String> listPermissionsNeeded = new ArrayList<>();
	String dialog_content = "";
	int req_code;

	public PermissionUtils(Context context, PermissionResultCallback permissionResultCallback) {
		this.context = context;
		this.current_activity = (AppCompatActivity) context;
		this.permissionResultCallback = permissionResultCallback;
	}


	/**
	 * Check the API Level & Permission
	 *
	 * @param permissions
	 * @param dialog_content
	 * @param request_code
	 */

	public void check_permission(ArrayList<String> permissions, String dialog_content, int request_code) {
		this.permission_list = permissions;
		this.dialog_content = dialog_content;
		this.req_code = request_code;

		if (Build.VERSION.SDK_INT >= 23) {
			if (checkAndRequestPermissions(permissions, request_code)) {
				permissionResultCallback.PermissionGranted(request_code);
			}
		} else {
			permissionResultCallback.PermissionGranted(request_code);
		}

	}


	/**
	 * Check and request the Permissions
	 *
	 * @param permissions
	 * @param request_code
	 * @return
	 */

	public boolean checkAndRequestPermissions(ArrayList<String> permissions, int request_code) {

		if (permissions.size() > 0) {
			listPermissionsNeeded = new ArrayList<>();

			for (int i = 0; i < permissions.size(); i++) {
				int hasPermission = ContextCompat.checkSelfPermission(current_activity, permissions.get(i));

				if (hasPermission != PackageManager.PERMISSION_GRANTED) {
					listPermissionsNeeded.add(permissions.get(i));
				}

			}

			if (!listPermissionsNeeded.isEmpty()) {
				ActivityCompat.requestPermissions(current_activity, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), request_code);
				return false;
			}
		}

		return true;
	}

	/**
	 * @param requestCode
	 * @param permissions
	 * @param grantResults
	 */
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
		switch (requestCode) {
			case 1:
			case 2:
				if (grantResults.length > 0) {
					Map<String, Integer> perms = new HashMap<>();

					for (int i = 0; i < permissions.length; i++) {
						perms.put(permissions[i], grantResults[i]);
					}

					final ArrayList<String> pending_permissions = new ArrayList<>();

					for (int i = 0; i < listPermissionsNeeded.size(); i++) {
						if (perms.get(listPermissionsNeeded.get(i)) != PackageManager.PERMISSION_GRANTED) {
							if (ActivityCompat.shouldShowRequestPermissionRationale(current_activity, listPermissionsNeeded.get(i)))
								pending_permissions.add(listPermissionsNeeded.get(i));
							else {
								permissionResultCallback.NeverAskAgain(req_code);
								Toast.makeText(current_activity, "Go to settings and enable permissions", Toast.LENGTH_LONG).show();
								return;
							}
						}

					}

					if (pending_permissions.size() > 0) {
						permissionResultCallback.PermissionDenied(req_code);
//                         showMessageOKCancel(dialog_content,
//                                 new DialogInterface.OnClickListener() {
//                                     @Override
//                                     public void onClick(DialogInterface dialog, int which) {
//
//                                         switch (which) {
//                                             case DialogInterface.BUTTON_POSITIVE:
//                                                 check_permission(permission_list,dialog_content,req_code);
//                                                 break;
//                                             case DialogInterface.BUTTON_NEGATIVE:
//                                                 Log.i("permisson","not fully given");
//                                                 if(permission_list.size()==pending_permissions.size())
//                                                     permissionResultCallback.PermissionDenied(req_code);
//                                                 else
//                                                     permissionResultCallback.PartialPermissionGranted(req_code,pending_permissions);
//                                                 break;
//                                         }
//
//
//                                     }
//                                 });

					} else {
						permissionResultCallback.PermissionGranted(req_code);

					}
				}
				break;
		}
	}


	/**
	 * Explain why the app needs permissions
	 *
	 * @param message
	 * @param okListener
	 */
	private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
		new AlertDialog.Builder(current_activity)
				.setMessage(message)
				.setPositiveButton("Ok", okListener)
				.setNegativeButton("Cancel", okListener)
				.create()
				.show();
	}

	public static boolean hasPermissions(Context context, String... permissions) {
		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
			for (String permission : permissions) {
				if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
					return false;
				}
			}
		}
		return true;
	}

}
