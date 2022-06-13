package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.information

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.awfs.coordination.R
import com.awfs.coordination.databinding.FragmentInformationBinding
import dagger.hilt.android.AndroidEntryPoint
import za.co.woolworths.financial.services.android.ui.base.ViewBindingFragment


@AndroidEntryPoint
class InformationFragment :
    ViewBindingFragment<FragmentInformationBinding>(FragmentInformationBinding::inflate),
    View.OnClickListener {

    val viewModel by viewModels<InformationViewModel>()
    val args: InformationFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAdapter()
        onBackPressed()
        binding.ivInformationClose.setOnClickListener(this)
    }

    fun onBackPressed() {
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true /* enabled by default */) {
                override fun handleOnBackPressed() {
                    findNavController().popBackStack()
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    private fun setAdapter() {
        binding.informationRcv.adapter = InformationAdapter(args.informationData.info)
    }

    override fun onClick(view: View?) {
        when (view) {
            binding.ivInformationClose -> {
                findNavController().popBackStack()
            }
        }
    }
}