package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_logout

import android.app.Activity
import android.content.Intent
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.ui.activities.SSOActivity
import za.co.woolworths.financial.services.android.util.SessionUtilities
import javax.inject.Inject

interface LogoutIntent {
    fun createSignOutIntent(): Intent
    fun createSignInIntent(): Intent
    fun createRegisterIntent(): Intent
}

class LogoutIntentImpl @Inject constructor(private val activity : Activity?) : LogoutIntent {
    override fun createSignOutIntent(): Intent {
        val params = HashMap<String, String?>()
        val sessionToken: String = SessionUtilities.getInstance().sessionToken ?: ""
        params["id_token_hint"] = sessionToken
        params["post_logout_redirect_uri"] = AppConfigSingleton.ssoRedirectURILogout
        return Intent(activity, SSOActivity::class.java).apply {
            putExtra(SSOActivity.TAG_PROTOCOL, SSOActivity.Protocol.HTTPS.rawValue())
            putExtra(SSOActivity.TAG_HOST, SSOActivity.Host.STS.rawValue())
            putExtra(SSOActivity.TAG_PATH, SSOActivity.Path.LOGOUT.rawValue())
            putExtra(SSOActivity.TAG_EXTRA_QUERYSTRING_PARAMS, params)
        }
    }
    override fun createSignInIntent(): Intent {
        return Intent(activity, SSOActivity::class.java).apply {
            putExtra(SSOActivity.TAG_PROTOCOL, SSOActivity.Protocol.HTTPS.rawValue())
            putExtra(SSOActivity.TAG_HOST, SSOActivity.Host.STS.rawValue())
            putExtra(SSOActivity.TAG_PATH, SSOActivity.Path.SIGNIN.rawValue())
        }
    }
    override fun createRegisterIntent(): Intent {
        return Intent(activity, SSOActivity::class.java).apply {
            putExtra(SSOActivity.TAG_PROTOCOL, SSOActivity.Protocol.HTTPS.rawValue())
            putExtra(SSOActivity.TAG_HOST, SSOActivity.Host.STS.rawValue())
            putExtra(SSOActivity.TAG_PATH, SSOActivity.Path.REGISTER.rawValue())
        }
    }
}