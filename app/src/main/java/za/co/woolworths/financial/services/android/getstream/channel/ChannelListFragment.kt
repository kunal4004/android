package za.co.woolworths.financial.services.android.getstream.channel

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.awfs.coordination.R
import com.awfs.coordination.databinding.FragmentChannelListBinding
import io.getstream.chat.android.livedata.ChatDomain
import za.co.woolworths.financial.services.android.getstream.chat.ChatFragment
import za.co.woolworths.financial.services.android.getstream.common.State
import za.co.woolworths.financial.services.android.getstream.common.navigateSafely

class ChannelListFragment : Fragment() {

    companion object{
        val messageType = "messaging"
    }

    private val viewModel: ChannelListViewModel by viewModels()

    private var _binding: FragmentChannelListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentChannelListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        viewModel.state.observe(
                viewLifecycleOwner,
                Observer {
                    when (it) {
                        is State.RedirectToChat -> redirectToChatScreen(it.channelId)
                        is State.Loading -> showLoading()
                        is State.Error -> showErrorMessage(it.errorMessage)
                    }
                }
        )

        viewModel.fetchChannels()
    }

    private fun showLoading() {
        //TODO a loading indicator
    }

    private fun showErrorMessage(errorMessage: String?) {
        binding.infoText.text = errorMessage;
    }

    private fun redirectToChatScreen(channelId: String) {
        val bundle = bundleOf(ChatFragment.ARG_CHANNEL_ID to channelId)
        findNavController().navigate(R.id.action_channelListFragment_to_chatFragment, bundle)
    }
}