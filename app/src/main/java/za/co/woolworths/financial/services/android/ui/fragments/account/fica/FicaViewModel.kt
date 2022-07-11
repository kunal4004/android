package za.co.woolworths.financial.services.android.ui.fragments.account.fica

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.account.RefreshStatus
import za.co.woolworths.financial.services.android.models.dto.app_config.account_options.FicaRefresh
import za.co.woolworths.financial.services.android.ui.fragments.account.available_fund.AvailableFundFragment
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.Utils
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class FicaViewModel @Inject constructor() :ViewModel(){
    private var refreshStatus: RefreshStatus? = null
    val accountOptions = AppConfigSingleton.accountOptions
    companion object {
        const val GET_REFRESH_STATUS = "GET_REFRESH_STATUS"
    }
    fun start(intent: Intent){
        //change modal status to avoid showing it again during 24h
        Utils.sessionDaoSave(SessionDao.KEY.FICA_LAST_REQUEST_TIME, LocalDateTime.now().toString())
        refreshStatus = intent.extras?.getParcelable(GET_REFRESH_STATUS)
    }

    fun handleVerify(activity: Activity){
        (accountOptions?.ficaRefresh)?.apply {
            var isWebView = false
            when (renderMode) {
                AvailableFundFragment.WEBVIEW -> {
                    isWebView = true
                }
                AvailableFundFragment.NATIVE_BROWSER -> {
                    isWebView = false
                }
            }
            //TODO: After SC Enhancement done , Use Command to notify view to start Browser
            KotlinUtils.ficaVerifyRedirect(
                activity,
                ficaRefreshUrl + refreshStatus?.appGuid,
                isWebView, exitUrl
            )
        }
    }
}