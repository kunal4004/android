package za.co.woolworths.financial.services.android.onecartgetstream.channel

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.awfs.coordination.R
import com.awfs.coordination.databinding.FragmentGetStreamInitializerBinding
import za.co.woolworths.financial.services.android.onecartgetstream.OCChatActivity
import za.co.woolworths.financial.services.android.onecartgetstream.chat.ChatFragment
import za.co.woolworths.financial.services.android.onecartgetstream.common.State



class ChannelListFragment : Fragment() {

    companion object{
        val messageType = "messaging"
    }

    private val viewModel: ChannelListViewModel by viewModels()
    private var _binding: FragmentGetStreamInitializerBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentGetStreamInitializerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initViewModel()
    }

    private fun initViewModel() {
        viewModel.state.observe(
            viewLifecycleOwner
        ) {
            when (it) {
                is State.RedirectToChat -> redirectToChatScreen(it.channelId)
                is State.Loading -> showLoading()
                is State.Error -> showErrorDialog()
                else -> {
                    // Nothing
                }
            }
        }
        viewModel.fetchChannels()
    }

    private fun showLoading() {
        binding.oneCartChatProgressBar.visibility = View.VISIBLE
    }



    private fun redirectToChatScreen(channelId: String) {
        binding.oneCartChatProgressBar.visibility = View.GONE
        val bundle = bundleOf(ChatFragment.ARG_CHANNEL_ID to channelId)
        findNavController().navigate(R.id.action_channelListFragment_to_chatFragment, bundle)
    }

    private fun showErrorDialog(){
        (activity as? OCChatActivity)?.showErrorDialog()
    }

}