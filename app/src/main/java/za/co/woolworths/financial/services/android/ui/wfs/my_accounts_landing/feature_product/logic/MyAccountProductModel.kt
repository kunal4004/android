package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.logic

import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_general.stabletype.GeneralProductType
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature.enumtype.MyAccountSectionHeaderType
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_offer.data.OfferSectionModel
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_onboarding.schema.WalkThroughDataType
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.enumtype.Authenticated
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.enumtype.MyAccountAuthenticationState
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.enumtype.NotAuthenticated
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.schema.CommonItem
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.schema.ParentAccountModel
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.schema.UserAccountInformation
import za.co.woolworths.financial.services.android.util.SessionUtilities
import javax.inject.Inject

interface IMyAccountProductModel {
    fun isAuthenticated(): MyAccountAuthenticationState
    fun getUsernameAndGreetings(value: String?): UserAccountInformation
    fun listOfSignInItems(
        appVersion: String?
    ): MutableList<Any>
    fun listOfSignedOutItems(appVersion: String?): MutableList<Any>
    fun getListOfOffers(): MutableList<ParentAccountModel>
    fun listOfMyProfileItems(): MutableList<Any>
    fun listOfGeneralItems(): MutableList<Any>
    fun listOfSignedOutGeneral(): MutableList<Any>
    fun listOfOnBoardingItems(): MutableList<ParentAccountModel>
    fun applicationInfoItem(appVersion: String?): MutableList<ParentAccountModel>
}

class MyAccountProductModel @Inject constructor(private val offerSectionModel: OfferSectionModel) :
    IMyAccountProductModel {

    @Inject lateinit var myProductsProducerImpl : MyProductsProducerImpl

    override fun isAuthenticated(): MyAccountAuthenticationState =
        if (SessionUtilities.getInstance().isUserAuthenticated) Authenticated else NotAuthenticated
    override fun getUsernameAndGreetings(value: String?): UserAccountInformation {
        return UserAccountInformation(username = value)
    }
    override fun listOfSignedOutItems(appVersion: String?): MutableList<Any> {
        return (
                listOfOnBoardingItems() +
                        getListOfOffers() +
                        listOfSignedOutGeneral() +
                        applicationInfoItem(appVersion = appVersion)).toMutableList()
    }
    override fun getListOfOffers(): MutableList<ParentAccountModel> {
        return mutableListOf(
            MyAccountSectionHeaderType.MyOffers.title(),
            CommonItem.Spacer24dp,
            CommonItem.UserOffersAccount(offers = offerSectionModel.buildInitialOfferList())
        )
    }
    override fun listOfSignInItems(
        appVersion: String?
    ): MutableList<Any> {
        return listOfMyProfileItems()
            .plus(listOfGeneralItems())
            .plus(applicationInfoItem(appVersion = appVersion))
            .toMutableList()
    }
    override fun listOfMyProfileItems(): MutableList<Any> {
        return GeneralProductType.list()
    }
    override fun listOfGeneralItems(): MutableList<Any> {
        return GeneralProductType.generalProduct()
    }
    override fun listOfSignedOutGeneral(): MutableList<Any> {
        return GeneralProductType.loggedOutProduct()
    }
    override fun listOfOnBoardingItems(): MutableList<ParentAccountModel> {
        return WalkThroughDataType.list()
    }
    override fun applicationInfoItem(appVersion: String?): MutableList<ParentAccountModel> {
        return mutableListOf(
            CommonItem.UserAccountApplicationInfo(appVersion = appVersion),
            CommonItem.Spacer80dp
        )
    }
}