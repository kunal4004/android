package za.co.woolworths.financial.services.android.ui.activities.account.sign_in.toolbar

import android.graphics.Color
import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import com.awfs.coordination.databinding.AccountProductLandingMainFragmentBinding
import za.co.woolworths.financial.services.android.ui.extension.onClick
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.landing.AccountProductsHomeViewModel
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.KotlinUtils

class AccountProductsToolbarHelper(
    private val binding: AccountProductLandingMainFragmentBinding,
    private val fragment: Fragment?
) {
    private val mContext = fragment?.requireContext()
    private fun setToolbar(
        @StringRes title: Int,
        @ColorRes colorId: Int? = R.color.white
    ) {
        mContext ?: return
        with(binding) {
            toolbarTitleTextView.text = mContext.getString(title)
            toolbarTitleTextView.setTextColor(
                ContextCompat.getColor(
                    mContext,
                    colorId ?: R.color.white
                )
            )
            toolbarTitleTextView.visibility = View.VISIBLE
            accountInArrearsTextView.visibility = View.GONE
        }
    }

    fun setHomeLandingToolbar(viewModel: AccountProductsHomeViewModel, onTap: (View) -> Unit) {
        with(binding) {
            infoIconImageView.onClick { onTap(it) }
            navigateBackImageButton.onClick { onTap(it) }
            setNavigationIconWhite()
            setTitleTextColorWhite()
            binding.accountToolbar.setBackgroundColor(Color.TRANSPARENT)
            when (viewModel.isProductInGoodStanding()) {
                true -> {
                    toolbarTitleTextView.visibility = View.VISIBLE
                    toolbarTitleTextView.text = getString(viewModel.getTitleId())
                    accountInArrearsTextView.visibility = View.GONE
                }
                false -> {
                    toolbarTitleTextView.visibility = View.GONE
                    KotlinUtils.roundCornerDrawable(
                        accountInArrearsTextView,
                        AppConstant.RED_HEX_COLOR
                    )
                    accountInArrearsTextView.visibility = View.VISIBLE
                }
            }
        }
    }

    fun setManageMyCardDetailsToolbar(isMultipleStoreCard: Boolean, onTap: (View) -> Unit) {
        getDetailToolbar(R.string.my_card_title, if (isMultipleStoreCard) "s" else "")
        binding.navigateBackImageButton.onClick { onTap(it) }
        binding.accountToolbar.setBackgroundColor(Color.WHITE)
        setNavigationIconBlack()
        setTitleTextColorBlack()
    }

    private fun getDetailToolbar(@StringRes id: Int, formatArgs: String = "") {
        with(binding) {
            toolbarTitleTextView.text = getString(id, formatArgs)
            toolbarTitleTextView.visibility = View.VISIBLE
            accountInArrearsTextView.visibility = View.GONE
        }
    }

    private fun getString(@StringRes id: Int, formatArgs: String = "") =
        fragment?.getString(id, formatArgs)

    private fun setNavigationIconBlack() {
        binding.navigateBackImageButton.setImageResource(R.drawable.back24)
    }

    private fun setNavigationIconWhite() {
        binding.navigateBackImageButton.setImageResource(R.drawable.back_white)
    }

    private fun setTitleTextColorBlack() {
        mContext ?: return
        binding.toolbarTitleTextView.setTextColor(ContextCompat.getColor(mContext, R.color.black))
    }

    private fun setTitleTextColorWhite() {
        mContext ?: return
        binding.toolbarTitleTextView.setTextColor(ContextCompat.getColor(mContext, R.color.white))
    }

}