package za.co.woolworths.financial.services.android.ui.fragments.contact_us.enquiry.emailus

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.awfs.coordination.R
import com.awfs.coordination.databinding.FragmentEmailUsBinding
import com.google.firebase.perf.session.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.ui.activities.account.MyAccountActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigator
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.contact_us.enquiry.list.EnquiriesListFragment
import za.co.woolworths.financial.services.android.ui.fragments.contact_us.enquiry.list.EnquiriesListViewModel
import za.co.woolworths.financial.services.android.ui.fragments.contact_us.enquiry.list.EnquiriesListViewModel.Companion.EMAIL_US_REQUEST
import za.co.woolworths.financial.services.android.ui.fragments.contact_us.enquiry.list.ValidationErrors
import za.co.woolworths.financial.services.android.ui.fragments.contact_us.enquiry.loading.EmailUsLoadingActivity
import za.co.woolworths.financial.services.android.util.SessionUtilities

@AndroidEntryPoint
class EmailUsFragment : Fragment(), View.OnClickListener, TextWatcher {
    private var _binding: FragmentEmailUsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EnquiriesListViewModel by activityViewModels()

    private var mBottomNavigator: BottomNavigator? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is BottomNavigationActivity)
            mBottomNavigator = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEmailUsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() {
        setupToolbar()
        observers()
        binding.btnEmailUs.setOnClickListener(this)
        binding.etEmailUsEnquiry.setOnClickListener(this)
        if (SessionUtilities.getInstance().isUserAuthenticated()) {
            binding.etEmailUsEmail.setText(viewModel.userEmailAddress())
            binding.etEmailUsName.setText(viewModel.userName())
        }
    }

    private fun editTextListeners() {
        binding.etEmailUsName.addTextChangedListener(this)
        binding.etEmailUsEmail.addTextChangedListener(this)
        binding.etEmailUsEnquiry.addTextChangedListener(this)
        binding.etEmailUsMessage.addTextChangedListener(this)
    }

    private fun observers() {
        viewModel.validationErrors?.observe(viewLifecycleOwner) {
            binding.apply {
                when (it) {
                    ValidationErrors.EmailNotValid -> {
                        binding.etEmailUsEmail.background = ContextCompat.getDrawable(requireActivity(), R.drawable.input_error_background)
                        binding.tvEmailUsEmailValidation.apply {
                            setTextColor(Color.RED)
                        }

                    }
                    ValidationErrors.EnquiryNotValid -> {
                        tvEmailUsEnquiry.setTextColor(resources.getColor(R.color.red))

                    }
                    ValidationErrors.ValidationSuccess -> {
                        btnEmailUs.isEnabled = true
                    }
                }
            }
        }
        viewModel.selectedEnquiry?.observe(viewLifecycleOwner) {
            binding.etEmailUsEnquiry.setText(it?.displayName)
        }
    }

    override fun onResume() {
        super.onResume()
        editTextListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        viewModel.selectedEnquiry?.value = null
        viewModel.validationErrors?.value = null
    }


    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden)
            setupToolbar()
    }

    private fun setupToolbar() {
        val title = bindString(R.string.email_us)
        mBottomNavigator?.apply {
            setTitle(title)
            displayToolbar()
            showBackNavigationIcon(true)
        }

        if (activity is MyAccountActivity)
            (activity as? MyAccountActivity)?.setToolbarTitle(title)
    }

    override fun onClick(view: View?) {
        when (view) {
            binding.btnEmailUs -> {
                binding.apply {
                    resetViews()
                    if (viewModel.contactUsValidation(
                            etEmailUsName.text.toString(),
                            etEmailUsEmail.text.toString(),
                            etEmailUsMessage.text.toString()
                        )
                    ) {
                        mBottomNavigator?.popFragment()
                        val intent = Intent(activity, EmailUsLoadingActivity::class.java)
                        intent.putExtra(EMAIL_US_REQUEST, viewModel.emailUsRequest.value)
                        activity?.startActivity(intent)
                    }

                }
            }
            binding.etEmailUsEnquiry -> {
                if (activity is BottomNavigationActivity)
                    mBottomNavigator?.pushFragmentSlideUp(EnquiriesListFragment())
                else
                    (activity as? MyAccountActivity)?.replaceFragment(EnquiriesListFragment())
            }
        }
    }

    private fun resetViews() {
        binding.tvEmailUsEnquiry.setTextColor(resources.getColor(R.color.color_222222))
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

    }

    override fun afterTextChanged(editable: Editable?) {
        binding.apply {
            btnEmailUs.isEnabled = viewModel.enableSenButton(
                etEmailUsName.text.toString(),
                etEmailUsEmail.text.toString(),
                etEmailUsEnquiry.text.toString(),
                etEmailUsMessage.text.toString()
            )
        }
    }
}