package za.co.woolworths.financial.services.android.ui.vto.ui

sealed class PermissionAction {
    object CameraPermissionsRequested : PermissionAction()
    object StoragePermissionsRequested : PermissionAction()
}