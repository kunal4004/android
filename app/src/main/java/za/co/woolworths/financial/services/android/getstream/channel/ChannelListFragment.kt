package za.co.woolworths.financial.services.android.getstream.channel

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
import dagger.hilt.android.AndroidEntryPoint
import za.co.woolworths.financial.services.android.getstream.chat.ChatFragment
import za.co.woolworths.financial.services.android.getstream.common.State
import za.co.woolworths.financial.services.android.ui.vto.ui.bottomsheet.VtoErrorBottomSheetDialog
import za.co.woolworths.financial.services.android.ui.vto.ui.bottomsheet.listener.VtoTryAgainListener
import javax.inject.Inject

@AndroidEntryPoint
class ChannelListFragment : Fragment(), VtoTryAgainListener {

    companion object{
        val messageType = "messaging"
    }

    private val viewModel: ChannelListViewModel by viewModels()
    private var _binding: FragmentGetStreamInitializerBinding? = null
    private val binding get() = _binding!!
    @Inject
    lateinit var errorBottomSheetDialog: VtoErrorBottomSheetDialog

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
            viewLifecycleOwner,
            {
                when (it) {
                    is State.RedirectToChat -> redirectToChatScreen(it.channelId)
                    is State.Loading -> showLoading()
                    is State.Error -> showErrorDialog()
                }
            }
        )
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
        binding.oneCartChatProgressBar.visibility = View.GONE
        requireContext().apply {
            errorBottomSheetDialog.showErrorBottomSheetDialog(
                this@ChannelListFragment,
                this,
                getString(R.string.vto_generic_error),
                getString(R.string.one_cart_chat_error_disc),
                getString(R.string.got_it)
            )
        }
    }

    override fun tryAgain() {
        requireActivity().finish()
    }

}