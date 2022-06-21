package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.card

import android.content.Context
import android.view.View
import androidx.annotation.StringRes
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ManageCardDetailsCardInfoLayoutBinding
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.main.StoreCardFeatureType
import za.co.woolworths.financial.services.android.util.KotlinUtils.Companion.capitaliseFirstLetterInEveryWord

class ManageStoreCardMoreDetail(
    private val mContext: Context,
    private val binding: ManageCardDetailsCardInfoLayoutBinding
) {

    fun setupView(storeCardFeatureType: StoreCardFeatureType?) {
        when (storeCardFeatureType) {

            is StoreCardFeatureType.ActivateVirtualTempCard -> {
                setPrimaryCardLabel()
                setSubTitleLabel(R.string.card_block_desc)
            }

            is StoreCardFeatureType.StoreCardIsInstantReplacementCardAndInactive -> {
                setPrimaryCardLabel()
                setSubTitleLabel(R.string.card_block_desc)
            }

            is StoreCardFeatureType.StoreCardIsTemporaryFreeze -> {
                setPrimaryCardLabel()
                setSubTitleLabel()
            }

            is StoreCardFeatureType.TemporaryCardEnabled -> {
                setVirtualTempCardLabel()
                setSubTitleLabel()
            }

            StoreCardFeatureType.ManageMyCard -> {
                setPrimaryCardLabel()
                setSubTitleLabel()
            }

            else -> Unit

        }
    }

    private fun setSubTitleLabel(@StringRes id : Int? = null) {
        with(binding) {
            when (id == null) {
                true -> {
                    subTitleTextView.visibility = View.GONE
                    cardTitleBottomSpacer.visibility = View.VISIBLE
                    subTitleTextView.text = ""
                }
                false -> {
                    subTitleTextView.visibility = View.VISIBLE
                    cardTitleBottomSpacer.visibility = View.GONE
                    subTitleTextView.text = mContext.getString(id)
                }
            }
        }
    }

    private fun setPrimaryCardLabel() {
        binding.headerTextView.text = mContext.getString(R.string.primary_card)
    }

    private fun setVirtualTempCardLabel() {
        binding.headerTextView.text = mContext.getString(R.string.virtual_temp_card_label)
    }

    fun setCardHolderName(cardHolderName: String?) {
        binding.cardholderValueTextView.text = cardHolderName?.capitaliseFirstLetterInEveryWord()
    }
}