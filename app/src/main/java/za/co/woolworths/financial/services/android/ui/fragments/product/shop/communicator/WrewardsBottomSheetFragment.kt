package za.co.woolworths.financial.services.android.ui.fragments.product.shop.communicator

import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import com.awfs.coordination.R
import com.awfs.coordination.databinding.FragmentWrewardsBottomSheetBinding
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.util.ScreenManager
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension

class WrewardsBottomSheetFragment(activity: FragmentActivity?) : WBottomSheetDialogFragment(),
    View.OnClickListener {

    private lateinit var binding: FragmentWrewardsBottomSheetBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWrewardsBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            savedAmountTextView.text = arguments?.get(TAG).toString()

            val alreadyHaveText: String =
                context?.getString(R.string.already_have_a_wrewards_card).toString()
            val addItNowText: String = context?.getString(R.string.add_it_now).toString()

            val wordSpan: Spannable = SpannableString(
                alreadyHaveText
                    .plus(" ")
                    .plus(addItNowText)
            )

            wordSpan.setSpan(
                StyleSpan(Typeface.BOLD),
                alreadyHaveText.length,
                wordSpan.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            addItNowButton.text = wordSpan
            addItNowButton.paintFlags = Paint.UNDERLINE_TEXT_FLAG

            signUpButton.setOnClickListener(this@WrewardsBottomSheetFragment)
            addItNowButton.setOnClickListener(this@WrewardsBottomSheetFragment)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.signUpButton -> {
                Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.CHECKOUT_WREWARD_SIGN_UP, hashMapOf(
                    FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE to
                            FirebaseManagerAnalyticsProperties.PropertyValues.ACTION_VALUE_NATIVE_CHECKOUT_WREWARDS_SIGN_UP
                ), activity)
                dismissAllowingStateLoss()
                AnimationUtilExtension.animateViewPushDown(v)
                Utils.openLinkInInternalWebView(AppConfigSingleton.wrewardsLink, true)
            }
            R.id.addItNowButton -> {
                Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.CHECKOUT_ALREADY_HAVE_WREWARD, hashMapOf(
                    FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE to
                            FirebaseManagerAnalyticsProperties.PropertyValues.ACTION_VALUE_NATIVE_CHECKOUT_WREWARDS_ADD_CARD
                ), activity)
                dismissAllowingStateLoss()
                AnimationUtilExtension.animateViewPushDown(v)
                ScreenManager.presentSSOLinkAccounts(activity)
            }
        }
    }

    companion object {
        const val TAG = "WrewardsBottomSheetFragment"
    }
}