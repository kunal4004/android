package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.card

import com.awfs.coordination.databinding.ManageCardDetailsCardInfoLayoutBinding
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.viewmodel.MyAccountsRemoteApiViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.main.StoreCardFeatureType
import za.co.woolworths.financial.services.android.util.KotlinUtils

class BindCardInfoTypeComponent(
    private val binding: ManageCardDetailsCardInfoLayoutBinding,
    viewModel: MyAccountsRemoteApiViewModel
) {

    fun initView(){
        setCardHolderName()
    }
    fun setupView(storeCardFeatureType: StoreCardFeatureType?) {
        when (val featureType = storeCardFeatureType) {

            is StoreCardFeatureType.ActivateVirtualTempCard -> {
            }

            is StoreCardFeatureType.StoreCardIsInstantReplacementCardAndInactive ->{
            }


            is StoreCardFeatureType.StoreCardIsTemporaryFreeze ->{
            }


            is StoreCardFeatureType.TemporaryCardEnabled -> {
            }


            StoreCardFeatureType.ManageMyCard -> {}

            else -> Unit

        }
    }

    private fun setCardHolderName() {
        val name = KotlinUtils.getCardHolderNameSurname()
        val cardHolderName = KotlinUtils.capitaliseFirstWordAndLetters(name ?: "")
        binding.cardholderValueTextView.text = cardHolderName
    }
}