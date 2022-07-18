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
import dagger.hilt.android.AndroidEntryPoint
import za.co.woolworths.financial.services.android.getstream.common.State
import za.co.woolworths.financial.services.android.getstream.common.navigateSafely
import za.co.woolworths.financial.services.android.ui.vto.ui.bottomsheet.VtoErrorBottomSheetDialog
import za.co.woolworths.financial.services.android.ui.vto.ui.bottomsheet.listener.VtoTryAgainListener
import javax.inject.Inject

@AndroidEntryPoint
class InitializerFragment : Fragment(), VtoTryAgainListener {
    private val viewModel: InitializerViewModel by viewModels()

    private var _binding: FragmentGetStreamInitializerBinding? = null
    private val binding get() = _binding!!
    @Inject
    lateinit var errorBottomSheetDialog: VtoErrorBottomSheetDialog

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
            initChat()
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
        binding.oneCartChatProgressBar.visibility = View.GONE
        requireContext().apply {
            errorBottomSheetDialog.showErrorBottomSheetDialog(
                this@InitializerFragment,
                this,
                getString(R.string.pma_retry_error_title),
                getString(R.string.vto_generic_error),
                getString(R.string.try_again)
            )
        }
    }
    override fun tryAgain() {
        requireActivity().finish()
    }


}