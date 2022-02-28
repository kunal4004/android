package za.co.woolworths.financial.services.android.ui.fragments.account.storecard.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.awfs.coordination.databinding.FragmentCartBinding
import dagger.hilt.android.AndroidEntryPoint
import za.co.woolworths.financial.services.android.ui.fragments.account.storecard.utils.autoCleared
import za.co.woolworths.financial.services.android.ui.fragments.account.storecard.viewmodel.StoreCardViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.storecard.utils.Result.Status.*

@AndroidEntryPoint
class StoreCardAccountOptionsFragment : Fragment() {
    //TODO: Change with it's own ui
    private var binding: FragmentCartBinding by autoCleared()
    val viewModel: StoreCardViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
    }

    private fun setupObservers() {
//        viewModel.toDo.observe(viewLifecycleOwner, Observer {
//            when (it.status) {
//                SUCCESS -> {
//                    Toast.makeText(requireContext(), ""+it.data, Toast.LENGTH_LONG).show()
//                }
//            }
//        })
    }
}