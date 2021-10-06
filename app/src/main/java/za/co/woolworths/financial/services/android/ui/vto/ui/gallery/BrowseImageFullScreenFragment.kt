package za.co.woolworths.financial.services.android.ui.vto.ui.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.fragment.app.Fragment
import com.awfs.coordination.databinding.BrowseFullScreenPhotoFragmentBinding
import java.io.File


class BrowseImageFullScreenFragment : Fragment() {

    private var _binding: BrowseFullScreenPhotoFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BrowseFullScreenPhotoFragmentBinding.inflate(inflater, container, false)
        //hideSystemUI()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }


    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        private const val FILE_NAME_KEY = "file_name"
        fun create(image: File) = BrowseImageFullScreenFragment().apply {
            arguments = Bundle().apply {
                putString(FILE_NAME_KEY, image.absolutePath)
            }
        }

    }


//    private fun hideSystemUI() {
//        WindowCompat.setDecorFitsSystemWindows(requireActivity().window, false)
//        WindowInsetsControllerCompat(requireActivity().window,binding.mainLayoutBrowseFile).let { controller ->
//            controller.hide(WindowInsetsCompat.Type.systemBars())
//            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
//        }
//    }

}