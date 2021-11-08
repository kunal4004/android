package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.more_review_layout.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.network.service.ReviewApiHelper
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view.adapter.MoreReviewsAdapter
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.viewmodel.ReviewViewModel
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.viewmodel.ReviewViewModelFactory

class MoreReviewFragment : Fragment() {

    private lateinit var reviewViewModel: ReviewViewModel
    private lateinit var reviewListAdapter: MoreReviewsAdapter

    companion object {
        fun newInstance() = MoreReviewFragment()
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.more_review_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.e("onViewCreated :", "called")
        setUpViewModel()
        setDefaultUi()
        setupView()
    }

    private fun setUpViewModel() {
        reviewViewModel = ViewModelProvider(this,
                ReviewViewModelFactory(ReviewApiHelper(10)))[ReviewViewModel::class.java]
    }

    private fun setDefaultUi() {
        reviewListAdapter = MoreReviewsAdapter()
        recycler_view.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = reviewListAdapter
        }
    }

    private fun setupView() {
        Log.e("setupView", "called")
        lifecycleScope.launch {
        Log.e("setupView_inside", "called")

            reviewViewModel.moreReviewData.collect {
                reviewListAdapter.submitData(it)
            }
        }
    }
}