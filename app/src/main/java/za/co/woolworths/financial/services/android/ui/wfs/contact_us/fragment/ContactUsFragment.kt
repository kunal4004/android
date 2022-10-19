package za.co.woolworths.financial.services.android.ui.wfs.contact_us.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.awfs.coordination.R
import dagger.hilt.android.AndroidEntryPoint
import za.co.woolworths.financial.services.android.ui.compose.contentView
import za.co.woolworths.financial.services.android.ui.wfs.contact_us.screen.ContactUsCategoryScreen
import za.co.woolworths.financial.services.android.ui.wfs.contact_us.screen.ContactUsEvent
import za.co.woolworths.financial.services.android.ui.wfs.contact_us.viewmodel.ContactUsViewModel
import za.co.woolworths.financial.services.android.ui.wfs.core.LandingRouter
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppTheme
import javax.inject.Inject

@AndroidEntryPoint
class ContactUsFragment : Fragment() {

    private val viewModel: ContactUsViewModel by activityViewModels()

    @Inject lateinit var router: LandingRouter
    @Inject lateinit var toolbar: ContactUsToolbar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?)
    = contentView { OneAppTheme {
            ContactUsCategoryScreen(viewModel) { event ->
                when(event){
                    is ContactUsEvent.CategoryItemClicked -> {
                        viewModel.setSubCategoryItem(event.details)
                        router.push(ContactUsSubCategoryFragment())}
                    is ContactUsEvent.Response -> {

                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setToolbar()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) setToolbar()
    }

    private fun setToolbar() {
        toolbar.setToolbar(getString(R.string.contact_us))
    }
}

