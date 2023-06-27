package za.co.woolworths.financial.services.android.ui.wfs.contact_us.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.awfs.coordination.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.models.dto.app_config.ConfigOptions
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigator
import za.co.woolworths.financial.services.android.ui.fragments.contact_us.enquiry.emailus.ContactUsEmailUsFragment
import za.co.woolworths.financial.services.android.ui.fragments.contact_us.enquiry.list.EnquiriesListViewModel
import za.co.woolworths.financial.services.android.ui.wfs.common.contentView
import za.co.woolworths.financial.services.android.ui.wfs.contact_us.screen.SelectEnquiryTypeList
import za.co.woolworths.financial.services.android.ui.wfs.contact_us.viewmodel.ContactUsViewModel
import za.co.woolworths.financial.services.android.ui.wfs.core.LandingRouter
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppTheme
import za.co.woolworths.financial.services.android.util.AppConstant
import javax.inject.Inject

@AndroidEntryPoint
class ContactUsSelectEmailEnquiryTypeFragment : Fragment() {

    val viewModel: ContactUsViewModel by activityViewModels()
    val enquiryViewModel: EnquiriesListViewModel by activityViewModels()

    @Inject lateinit var router: LandingRouter
    @Inject lateinit var toolbar: ContactUsToolbar
    private var mBottomNavigator: BottomNavigator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setToolbar()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is BottomNavigationActivity)
            mBottomNavigator = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?)
            = contentView(ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)) {
    OneAppTheme {
            SelectEnquiryTypeList(viewModel.enquiryList) { childrenItem ->
                enquiryViewModel.apply {
                    (activity is BottomNavigationActivity).apply {
                        mBottomNavigator?.popFragment()
                        if (selectedEnquiry?.value == null) {
                            router.push(ContactUsEmailUsFragment())
                        }
                    }.apply {
                        selectedEnquiry?.value = ConfigOptions(key = childrenItem.reference ?: "", displayName = childrenItem.title ?: "", value = childrenItem.description ?: "")
                    }
                }
            }
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            setToolbar()
        }else {
            lifecycleScope.launch {
                delay(AppConstant.DELAY_200_MS)
                (activity as? BottomNavigationActivity)?.mNavController?.popFragment()
            }
        }
    }

    private fun setToolbar() {
        toolbar.setToolbar(getString(R.string.enquiry_type))
    }
}