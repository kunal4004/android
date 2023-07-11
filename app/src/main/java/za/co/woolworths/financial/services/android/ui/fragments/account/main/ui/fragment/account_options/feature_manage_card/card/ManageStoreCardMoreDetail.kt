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

            is StoreCardFeatureType.StoreCardFreezeCardUpShellMessage -> {
                setUpshellTitleForFreezeCard()
            }

            is StoreCardFeatureType.StoreCardActivateVirtualTempCardUpShellMessage -> {
                setUpshellTitleForActivateVirtualCard()
            }

            is StoreCardFeatureType.ActivateVirtualTempCard -> {
                setPrimaryCardLabel()
                setSubTitleLabel(R.string.virtual_temp_card_detail)
            }

            is StoreCardFeatureType.StoreCardIsInstantReplacementCardAndInactive -> {
                setStoreCardLabel()
                setSubTitleLabel(R.string.card_block_desc)
            }

            is StoreCardFeatureType.StoreCardIsTemporaryFreeze -> {
                when (storeCardFeatureType.isStoreCardFrozen) {
                    true -> {
                        setFrozenPrimaryCardLabel()
                        setSubTitleLabel()
                    }
                    false -> {
                        setPrimaryCardLabel()
                        setSubTitleLabel()
                    }
                }
            }

            is StoreCardFeatureType.TemporaryCardEnabled -> {
                setVirtualTempCardLabel()
                setSubTitleLabel()
            }

            is StoreCardFeatureType.ManageMyCard -> {
                setPrimaryCardLabel()
                setSubTitleLabel()
            }

            else -> Unit

        }
    }

    private fun setSubTitleLabel(@StringRes id: Int? = null) {
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

    private fun isManageCardSubcategoryLabelVisible(isVisible: Boolean) {
        with(binding) {
            if (isVisible) {
                headerTextView.visibility = View.VISIBLE
                cardDetailDivider.visibility = View.VISIBLE
                cardDetailLinearLayout.visibility = View.VISIBLE
            } else {
                headerTextView.visibility = View.INVISIBLE
                cardDetailDivider.visibility = View.INVISIBLE
                cardDetailLinearLayout.visibility = View.INVISIBLE
            }
        }

    }

    private fun setPrimaryCardLabel() {
        binding.headerTextView.text = mContext.getString(R.string.primary_card)
    }

    private fun setFrozenPrimaryCardLabel() {
        binding.headerTextView.text = mContext.getString(R.string.frozen_primary_card)
    }

    private fun setStoreCardLabel() {
        binding.headerTextView.text = mContext.getString(R.string.store_card_title)
    }

    private fun setVirtualTempCardLabel() {
        binding.headerTextView.text = mContext.getString(R.string.virtual_temp_card_label)
    }

    fun setCardHolderName(cardHolderName: String?) {
        binding.cardholderValueTextView.text = cardHolderName?.capitaliseFirstLetterInEveryWord()
    }

    fun setUpshellTitleForFreezeCard() {
        binding.headerTextView.text = mContext.getString(R.string.upshell_message_freeze_title)
    }

    fun setUpshellTitleForActivateVirtualCard() {
        binding.headerTextView.text =
            mContext.getString(R.string.upshell_message_activate_virtual_card_title)
    }
}