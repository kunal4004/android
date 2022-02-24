package za.co.woolworths.financial.services.android.ui.fragments.account.main.fragment.landing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.awfs.coordination.databinding.AccountProductsHomeFragmentBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.AndroidEntryPoint
import za.co.woolworths.financial.services.android.ui.fragments.account.storecard.StoreCardActivity

@AndroidEntryPoint
class AccountProductsHomeFragment : Fragment() {

    private lateinit var binding: AccountProductsHomeFragmentBinding
    private var sheetBehavior: BottomSheetBehavior<*>? = null

    val viewModel: AccountProductsHomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = AccountProductsHomeFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBottomSheet()
    }

    private fun setupBottomSheet() {
        with(viewModel) {
            sheetBehavior = init(binding.bottomSheetBehaviourView)
            sheetBehavior?.addBottomSheetCallback(callback { slideOffset ->
                val homeIcon = (activity as? StoreCardActivity)?.findViewById<Button>(android.R.id.home)
                animateDim(slideOffset, binding.dimView)
                animateDim(slideOffset, homeIcon)
                animateDim(slideOffset, (activity as? StoreCardActivity)?.binding?.accountToolbar)
                homeIcon?.rotation = slideOffset * 90
            })
        }
    }
}