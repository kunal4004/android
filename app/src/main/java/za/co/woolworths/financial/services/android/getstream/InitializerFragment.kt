package za.co.woolworths.financial.services.android.getstream

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.awfs.coordination.R
import com.awfs.coordination.databinding.FragmentGetStreamInitializerBinding
import za.co.woolworths.financial.services.android.getstream.common.State
import za.co.woolworths.financial.services.android.getstream.common.navigateSafely



class InitializerFragment : Fragment() {

    private val viewModel: InitializerViewModel by viewModels()
    private var _binding: FragmentGetStreamInitializerBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGetStreamInitializerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (viewModel.isConnectedToInternet(requireContext()))
        initChat()
        else
            binding.oneCartChatConnectionLayout.noConnectionLayout.visibility = View.VISIBLE

    }

    private fun initChat() {
        viewModel.state.observe(
            viewLifecycleOwner
        ) {
            when (it) {
                is State.RedirectToChannels -> redirectToChannelsScreen()
                is State.Loading -> showLoading()
                is State.Error -> showErrorDialog()
            }
        }

        binding.oneCartChatConnectionLayout.btnRetry.setOnClickListener {
           requireActivity().finish()
        }
    }

    private fun showLoading() {
        binding.oneCartChatProgressBar.visibility = View.VISIBLE
    }

    private fun redirectToChannelsScreen() {
        binding.oneCartChatProgressBar.visibility = View.GONE
        findNavController().navigateSafely(R.id.action_initializerFragment_to_channelListFragment)
    }

    private fun showErrorDialog(){
        (activity as? OCChatActivity)?.showErrorDialog()
    }

}