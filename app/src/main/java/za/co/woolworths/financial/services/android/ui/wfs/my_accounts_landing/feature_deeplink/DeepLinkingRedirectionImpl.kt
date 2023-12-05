package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_deeplink

import android.os.Bundle
import android.text.TextUtils
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import za.co.woolworths.financial.services.android.models.dto.account.AccountsProductGroupCode
import za.co.woolworths.financial.services.android.models.dto.account.AccountsProductGroupCode.Companion.getEnum
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import javax.inject.Inject

interface DeepLinkingRedirection {
    fun parseDeepLinkData(bundle: Bundle?): JsonObject?

    fun parseDeepLinkDataAsString(bundle: Bundle?): String?

    fun validateDeepLinkData(deepLinkParams: JsonObject?): AccountsProductGroupCode?
}

class DeepLinkingRedirectionImpl @Inject constructor() : DeepLinkingRedirection {

    override fun parseDeepLinkData(bundle: Bundle?): JsonObject? {
        bundle ?: return null

        val data: String = bundle.getString("parameters", "").replace("\\", "")
        if (TextUtils.isEmpty(data)) {
            return null
        }
        var deepLinkParams: JsonObject? = null
        try {
            deepLinkParams = Gson().fromJson(data, JsonObject::class.java)
            deepLinkParams?.addProperty("feature", bundle.getString("feature"))
        } catch (e: JsonSyntaxException) {
            FirebaseManager.logException(e)
        }
        return deepLinkParams
    }

    override fun parseDeepLinkDataAsString(bundle: Bundle?): String? {
        return Utils.objectToJson(parseDeepLinkData(bundle))
    }

    override fun validateDeepLinkData(deepLinkParams: JsonObject?): AccountsProductGroupCode? {
        if (deepLinkParams?.get("productGroupCode") == null || !SessionUtilities.getInstance().isUserAuthenticated) {
            return null
        }
        val productGroupCode: String? = deepLinkParams.get("productGroupCode")?.asString
        return getEnum(productGroupCode)
    }
}