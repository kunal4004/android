package za.co.woolworths.financial.services.android.ui.views.actionsheet

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.awfs.coordination.R
import com.awfs.coordination.databinding.SignOutFragmentBinding
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.ui.activities.account.MyAccountActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.helper.LiveChatService
import za.co.woolworths.financial.services.android.util.ServiceTools

class SignOutFragment : WBottomSheetDialogFragment(), View.OnClickListener {

    private lateinit var binding: SignOutFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = SignOutFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            okaySignOutButton?.setOnClickListener(this@SignOutFragment)
            cancelSignOutButton?.setOnClickListener(this@SignOutFragment)
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.cancelSignOutButton -> {
                convertButtonBackgroundToBlack(binding.cancelSignOutButton)
                val cancelHandler: Handler? = Handler()
                cancelHandler?.postDelayed({ dismiss() }, 200)
            }

            R.id.okaySignOutButton -> {
                convertButtonBackgroundToBlack(binding.okaySignOutButton)
                val cancelHandler: Handler? = Handler()
                WoolworthsApplication.setValidatedSuburbProducts(null)
                WoolworthsApplication.setCncBrowsingValidatePlaceDetails(null)
                WoolworthsApplication.setDashBrowsingValidatePlaceDetails(null)
                cancelHandler?.postDelayed({
                    ServiceTools.stop(activity, LiveChatService::class.java)
                    dismiss()
                    onSignedOutTap() }, 200)
            }
        }
    }

    private fun onSignedOutTap() {
        if (activity is BottomNavigationActivity)
            (activity as? BottomNavigationActivity)?.onSignedOut()

        if (activity is MyAccountActivity)
            (activity as? MyAccountActivity)?.onSignedOut()

    }

    private fun convertButtonBackgroundToBlack(view: Button?) {
        view?.setBackgroundColor(Color.BLACK)
        view?.setTextColor(Color.WHITE)
    }
}