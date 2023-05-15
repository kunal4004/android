package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_fica.data.network

import android.app.Activity
import android.content.Intent
import com.awfs.coordination.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.account.FicaModel
import za.co.woolworths.financial.services.android.models.dto.account.RefreshStatus
import za.co.woolworths.financial.services.android.ui.fragments.account.fica.FicaActivity
import za.co.woolworths.financial.services.android.ui.fragments.account.fica.FicaViewModel
import za.co.woolworths.financial.services.android.ui.wfs.core.NetworkStatusUI
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_fica.data.FicaRemoteDataSource
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_fica.data.FicaRemoteDataSourceImpl
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.Utils
import javax.inject.Inject

interface FicaProducer {
    fun isFicaInAppConfigEnabled(): Boolean
    fun isFicaEnabled(): Boolean
    fun hasADayPassed(): Boolean
    fun handleResult(fica : FicaModel?, activity: Activity?)
    fun createFicaIntent(activity: Activity?, refreshStatus : RefreshStatus)
    suspend fun getFicaRemoteService(_state: MutableSharedFlow<NetworkStatusUI<FicaModel>>)
}

class FicaProducerImpl @Inject constructor(private val ficaRemote : FicaRemoteDataSourceImpl)
    : FicaProducer, FicaRemoteDataSource by ficaRemote {

    override fun isFicaInAppConfigEnabled(): Boolean {
        return Utils.isFeatureEnabled(AppConfigSingleton.accountOptions?.ficaRefresh?.minimumSupportedAppBuildNumber)
    }

    override fun isFicaEnabled(): Boolean {
        return hasADayPassed() && isFicaInAppConfigEnabled()
    }

    override fun hasADayPassed() = runBlocking {
        withContext(Dispatchers.IO) {  KotlinUtils.hasADayPassed(Utils.getSessionDaoValue(SessionDao.KEY.FICA_LAST_REQUEST_TIME))}
    }

    override fun handleResult(fica : FicaModel?, activity: Activity?) {
        if (fica?.refreshStatus?.refreshDue == true) {
            createFicaIntent(activity = activity, refreshStatus =fica.refreshStatus)
        }
    }

    override fun createFicaIntent(activity: Activity?, refreshStatus: RefreshStatus) {
        activity?.apply {
            val intent = Intent(this, FicaActivity::class.java)
            intent.putExtra(FicaViewModel.GET_REFRESH_STATUS, refreshStatus)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left)
        }
    }

    override suspend fun getFicaRemoteService(_state: MutableSharedFlow<NetworkStatusUI<FicaModel>>) {
        if (isFicaEnabled()) {
            ficaRemote.queryFicaRemoteService().collect { result -> _state.emit(result) }
        }
    }

}