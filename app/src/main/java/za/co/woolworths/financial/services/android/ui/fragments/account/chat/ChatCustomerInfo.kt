package za.co.woolworths.financial.services.android.ui.fragments.account.chat

import za.co.woolworths.financial.services.android.models.JWTDecodedModel
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.SessionUtilities

class ChatCustomerInfo {

    private var jWTDecodedModel: JWTDecodedModel? = null

    init {
        jWTDecodedModel = SessionUtilities.getInstance().jwt
    }

    fun getCustomerFamilyName(): String {
        val familyName = jWTDecodedModel?.family_name?.get(0)
        return KotlinUtils.firstLetterCapitalization(familyName) ?: ""
    }

    fun getCustomerUsername(): String {
        val username = jWTDecodedModel?.name?.get(0)
        return KotlinUtils.firstLetterCapitalization(username) ?: ""
    }

    fun getCustomerEmail() = jWTDecodedModel?.email?.get(0) ?: ""

    fun getCustomerC2ID() = jWTDecodedModel?.C2Id ?: ""

    fun getUsername(): String? {
        //logged in user's name and family name will be displayed on the page
        val name = jWTDecodedModel?.name?.get(0) ?: ""
        return KotlinUtils.firstLetterCapitalization(name)
    }

}