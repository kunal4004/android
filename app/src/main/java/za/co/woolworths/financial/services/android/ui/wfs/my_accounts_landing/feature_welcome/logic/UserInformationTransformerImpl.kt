package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_welcome.logic

import za.co.woolworths.financial.services.android.models.JWTDecodedModel
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.extensions.getJwtModel
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.schema.UserAccountInformation

import javax.inject.Inject

interface UserInformationTransformer {
    fun getJwt(): JWTDecodedModel?
    fun getNameAndFamilyName(): String
    fun getUsernameAndGreeting(): UserAccountInformation

    fun getRefreshIcon(): Int
}
open class UserInformationTransformerImpl @Inject constructor() : UserInformationTransformer {

    override fun getJwt(): JWTDecodedModel? = getJwtModel()


    override fun getNameAndFamilyName(): String {
        val jwtDecodedModel = getJwt()
        val name = jwtDecodedModel?.name?.firstOrNull()
        val familyName = jwtDecodedModel?.family_name?.firstOrNull()
        return "$name $familyName".uppercase()
    }

    override fun getUsernameAndGreeting(): UserAccountInformation {
        return UserAccountInformation(username = getNameAndFamilyName())
    }

    override fun getRefreshIcon(): Int {
        val item  =  getUsernameAndGreeting()
        return item.refreshIcon
    }
}