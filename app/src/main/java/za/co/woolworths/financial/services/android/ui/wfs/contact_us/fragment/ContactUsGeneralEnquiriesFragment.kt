package za.co.woolworths.financial.services.android.ui.wfs.contact_us.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import za.co.woolworths.financial.services.android.ui.compose.contentView
import za.co.woolworths.financial.services.android.ui.wfs.contact_us.viewmodel.ContactUsViewModel
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppTheme
import za.co.woolworths.financial.services.android.ui.wfs.contact_us.screen.ContactUsSubCategoryScreen
import za.co.woolworths.financial.services.android.ui.wfs.core.LandingRouter
import za.co.woolworths.financial.services.android.ui.wfs.mobileconfig.ContactUsType
import javax.inject.Inject

@AndroidEntryPoint
class ContactUsGeneralEnquiriesFragment : Fragment() {

    val viewModel: ContactUsViewModel by activityViewModels()

    @Inject
    lateinit var router: LandingRouter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = contentView {
        OneAppTheme {
            ContactUsSubCategoryScreen(viewModel) { children ->
                when (children.type) {
                    ContactUsType.ACTION_EMAIL_INAPP -> { router.push(ContactUsEmailInAppFragment()) }
                    ContactUsType.ACTION_CALL -> {}
                    ContactUsType.ACTION_WHATSAPP_FS -> {}
                    ContactUsType.NONE, null -> Unit
                }
            }
        }
    }
}