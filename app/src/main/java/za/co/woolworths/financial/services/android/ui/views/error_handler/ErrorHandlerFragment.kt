package za.co.woolworths.financial.services.android.ui.views.error_handler

import android.graphics.Color
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.absa_error_fragment_layout.*
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension
import za.co.woolworths.financial.services.android.util.spannable.WSpannableStringBuilder
import za.co.woolworths.financial.services.android.util.wenum.LinkType

class ErrorHandlerFragment : Fragment(), View.OnClickListener {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.absa_error_fragment_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListener()
        descriptionBlock()
    }

    private fun descriptionBlock() {
        val descBlock1Text = WSpannableStringBuilder(getString(R.string.absa_mobile_app_pin_blocked_desc_block_1))

        with(descBlock1Text) {
            makeStringInteractable("queries@wfs.co.za", LinkType.EMAIL)
            makeStringInteractable("086 150 2020", LinkType.PHONE)
            makeChangeToStringFont("release the hold", R.font.myriad_pro_semi_bold_otf)
            makeChangeToStringFont("086 150 2020", R.font.myriad_pro_semi_bold_otf)
            makeChangeToStringFont("queries@wfs.co.za", R.font.myriad_pro_semi_bold_otf)
        }

        errorDescriptionBlock1TextView?.apply {
            descBlock1Text.build()
            movementMethod = LinkMovementMethod.getInstance()
            highlightColor = Color.TRANSPARENT
        }
    }

    private fun initListener() {
        actionButton?.apply {
            AnimationUtilExtension.animateViewPushDown(this)
            setOnClickListener(this@ErrorHandlerFragment)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.actionButton -> activity?.onBackPressed()
        }
    }
}