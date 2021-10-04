package za.co.woolworths.financial.services.android.ui.vto.ui

public interface PfSDKInitialCallback {
    fun onInitialized()
    fun onFailure(throwable: Throwable?)

}