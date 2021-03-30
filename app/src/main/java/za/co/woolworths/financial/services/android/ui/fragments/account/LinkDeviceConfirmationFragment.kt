package za.co.woolworths.financial.services.android.ui.fragments.account

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.fragment_link_device_from_account_prod.*
import kotlinx.android.synthetic.main.layout_link_device_result.*
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.ui.activities.account.LinkDeviceConfirmationActivity
import za.co.woolworths.financial.services.android.ui.activities.account.LinkDeviceConfirmationInterface
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInPresenterImpl
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.Utils


class LinkDeviceConfirmationFragment : Fragment(), View.OnClickListener {

    private var mApplyNowState: ApplyNowState? = null
    private var toolbar: Toolbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            mApplyNowState = it.getSerializable(AccountSignedInPresenterImpl.APPLY_NOW_STATE) as? ApplyNowState
                    ?: ApplyNowState.STORE_CARD
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        activity?.runOnUiThread { activity?.window?.clearFlags(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE) }
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_link_device_from_account_prod, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let {
            if (it is LinkDeviceConfirmationInterface) {
                toolbar = it.getToolbar() as Toolbar
            }
        }
        val skipButton: TextView = toolbar?.findViewById(R.id.linkDeviceConfirmToolbarRightButton) as TextView
        skipButton.setOnClickListener(this)
        val navController = Navigation.findNavController(view)
        linkDeviceConfirmationButton.setOnClickListener {

            navController.navigate(R.id.action_linkDeviceConfirmationFragment_to_otp_navigation, bundleOf(
                    AccountSignedInPresenterImpl.APPLY_NOW_STATE to mApplyNowState
            ))
        }

    }

    companion object {
        @JvmStatic
        fun newInstance() =
                LinkDeviceConfirmationFragment().apply {
                    arguments = Bundle().apply {
                    }
                }

        private const val TAG = "LinkDeviceConfirmationF"
    }

    override fun onResume() {
        super.onResume()
        activity?.let {
            if (it is LinkDeviceConfirmationInterface) {
                it.showToolbarButton()
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.linkDeviceConfirmToolbarRightButton -> {
                onSkipPressed()
            }
        }
    }

    private fun onSkipPressed() {
        context?.let {
            linkDeviceResultIcon?.setImageDrawable(ContextCompat.getDrawable(it, R.drawable.ic_skip))
            linkDeviceResultTitle?.text = it.getString(R.string.ok_cool)
        }
        linkDeviceResultLayout.visibility = View.VISIBLE
        linkDeviceConfirmationScrollLayout.visibility = View.GONE

        activity?.apply {
            if (this is LinkDeviceConfirmationActivity) {
                supportActionBar?.let {
                    it.setDisplayHomeAsUpEnabled(false)
                }
            }
            if (this is LinkDeviceConfirmationInterface) {
                hideToolbarButton()
            }
            Handler().postDelayed({

                Utils.setLinkConfirmationShown(true)
                val intent = Intent()
                intent.putExtra(AccountSignedInPresenterImpl.APPLY_NOW_STATE, mApplyNowState)
                setResult(MyAccountsFragment.RESULT_CODE_LINK_DEVICE, intent)
                finish()
            }, AppConstant.DELAY_1500_MS)
        }
    }
}