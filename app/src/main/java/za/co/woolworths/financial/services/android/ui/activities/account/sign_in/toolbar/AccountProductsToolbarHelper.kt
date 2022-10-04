package za.co.woolworths.financial.services.android.ui.activities.account.sign_in.toolbar

import android.graphics.Color
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import com.awfs.coordination.databinding.AccountProductLandingToolbarViewBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.landing.AccountProductsHomeViewModel
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.KotlinUtils

class AccountProductsToolbarHelper(
    private val binding: AccountProductLandingToolbarViewBinding,
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
            toolbarTitleTextView.visibility = VISIBLE
            accountInArrearsTextView.visibility = GONE
        }
    }

    fun setOnAccountInArrearsTapListener(onTap: (View) -> Unit) {
        binding.accountInArrearsTextView.setOnClickListener { onTap(binding.accountInArrearsTextView) }
    }
    fun setHomeLandingToolbar(viewModel: AccountProductsHomeViewModel, onTap: (View) -> Unit) {
        with(binding) {
            infoIconImageView.setOnClickListener { onTap(it) }
            binding.infoIconImageView.visibility = VISIBLE
            binding.horizontalDivider.visibility = GONE
            binding.closeIconImageButton.visibility = GONE
            binding.navigateBackImageButton.visibility = VISIBLE
            navigateBackImageButton.setOnClickListener {
                if (viewModel.bottomSheetBehaviorState == BottomSheetBehavior.STATE_EXPANDED){
                    viewModel.setIsBottomSheetBehaviorExpanded(true)
                    return@setsetOnClickListenerListener
                }
                onTap(it)
            }
            setNavigationIconWhite()
            setTitleTextColorWhite()
            binding.accountProductLandingToolbarView.setBackgroundColor(Color.TRANSPARENT)
            when (viewModel.isProductInGoodStanding()) {
                true -> {
                    toolbarTitleTextView.visibility = VISIBLE
                    toolbarTitleTextView.text = getString(viewModel.getTitleId())
                    accountInArrearsTextView.visibility = GONE
                }
                false -> {
                    toolbarTitleTextView.visibility = GONE
                    KotlinUtils.roundCornerDrawable(
                        accountInArrearsTextView,
                        AppConstant.RED_HEX_COLOR
                    )
                    accountInArrearsTextView.visibility = VISIBLE
                }
            }
        }
    }

    fun setManageMyCardDetailsToolbar(isMultipleStoreCard: Boolean, onTap: (View) -> Unit) {
        getDetailToolbar(R.string.my_card, if (isMultipleStoreCard) "s" else "")
        binding.navigateBackImageButton.rotation = 0f
        binding.navigateBackImageButton.setOnClickListener { onTap(it) }
        binding.navigateBackImageButton.visibility = VISIBLE
        binding.horizontalDivider.visibility = VISIBLE
        binding.infoIconImageView.visibility = GONE
        binding.closeIconImageButton.visibility = GONE
        binding.accountProductLandingToolbarView.setBackgroundColor(Color.WHITE)
        setNavigationIconBlack()
        setTitleTextColorBlack()
    }

    private fun getDetailToolbar(@StringRes id: Int, formatArgs: String = "") {
        with(binding) {
            toolbarTitleTextView.text = getString(id, formatArgs)
            toolbarTitleTextView.visibility = VISIBLE
            accountInArrearsTextView.visibility = GONE
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

    fun getBackIcon() = binding.navigateBackImageButton

    fun setInformationToolbar(onTap: (View) -> Unit) {
        getDetailToolbar(R.string.information)
        binding.navigateBackImageButton.rotation = 0f
        binding.infoIconImageView.setOnClickListener { onTap(it) }
        binding.closeIconImageButton.setOnClickListener { onTap(it) }
        binding.horizontalDivider.visibility = VISIBLE
        binding.navigateBackImageButton.visibility = GONE
        binding.infoIconImageView.visibility = GONE
         binding.closeIconImageButton.visibility = VISIBLE
        binding.accountProductLandingToolbarView.setBackgroundColor(Color.WHITE)
        setNavigationIconBlack()
        setTitleTextColorBlack()
    }

    fun setCardNotReceivedToolbar(onTap: (View) -> Unit) {
        binding.apply {
            navigateBackImageButton.rotation = 0f
            toolbarTitleTextView.visibility = GONE
            infoIconImageView.setOnClickListener { onTap(it) }
            closeIconImageButton.setOnClickListener { onTap(it) }
            horizontalDivider.visibility = GONE
            navigateBackImageButton.visibility = GONE
            infoIconImageView.visibility = GONE
            closeIconImageButton.visibility = VISIBLE
            accountProductLandingToolbarView.setBackgroundColor(Color.WHITE)
        }
        setNavigationIconBlack()
        setTitleTextColorBlack()
    }

    fun hideCloseIcon(){
        binding.closeIconImageButton.visibility = GONE
    }

}