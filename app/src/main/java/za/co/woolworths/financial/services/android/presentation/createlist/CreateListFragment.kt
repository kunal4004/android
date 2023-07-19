package za.co.woolworths.financial.services.android.presentation.createlist

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.awfs.coordination.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.presentation.createlist.components.CreateListScreenEvent
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigator
import za.co.woolworths.financial.services.android.ui.compose.contentView
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppTheme
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.REQUEST_CODE
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.REQUEST_CODE_CREATE_LIST
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.RESULT_CODE
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.RESULT_FAILED

@OptIn(ExperimentalComposeUiApi::class)
@AndroidEntryPoint
class CreateListFragment : Fragment() {

    private val viewModel: CreateListViewModel by viewModels()
    private var mBottomNavigator: BottomNavigator? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is BottomNavigationActivity)
            mBottomNavigator = context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isListCreated.collect {
                    it ?: return@collect
                    navigateBack(it)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = contentView(
        ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)
    ) {
        OneAppTheme {
            val state = viewModel.getCreateListState()
            CreateListScreen(
                state = state.copy(
                    title = stringResource(id = R.string.new_list)
                )
            ) {
                when (it) {
                    CreateListScreenEvent.BackPressed -> navigateBack(state.isSuccess)
                    else -> viewModel.onEvent(it)
                }
            }
        }
    }

    private fun navigateBack(success: Boolean) {
        setFragmentResult(
            REQUEST_CODE_CREATE_LIST.toString(),
            bundleOf(
                RESULT_CODE to if (success) arguments?.getInt(REQUEST_CODE) else RESULT_FAILED
            )
        )
        activity?.onBackPressed()
    }

    override fun onResume() {
        super.onResume()
        hideToolbar()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        showToolbar()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden)
            hideToolbar()
    }

    private fun showToolbar() {
        mBottomNavigator?.displayToolbar()
        (requireActivity() as? BottomNavigationActivity)?.showToolbar()
    }

    private fun hideToolbar() {
        mBottomNavigator?.apply {
            removeToolbar()
        }
        (requireActivity() as? BottomNavigationActivity)?.hideToolbar()
    }
}