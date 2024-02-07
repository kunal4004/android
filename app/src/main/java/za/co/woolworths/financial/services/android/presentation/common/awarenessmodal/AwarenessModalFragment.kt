package za.co.woolworths.financial.services.android.presentation.common.awarenessmodal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import za.co.woolworths.financial.services.android.ui.wfs.common.contentView

class AwarenessModalFragment: BottomSheetDialogFragment() {

    private val awarenessViewModel: AwarenessViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = contentView(ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)) {

        AwarenessModalView(
            awarenessViewModel
        ) {

        }
    }

}