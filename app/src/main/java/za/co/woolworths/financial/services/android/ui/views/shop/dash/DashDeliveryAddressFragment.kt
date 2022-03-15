package za.co.woolworths.financial.services.android.ui.views.shop.dash

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.fragment_dash_delivery.*
import za.co.woolworths.financial.services.android.ui.adapters.shop.dash.DashDeliveryAdapter
import za.co.woolworths.financial.services.android.viewmodels.shop.ShopViewModel
import za.co.woolworths.financial.services.android.models.network.Status

class DashDeliveryAddressFragment : Fragment(R.layout.fragment_dash_delivery) {

    lateinit var viewModel: ShopViewModel

    private lateinit var dashDeliveryAdapter: DashDeliveryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dashDeliveryAdapter = DashDeliveryAdapter()
        viewModel = ViewModelProvider(requireActivity()).get(ShopViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!isVisible) {
            return
        }

        if (viewModel.isCategoriesAvailable.value == true) {
            // set data to views
            layoutDashSetAddress?.visibility = View.GONE
            setupRecyclerView()
            viewModel.categories.value?.peekContent()?.let { resource ->
                dashDeliveryAdapter.categoryList =
                    resource.data?.rootCategories ?: ArrayList(0)
            }
        }

        viewModel.categories.observe(viewLifecycleOwner, {
            it.getContentIfNotHandled()?.let { resource ->
                when (resource.status) {
                    Status.LOADING -> {
                        //TODO need to confirm loading screens between shimmer view or progressbar
                    }
                    Status.SUCCESS -> {
                        layoutDashSetAddress?.visibility = View.GONE
                        setupRecyclerView()
                        dashDeliveryAdapter.categoryList =
                            resource.data?.rootCategories ?: ArrayList(0)
                    }
                    Status.ERROR -> {
                        showErrorView(resource.message, resource.data)
                    }
                }
            }
        })
    }

    private fun setupRecyclerView() {
        rvDashDelivery?.apply {
            adapter = dashDeliveryAdapter
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        }
    }

    private fun showErrorView(message: String?, data: Any?) {
        //TODO: get error screens from UI/UX team
    }
}