package za.co.woolworths.financial.services.android.ui.wfs.contact_us.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.whatsapp.WhatsAppChatToUs
import za.co.woolworths.financial.services.android.ui.compose.contentView
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WhatsAppUnavailableFragment
import za.co.woolworths.financial.services.android.ui.wfs.contact_us.viewmodel.ContactUsViewModel
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppTheme
import za.co.woolworths.financial.services.android.ui.wfs.contact_us.screen.ContactUsSubCategoryScreen
import za.co.woolworths.financial.services.android.ui.wfs.core.LandingRouter
import za.co.woolworths.financial.services.android.ui.wfs.mobileconfig.ContactUsType
import za.co.woolworths.financial.services.android.util.ScreenManager
import za.co.woolworths.financial.services.android.util.Utils
import javax.inject.Inject

@AndroidEntryPoint
class ContactUsSubCategoryFragment : Fragment() {

    val viewModel: ContactUsViewModel by activityViewModels()

    @Inject lateinit var router: LandingRouter
    @Inject lateinit var toolbar: ContactUsToolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setToolbar()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) = contentView {
        OneAppTheme {
            ContactUsSubCategoryScreen(viewModel) { children ->
                when (children.type) {
                    ContactUsType.ACTION_EMAIL_INAPP -> {
                        viewModel.wasEnquiryListOpenedFromEmailFragment = false
                        viewModel.setEnquiryTypeList(children.children)
                        router.push(ContactUsSelectEmailEnquiryTypeFragment(), true) }
                    ContactUsType.ACTION_CALL -> viewModel.call(children.description)
                    ContactUsType.ACTION_WHATSAPP_FS -> whatsappChat()
                    ContactUsType.NONE, null -> Unit
                }
            }
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            setToolbar()
        }
    }

    private fun setToolbar() {
      toolbar.setToolbar(viewModel.subCategories.first)
    }

   private fun whatsappChat(){
        if (!WhatsAppChatToUs().isCustomerServiceAvailable) {
            val whatsAppUnavailableFragment = WhatsAppUnavailableFragment()
            activity?.supportFragmentManager?.let { supportFragmentManager -> whatsAppUnavailableFragment.show(supportFragmentManager, WhatsAppUnavailableFragment::class.java.simpleName) }
            return
        }
        activity?.apply { Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.WHATSAPP_CONTACT_US, this) }
        ScreenManager.presentWhatsAppChatToUsActivity(activity,
            WhatsAppChatToUs.FEATURE_WHATSAPP,
            WhatsAppChatToUs.CONTACT_US
        )
    }
}