package za.co.woolworths.financial.services.android.ui.activities.webview.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.ui.activities.webview.usercase.IWebViewClientHandler
import za.co.woolworths.financial.services.android.ui.activities.webview.data.WebViewActions
import za.co.woolworths.financial.services.android.ui.activities.webview.data.WebViewData
import za.co.woolworths.financial.services.android.ui.activities.webview.usercase.WebViewHandler
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.Constants
import za.co.woolworths.financial.services.android.ui.wfs.core.NetworkStatusUI
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_appguid.data.AppGuidProducer
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_appguid.data.AppGuidProducerImpl
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_appguid.model.AppGUIDResponse
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_pet_insurance.data.PetInsuranceImpl
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_pet_insurance.data.PetInsurance
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_pet_insurance.data.PetInsuranceUrlIsWebviewExitUrl
import za.co.woolworths.financial.services.android.util.ChromeClient
import za.co.woolworths.financial.services.android.util.KotlinUtils
import javax.inject.Inject


@HiltViewModel
class WebViewModel @Inject constructor(val webViewClientHandler: WebViewHandler,
                                       private val appGUID: AppGuidProducerImpl,
                                       private val petInsurance : PetInsuranceImpl) :
    ViewModel(),
    IWebViewClientHandler by webViewClientHandler ,
    AppGuidProducer by appGUID,
    PetInsurance by petInsurance {

    private val _fetchAppGuidState = MutableStateFlow(NetworkStatusUI<AppGUIDResponse>())
    val fetchAppGuidState = _fetchAppGuidState.asStateFlow()

    val webViewActions: StateFlow<WebViewActions> get() = _webViewActions
    fun bundle(bundle: Bundle, chromeClient: ChromeClient) {
        webViewClientHandler.webViewData = WebViewData(
            mExternalLink = bundle.getString("externalLink")!!,
            treatmentPlan = bundle.getBoolean(KotlinUtils.TREATMENT_PLAN),
            collectionsExitUrl = bundle.getString(KotlinUtils.COLLECTIONS_EXIT_URL),
            mustRedirectBlankTargetLinkToExternal = bundle.getBoolean(
                WebViewHandler.ARG_REDIRECT_BLANK_TARGET_LINK_EXTERNAL, false
            ),
            isPetInsurance = bundle.getBoolean(Constants.IS_PET_INSURANCE, false),
            chromeClient = when (KotlinUtils.isFicaEnabled()) {
                true -> {
                    chromeClient
                }
                false -> null
            }
        )
    }

    fun handleFicaFilesCallBack(requestCode: Int, resultCode: Int, data: Intent?):Boolean{
        webViewClientHandler.webViewData?.chromeClient?.apply {
            if (KotlinUtils.isFicaEnabled()) {
                if (requestCode != ChromeClient.INPUT_FILE_REQUEST_CODE || mFilePathCallback == null) {
                    return false
                }
                var results: Array<Uri>? = null

                // Check that the response is a good one
                if (resultCode == AppCompatActivity.RESULT_OK) {
                    if (data == null || data.dataString == null) {
                        // If there is not data, then we may have taken a photo
                        if (mCameraPhotoPath != null) {
                            results = arrayOf(Uri.parse(mCameraPhotoPath))
                        }
                    } else {
                        results = arrayOf(Uri.parse(data.dataString))
                    }
                }
                mFilePathCallback!!.onReceiveValue(results)
                mFilePathCallback = null
                return true
            }
        }
        return true
    }

    fun handleRequestPermission(requestCode:Int){
        when (requestCode) {
            WebViewHandler.REQUEST_CODE -> webViewClientHandler.let {
                it.webViewData?.apply {
                    it.downloadFile(
                        downLoadUrl,
                        downLoadMimeType,
                        downLoadUserAgent,
                        downLoadConntentDisposition
                    )
                }
            }
            ChromeClient.CAMERA_REQUEST_CODE -> webViewClientHandler.webViewData?.chromeClient?.displayFile()
        }
    }

    fun navigateToPetInsuranceStatusCOVERED() {
        viewModelScope.launch { queryAppGuidRemoteService(_fetchAppGuidState) }
    }
    fun setBreakoutParams(params: PetInsuranceUrlIsWebviewExitUrl, chromeClient: ChromeClient) {
        webViewData = WebViewData(
            mExternalLink = params.first,
            treatmentPlan = false,
            collectionsExitUrl = params.third,
            mustRedirectBlankTargetLinkToExternal = false,
            isPetInsurance = true,
            chromeClient = when (KotlinUtils.isFicaEnabled()) {
                true -> chromeClient
                false -> null
            }
        )
    }
}