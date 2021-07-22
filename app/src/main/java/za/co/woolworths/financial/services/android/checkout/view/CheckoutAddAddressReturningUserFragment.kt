package za.co.woolworths.financial.services.android.checkout.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.checkout_delivery_time_slot_selection_fragment.*
import kotlinx.android.synthetic.main.layout_native_checkout_delivery_food_substitution.*
import za.co.woolworths.financial.services.android.checkout.interactor.CheckoutAddAddressNewUserInteractor
import za.co.woolworths.financial.services.android.checkout.service.network.AvailableDeliverySlotsResponse
import za.co.woolworths.financial.services.android.checkout.service.network.CheckoutAddAddressNewUserApiHelper
import za.co.woolworths.financial.services.android.checkout.service.network.CheckoutMockApiHelper
import za.co.woolworths.financial.services.android.checkout.service.network.HeaderDate
import za.co.woolworths.financial.services.android.checkout.view.adapter.DeliverySlotsGridViewAdapter
import za.co.woolworths.financial.services.android.checkout.view.adapter.SlotsDateGridViewAdapter
import za.co.woolworths.financial.services.android.checkout.view.adapter.SlotsTimeGridViewAdapter
import za.co.woolworths.financial.services.android.checkout.viewmodel.CheckoutAddAddressNewUserViewModel
import za.co.woolworths.financial.services.android.checkout.viewmodel.DeliveryGridModel
import za.co.woolworths.financial.services.android.checkout.viewmodel.ViewModelFactory
import za.co.woolworths.financial.services.android.service.network.ResponseStatus


/**
 * Created by Kunal Uttarwar on 27/05/21.
 */
class CheckoutAddAddressReturningUserFragment : Fragment(), View.OnClickListener {

    private lateinit var checkoutAddAddressNewUserViewModel: CheckoutAddAddressNewUserViewModel

    enum class FoodSubstitution(val rgb: String) {
        PHONE_CONFIRM("YES_CALL_CONFIRM"),
        SIMILAR_SUBSTITUTION("YES"),
        NO_THANKS("NO")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.checkout_add_address_retuning_user, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
    }

    private fun initViews() {
        initializeDeliveryFoodItems()
        initializeFoodSubstitution()
    }

    /**
     * Initializes food substitution view and Set by default selection to [FoodSubstitution.SIMILAR_SUBSTITUTION]
     *
     * @see [FoodSubstitution]
     */
    private fun initializeFoodSubstitution() {
        var selectedFoodSubstitution = FoodSubstitution.SIMILAR_SUBSTITUTION
        radioGroupFoodSubstitution?.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.radioBtnPhoneConfirmation -> {
                    selectedFoodSubstitution = FoodSubstitution.PHONE_CONFIRM
                }
                R.id.radioBtnSimilarSubst -> {
                    selectedFoodSubstitution = FoodSubstitution.SIMILAR_SUBSTITUTION
                }
                R.id.radioBtnNoThanks -> {
                    selectedFoodSubstitution = FoodSubstitution.NO_THANKS
                }
            }
        }
    }

    private fun initializeDeliveryFoodItems() {
        setupViewModel()
        getAvailableDeliverySlots()
        previousImgBtn.setOnClickListener(this)
        nextImgBtn.setOnClickListener(this)
    }

    private fun initializeGrid(availableDeliverySlotsResponse: AvailableDeliverySlotsResponse?, weekNumber: Int) {
        //TODO: Need to change this harcode to response. Will do in next PR.
        val deliveryGridList: ArrayList<DeliveryGridModel> = ArrayList()
        deliveryGridList. add(DeliveryGridModel("A", R.color.selected_address_background_color, 1, false))
        deliveryGridList.add(DeliveryGridModel("B", R.color.selected_address_background_color, 2, false))
        deliveryGridList.add(DeliveryGridModel("C", R.color.selected_address_background_color, 3, false))
        deliveryGridList.add(DeliveryGridModel("D", R.color.selected_address_background_color, 4, false))
        deliveryGridList.add(DeliveryGridModel("E", R.color.selected_address_background_color, 5, false))
        deliveryGridList.add(DeliveryGridModel("F", R.color.selected_address_background_color, 6, false))

        val adapter = context?.let { DeliverySlotsGridViewAdapter(it, R.layout.delivery_grid_card_item, deliveryGridList) }
        val hoursSlots = availableDeliverySlotsResponse?.sortedJoinDeliverySlots?.get(weekNumber)?.hourSlots
        val datesSlots = availableDeliverySlotsResponse?.sortedJoinDeliverySlots?.get(weekNumber)?. headerDates
        createTimingsGrid(hoursSlots)
        createDatesGrid(datesSlots)
        timeSlotsGridView.numColumns = hoursSlots?.size ?: 0
        timeSlotsGridView.setAdapter(adapter)

        timeSlotsGridView.setOnItemClickListener { parent, view, position, id ->
            for (model in deliveryGridList){
                model.isSelected = false
            }
            val deliveryGridModel: DeliveryGridModel = deliveryGridList.get(position)
            deliveryGridModel.isSelected = true
            adapter?.notifyDataSetChanged()
        }
    }

    private fun createTimingsGrid(hoursSlots: List<String>?) {
        timingsGridView.numColumns = hoursSlots?.size ?: 0 + 1 // Adding 1 only to match slots title grid with actual slots
        timingsGridView.adapter = context?.let { hoursSlots?.let { it1 ->
            SlotsTimeGridViewAdapter(
                it,
                R.layout.checkout_delivery_slot_timedate_item,
                it1
            )
        } }
    }

    private fun createDatesGrid(datesSlots: List<HeaderDate>?) {
        dateGridView.adapter = context?.let { datesSlots?.let { it1 ->
            SlotsDateGridViewAdapter(
                it,
                R.layout.checkout_delivery_slot_timedate_item,
                it1
            )
        } }
    }

    private fun setupViewModel() {
        checkoutAddAddressNewUserViewModel = ViewModelProviders.of(
            this,
            ViewModelFactory(
                CheckoutAddAddressNewUserInteractor(
                    CheckoutAddAddressNewUserApiHelper(),
                    CheckoutMockApiHelper()
                )
            )
        ).get(CheckoutAddAddressNewUserViewModel::class.java)
    }

    private fun getAvailableDeliverySlots(){
        checkoutAddAddressNewUserViewModel.getAvailableDeliverySlots().observe(viewLifecycleOwner, {
            when (it.responseStatus) {
                ResponseStatus.SUCCESS -> {
                    loadingBar.visibility = View.GONE
                    if(it.data != null) {
                        initializeGrid(it.data as? AvailableDeliverySlotsResponse, 0)
                    }
                }
                ResponseStatus.LOADING -> {
                    loadingBar.visibility = View.VISIBLE
                }
                ResponseStatus.ERROR -> {
                    loadingBar.visibility = View.GONE
                }
            }
        })
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.previousImgBtn -> {

            }
            R.id.nextImgBtn -> {

            }
        }
    }
}