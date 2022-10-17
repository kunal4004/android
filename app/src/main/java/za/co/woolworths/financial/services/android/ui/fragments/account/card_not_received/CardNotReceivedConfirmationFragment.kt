package za.co.woolworths.financial.services.android.ui.fragments.account.card_not_received

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.awfs.coordination.R
import com.awfs.coordination.databinding.CardNotArrivedFragmentBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.ui.base.onClick
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.activities.StoreCardActivity
import za.co.woolworths.financial.services.android.util.AppConstant

class CardNotReceivedConfirmationFragment : Fragment(R.layout.card_not_arrived_fragment) {

    val viewModel: CircularProgressIndicatorViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = CardNotArrivedFragmentBinding.bind(view)
        binding.gotItButton.onClick {
            onBackPressed()
        }
        setSuccess()
    }

    private fun setSuccess() {
        CoroutineScope(Dispatchers.Main).launch {
            delay(AppConstant.DELAY_200_MS)
            viewModel.setState(ProgressIndicator.Success)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        onBackPressed()
    }

    private fun onBackPressed() {
        (activity as? StoreCardActivity)?.landingNavController()?.popBackStack()
    }

}