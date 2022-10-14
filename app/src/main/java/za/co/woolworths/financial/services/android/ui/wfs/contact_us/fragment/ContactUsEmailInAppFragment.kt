package za.co.woolworths.financial.services.android.ui.wfs.contact_us.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import za.co.woolworths.financial.services.android.ui.compose.contentView
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppTheme

@AndroidEntryPoint
class ContactUsEmailInAppFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = contentView {
        OneAppTheme {

        }
    }
}