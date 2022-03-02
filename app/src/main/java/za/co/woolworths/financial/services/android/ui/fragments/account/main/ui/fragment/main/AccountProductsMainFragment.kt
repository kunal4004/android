package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.main

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.awfs.coordination.R
import com.awfs.coordination.databinding.AccountProductLandingMainFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.landing.AccountProductsHomeViewModel

@AndroidEntryPoint
class AccountProductsMainFragment : Fragment() {
    private lateinit var binding: AccountProductLandingMainFragmentBinding

    val viewModel : AccountProductsHomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = AccountProductLandingMainFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ResourceType")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        findNavController().navigate(R.id.accountProductsHomeFragment,arguments)
    }
}