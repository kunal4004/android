package za.co.woolworths.financial.services.android.ui.fragments.account

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.fragment_link_device_from_account_prod.*
import za.co.woolworths.financial.services.android.models.UserManager
import za.co.woolworths.financial.services.android.ui.activities.account.LinkDeviceConfirmationActivity
import za.co.woolworths.financial.services.android.ui.activities.account.LinkDeviceConfirmationInterface
import za.co.woolworths.financial.services.android.util.Utils


class LinkDeviceConfirmationFragment : Fragment(), View.OnClickListener {

    private var toolbar: Toolbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
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
            navController.navigate(R.id.action_linkDeviceConfirmationFragment_to_otp_navigation)
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
                activity?.apply {
                    val prefs = getSharedPreferences(Utils.SHARED_PREF, Context.MODE_PRIVATE)
                    prefs.edit().putBoolean(UserManager.LINK_DEVICE_CONFIRMATION, true).apply()
                    setResult(MyAccountsFragment.REQUEST_CODE_LINK_DEVICE)
                    finish()
                }
            }
        }
    }
}