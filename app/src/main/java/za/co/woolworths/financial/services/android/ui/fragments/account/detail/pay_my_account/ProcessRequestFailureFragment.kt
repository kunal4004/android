package za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.processing_request_failure_fragment.*
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension

class ProcessRequestFailureFragment : Fragment(), View.OnClickListener {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.processing_request_failure_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupOnClickListener()
    }

    private fun setupOnClickListener() {
//        btnRetryOnFailure?.apply {
//            setOnClickListener(this@ProcessRequestFailureFragment)
//            AnimationUtilExtension.animateViewPushDown(this)
//        }

        callCenterNumberTextView?.apply {
            setOnClickListener(this@ProcessRequestFailureFragment)
            AnimationUtilExtension.animateViewPushDown(this)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnRetryOnFailure -> {
            }
            R.id.callCenterNumberTextView -> {
            }
        }
    }

}