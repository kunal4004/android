package za.co.woolworths.financial.services.android.ui.fragments.card

import android.graphics.Paint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.link_card_fragment.*
import za.co.woolworths.financial.services.android.ui.extension.replaceFragment
import za.co.woolworths.financial.services.android.util.CreditCardTextWatcher

class LinkCardFragment : MyCardExtension() {

    companion object {
        fun newInstance() = LinkCardFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.link_card_fragment, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvReplacementCardInfo?.apply {
            paintFlags = Paint.UNDERLINE_TEXT_FLAG
            setOnClickListener {
                replaceFragment(
                        fragment = GetReplacementCardFragment.newInstance(),
                        tag = GetReplacementCardFragment::class.java.simpleName,
                        containerViewId = R.id.flMyCard,
                        allowStateLoss = true,
                        enterAnimation = R.anim.slide_in_from_right,
                        exitAnimation = R.anim.slide_to_left,
                        popEnterAnimation = R.anim.slide_from_left,
                        popExitAnimation = R.anim.slide_to_right
                )
            }
        }
        inputTextWatcher()
        tappedEvent()
    }

    private fun tappedEvent() {
        imNavigateToOTPFragment?.setOnClickListener {
            if (imNavigateToOTPFragment?.alpha == 1.0f) {
                replaceFragment(
                        fragment = EnterOtpFragment.newInstance(),
                        tag = EnterOtpFragment::class.java.simpleName,
                        containerViewId = R.id.flMyCard,
                        allowStateLoss = true,
                        enterAnimation = R.anim.slide_in_from_right,
                        exitAnimation = R.anim.slide_to_left,
                        popEnterAnimation = R.anim.slide_from_left,
                        popExitAnimation = R.anim.slide_to_right
                )
            }
        }
    }

    private fun inputTextWatcher() {
        etCardNumber?.apply {
            addTextChangedListener(object : CreditCardTextWatcher(this) {

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    super.onTextChanged(s, start, before, count)
                    imNavigateToOTPFragment?.alpha = if (etCardNumber?.length() == 19) 1.0f else 0.5f
                }
            })
        }
    }

    override fun onResume() {
        super.onResume()
        activity?.let {
            etCardNumber?.apply {
                requestFocus()
                showSoftKeyboard(it, this)
            }
        }
    }
}