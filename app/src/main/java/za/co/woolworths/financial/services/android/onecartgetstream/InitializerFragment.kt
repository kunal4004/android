package za.co.woolworths.financial.services.android.onecartgetstream

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.awfs.coordination.R
import com.awfs.coordination.databinding.FragmentGetStreamInitializerBinding
import za.co.woolworths.financial.services.android.models.network.Status
import za.co.woolworths.financial.services.android.onecartgetstream.common.State
import za.co.woolworths.financial.services.android.onecartgetstream.common.navigateSafely



class InitializerFragment : Fragment() {

    private val viewModel: InitializerViewModel by activityViewModels()
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
        if (viewModel.isConnectedToInternet(requireContext())) {
            initChat()
            getOCAuthData()
        } else
            binding.oneCartChatConnectionLayout.noConnectionLayout.visibility = View.VISIBLE

    }

    private fun getOCAuthData() {
        viewModel.ocAuthData.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        resource.data?.details?.let { details ->
                            viewModel.initChatUser(details.userId,details.name,details.token)
                        }
                    }
                    Status.ERROR -> {
                        showErrorDialog()
                    }
                }
            }
        }
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