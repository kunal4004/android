package za.co.woolworths.financial.services.android.ui.fragments.contact_us.enquiry.list

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.awfs.coordination.R
import com.awfs.coordination.databinding.FragmentEnquiriesListBinding
import dagger.hilt.android.AndroidEntryPoint
import za.co.woolworths.financial.services.android.ui.activities.account.MyAccountActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigator
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.contact_us.enquiry.emailus.EmailUsFragment


@AndroidEntryPoint
class EnquiriesListFragment : Fragment(), EnquiriesListAdapter.ItemListener {
    private var _binding: FragmentEnquiriesListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EnquiriesListViewModel by activityViewModels()
    private lateinit var adapter: EnquiriesListAdapter

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
        setupToolbar()
        _binding = FragmentEnquiriesListBinding.inflate(inflater, container, false)
        setAdapter()
        return binding.root
    }
    fun setAdapter(){
        viewModel.contactUsModel.observe(viewLifecycleOwner) {
            when(it.contactUsFinancialServicesEmail().isNullOrEmpty()){
                false->{
                    var emailList = it.contactUsFinancialServicesEmail()
                    var selectedIndex = emailList?.indexOf(viewModel.selectedEnquiry?.value)
                    adapter = EnquiriesListAdapter(this, emailList!!,selectedIndex)
                    binding.rcvEnquiries.adapter = adapter
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onSelectEnquiry(pos: Int) {
        adapter.notifyDataSetChanged()
        viewModel.apply {
            (activity is BottomNavigationActivity).apply{
                    mBottomNavigator?.popFragment()
                    if (viewModel.selectedEnquiry?.value ==null){
                        mBottomNavigator?.pushFragment(EmailUsFragment())
                    }
            }.apply {
                selectedEnquiry?.value = contactUsModel.value?.contactUsFinancialServicesEmail()!![pos]
            }
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden)
            setupToolbar()
    }

    private fun setupToolbar() {
        val title = bindString(R.string.enquiry_type)
        mBottomNavigator?.apply {
            setTitle(title)
            displayToolbar()
            showBackNavigationIcon(true)
        }

        if (activity is MyAccountActivity)
            (activity as? MyAccountActivity)?.setToolbarTitle(title)
    }
}