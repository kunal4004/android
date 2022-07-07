package za.co.woolworths.financial.services.android.getstream.chat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.awfs.coordination.R
import com.awfs.coordination.databinding.FragmentChannelListBinding
import com.awfs.coordination.databinding.FragmentOneCartChatBinding
import za.co.woolworths.financial.services.android.getstream.channel.ChannelListViewModel
import za.co.woolworths.financial.services.android.getstream.common.State


class ChatFragment : Fragment() {

    companion object{
        const val ARG_CHANNEL_ID = "channelId"
    }

    private val viewModel: ChatViewModel by viewModels()

    private var _binding: FragmentOneCartChatBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            viewModel.channelId = it.getString(ARG_CHANNEL_ID)
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentOneCartChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.fetchMessages()
    }
}