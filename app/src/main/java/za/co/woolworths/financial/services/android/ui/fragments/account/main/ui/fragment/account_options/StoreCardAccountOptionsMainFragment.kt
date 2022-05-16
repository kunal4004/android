package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.awfs.coordination.databinding.StoreCardAccountOptionsFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import za.co.woolworths.financial.services.android.ui.base.ViewBindingFragment

@AndroidEntryPoint
class StoreCardAccountOptionsMainFragment :
    ViewBindingFragment<StoreCardAccountOptionsFragmentBinding>(
        StoreCardAccountOptionsFragmentBinding::inflate)