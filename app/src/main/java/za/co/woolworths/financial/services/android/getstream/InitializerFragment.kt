package za.co.woolworths.financial.services.android.getstream

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.awfs.coordination.R
import com.awfs.coordination.databinding.FragmentGetStreamInitializerBinding
import io.getstream.chat.android.client.BuildConfig
import za.co.woolworths.financial.services.android.getstream.common.navigateSafely
import za.co.woolworths.financial.services.android.ui.views.ToastFactory.Companion.showToast

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

        viewModel.state.observe(
                viewLifecycleOwner,
                Observer {
                    when (it) {
                        is State.RedirectToChannels -> redirectToChannelsScreen()
                        is State.Loading -> showLoading()
                        is State.Error -> showErrorMessage(it.errorMessage)
                    }
                }
        )
    }

    private fun showLoading() {
//        binding.loadingProgressBar.isVisible = true
    }

    private fun showErrorMessage(errorMessage: String?) {
//        binding.loadingProgressBar.isVisible = false
//        showToast(errorMessage ?: getString(R.string.backend_error_info))
    }

    private fun redirectToChannelsScreen() {
        findNavController().navigateSafely(R.id.action_initializerFragment_to_channelListFragment)
    }

}