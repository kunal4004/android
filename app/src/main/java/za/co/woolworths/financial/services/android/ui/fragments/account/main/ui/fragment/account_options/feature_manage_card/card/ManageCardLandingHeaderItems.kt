package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.card

import android.graphics.Paint
import android.view.View
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import com.awfs.coordination.databinding.AccountOptionsManageCardFragmentBinding
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.viewmodel.MyAccountsRemoteApiViewModel
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.viewmodel.StoreCardInfo
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.main.StoreCardFeatureType
import za.co.woolworths.financial.services.android.util.KotlinUtils

/***
 * Class to manage my card label, manage my card label and badge
 */
class ManageCardLandingHeaderItems(
    private val viewModel: MyAccountsRemoteApiViewModel,
    private val binding: AccountOptionsManageCardFragmentBinding,
    private val fragment: Fragment?
) {

    val mContext = fragment?.requireContext()

    fun setCardLabel() {
        mContext ?: return
        when (viewModel.dataSource.isMultipleStoreCardEnabled()) {
            true -> {
                binding.cardText.text = getString(R.string.my_card, "s")
                binding.manageCardText.text = getString(R.string.manage_my_card_title, "s")
            }
            false -> {
                binding.cardText.text = getString(R.string.my_card, "")
                binding.manageCardText.text = getString(R.string.manage_my_card_title, "")
            }
        }
    }

    private fun manageCardLabelVisibility(isVisible: Boolean, isLabelUnderline: Boolean = false) {
        binding.manageCardText.visibility = if (isVisible) View.VISIBLE else View.INVISIBLE
        binding.manageCardText.paintFlags = if (isLabelUnderline) binding.manageCardText.paintFlags or Paint.UNDERLINE_TEXT_FLAG else 0
    }

    private fun myCardLabelVisibility(isVisible: Boolean) {
        binding.cardText.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    fun setBadge(
        @StringRes tagTitleId: Int,
        @StringRes tagColor: Int,
        isVisible: Boolean) {
        if (isVisible) {
            binding.storeCardTagTextView.text = bindString(tagTitleId)
            KotlinUtils.roundCornerDrawable(binding.storeCardTagTextView, bindString(tagColor))
            binding.storeCardTagTextView.visibility = View.VISIBLE
        } else {
            binding.storeCardTagTextView.visibility = View.GONE
        }
    }

    fun getString(@StringRes id: Int, formatArgs: String = "") = fragment?.getString(id, formatArgs)

    fun showHeaderItem(storeCardFeatureType: StoreCardInfo) {
        myCardLabelVisibility(false)
        manageCardLabelVisibility(isVisible = false, isLabelUnderline = false)
        when (val featureType = storeCardFeatureType.feature) {

            is StoreCardFeatureType.ActivateVirtualTempCard -> {
                myCardLabelVisibility(true)
                manageCardLabelVisibility(isVisible = true, isLabelUnderline = true)
            }

            is StoreCardFeatureType.StoreCardIsInstantReplacementCardAndInactive ->{
                myCardLabelVisibility(true)
                manageCardLabelVisibility(isVisible = true, isLabelUnderline = true)
            }

            is StoreCardFeatureType.StoreCardIsTemporaryFreeze -> {
                when (featureType.isStoreCardFrozen) {
                    true -> {
                       // setBadge(R.string.freeze_temp_label, R.string.orange_tag, false)
                        manageCardLabelVisibility(isVisible = true, isLabelUnderline = true)
                        myCardLabelVisibility(true)
                    }
                    false ->{
                      //  setBadge(R.string.freeze_temp_label, R.string.orange_tag, false)
                        manageCardLabelVisibility(isVisible = true, isLabelUnderline = true)
                        myCardLabelVisibility(true)
                    }
                }
            }

            is StoreCardFeatureType.TemporaryCardEnabled -> {
             //   setBadge(R.string.temp_card, R.string.orange_tag, false)
                manageCardLabelVisibility(isVisible = true, isLabelUnderline = true)
                myCardLabelVisibility(true)
            }

            is StoreCardFeatureType.ManageMyCard -> {
//                setBadge(R.string.inactive, R.string.red_tag, false)
                manageCardLabelVisibility(isVisible = true, isLabelUnderline = true)
                myCardLabelVisibility(true)
            }

            else -> Unit

        }
    }
}