package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.logic

import kotlinx.coroutines.flow.MutableStateFlow
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.models.dto.account.CoveredStatus
import za.co.woolworths.financial.services.android.models.dto.account.InsuranceProducts
import za.co.woolworths.financial.services.android.models.dto.account.PetInsuranceModel
import za.co.woolworths.financial.services.android.models.dto.app_config.account_options.PetInsuranceConfig
import za.co.woolworths.financial.services.android.ui.wfs.core.NetworkStatusUI
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_device_security.data.network.DeviceSecurityRemoteRepository
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_device_security.data.network.DeviceSecurityRemoteRepositoryImpl
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_pet_insurance.data.PetInsuranceDelegate
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_pet_insurance.data.PetInsuranceDelegateImpl
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_pet_insurance.data.network.PetInsuranceRemoteDataSource
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_pet_insurance.data.network.PetInsuranceRemoteDataSourceImpl
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.enumtype.AccountProductKeys
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.enumtype.AccountProductCardsGroup
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.model.UserAccountResponse
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.network.datasource.IUserAccountRemote
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.network.repository.MyAccounts
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.network.repository.MyAccountsImpl
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.network.datasource.UserAccountRemoteDataSource
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_view_application_status.ApplicationStatusView
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_view_application_status.ViewApplicationStatusImpl
import za.co.woolworths.financial.services.android.util.Utils
import javax.inject.Inject

interface MyProductsProducer {

    fun setPetInsuranceResult(
        userAccountResponse: UserAccountResponse?,
        mapOfMyProducts: MutableMap<String, AccountProductCardsGroup?>,
        model: PetInsuranceModel?,
        petAwarenessModelNotCovered : (InsuranceProducts) ->  Unit
    )


    suspend fun queryPetInsuranceRemoteService(
        _fetchPetInsuranceState: MutableStateFlow<NetworkStatusUI<PetInsuranceModel>>
    )

    fun isPetInsuranceIntroductionShown(): Boolean

    fun getInsuranceProduct(petInsuranceModel: PetInsuranceModel?): InsuranceProducts?


    fun getApplyNowState(accountNumberBin: String?): ApplyNowState?

    fun getPetInsuranceFromMobileConfig(): PetInsuranceConfig?
}

class MyProductsProducerImpl @Inject constructor(
    private val productsHandler: MyProductsHandlerImpl,
    private val status: ViewApplicationStatusImpl,
    private val pet: PetInsuranceDelegateImpl,
    private val account: MyAccountsImpl
) :
    MyProductsProducer,
    MyProductsHandler by productsHandler,
    ApplicationStatusView by status,
    PetInsuranceDelegate by pet,
    MyAccounts by account {

    override fun setPetInsuranceResult(
        userAccountResponse: UserAccountResponse?,
        mapOfMyProducts: MutableMap<String, AccountProductCardsGroup?>,
        model: PetInsuranceModel?,
        petAwarenessModelNotCovered : (InsuranceProducts) ->  Unit
    ) {
        if (model?.insuranceProducts?.isNotEmpty() == true) {
            val petInsuranceProduct = getInsuranceProduct(model)
            when (petInsuranceProduct?.statusType()) {
                CoveredStatus.NOT_COVERED -> {
                    if (isPetInsuranceIntroductionShown()) {
                        petAwarenessModelNotCovered(petInsuranceProduct)
                    }
                }
                CoveredStatus.PENDING, CoveredStatus.COVERED -> {
                    val viewApplicationStatus = mapOfMyProducts[AccountProductKeys.ViewApplicationStatus.value]
                        viewApplicationStatus?.let {
                            mapOfMyProducts -= AccountProductKeys.ViewApplicationStatus.value
                        }
                    mapOfMyProducts += AccountProductKeys.PetInsurance.value to convertProductToAccountProductCardsGroup(AccountProductKeys.PetInsurance.value, petInsuranceProduct)
                        if (status.isVisible(userAccountResponse?.products?.size) ||
                            (mapOfMyProducts.size == 1 && mapOfMyProducts[AccountProductKeys.PetInsurance.value] != null)) {
                            mapOfMyProducts += AccountProductKeys.ViewApplicationStatus.value to (viewApplicationStatus ?: AccountProductCardsGroup.ApplicationStatus())
                        }

                }
                null -> Unit
            }
        }
    }

    override fun getApplyNowState(accountNumberBin: String?): ApplyNowState? {
        return when (accountNumberBin) {
            Utils.GOLD_CARD -> ApplyNowState.GOLD_CREDIT_CARD
            Utils.BLACK_CARD -> ApplyNowState.BLACK_CREDIT_CARD
            Utils.SILVER_CARD -> ApplyNowState.SILVER_CREDIT_CARD
            else -> null
        }
    }

    override fun getPetInsuranceFromMobileConfig() = pet.getPetInsuranceConfigFromMobileConfig()

    override suspend fun queryPetInsuranceRemoteService(
        _fetchPetInsuranceState: MutableStateFlow<NetworkStatusUI<PetInsuranceModel>>
    ) = pet.queryPetInsurance(_fetchPetInsuranceState)

    override fun isPetInsuranceIntroductionShown(): Boolean {
        return pet.isPetInsuranceIntroductionShown()
    }

    override fun getInsuranceProduct(petInsuranceModel: PetInsuranceModel?): InsuranceProducts? {
        return pet.getInsuranceProduct(petInsuranceModel)
    }
}

class AccountRemoteRepository @Inject constructor(
    private val deviceSecurity: DeviceSecurityRemoteRepositoryImpl,
    private val account: UserAccountRemoteDataSource,
    private val petRemote: PetInsuranceRemoteDataSourceImpl,
) : DeviceSecurityRemoteRepository by deviceSecurity,
    IUserAccountRemote by account,
    PetInsuranceRemoteDataSource by petRemote