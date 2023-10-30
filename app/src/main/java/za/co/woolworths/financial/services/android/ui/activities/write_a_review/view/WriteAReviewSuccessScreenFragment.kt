package za.co.woolworths.financial.services.android.ui.activities.write_a_review.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import com.awfs.coordination.databinding.WriteAReviewSuccessScreenFragmentBinding
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity

class WriteAReviewSuccessScreenFragment: Fragment(), View.OnClickListener {
    private var _binding: WriteAReviewSuccessScreenFragmentBinding ? = null
    private val binding get() = _binding!!

    companion object {
        fun newInstance() = WriteAReviewSuccessScreenFragment()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = WriteAReviewSuccessScreenFragmentBinding.inflate(inflater,container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.continueShopping.setOnClickListener(this@WriteAReviewSuccessScreenFragment)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.continue_shopping -> {
                (activity as? BottomNavigationActivity)?.navigateToDepartmentFragment()
            }
        }

    }
}