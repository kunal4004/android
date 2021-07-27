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
import kotlinx.android.synthetic.main.layout_native_checkout_delivery_instructions.*
import za.co.woolworths.financial.services.android.checkout.interactor.CheckoutAddAddressNewUserInteractor
import za.co.woolworths.financial.services.android.checkout.service.network.AvailableDeliverySlotsResponse
import za.co.woolworths.financial.services.android.checkout.service.network.CheckoutAddAddressNewUserApiHelper
import za.co.woolworths.financial.services.android.checkout.service.network.CheckoutMockApiHelper
import za.co.woolworths.financial.services.android.checkout.viewmodel.CheckoutAddAddressNewUserViewModel
import za.co.woolworths.financial.services.android.checkout.viewmodel.ViewModelFactory
import za.co.woolworths.financial.services.android.service.network.ResponseStatus
import za.co.woolworths.financial.services.android.util.Utils


/**
 * Created by Kunal Uttarwar on 27/05/21.
 */
open class CheckoutAddAddressReturningUserFragment : Fragment(), View.OnClickListener {

    private lateinit var checkoutAddAddressNewUserViewModel: CheckoutAddAddressNewUserViewModel
    private val expandableGrid = ExpandableGrid(this)
    private var selectedSlotResponse: AvailableDeliverySlotsResponse? = null

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

        activity?.apply {
            view?.setOnClickListener {
                Utils.hideSoftKeyboard(this)
            }
        }

        switchSpecialDeliveryInstruction?.setOnCheckedChangeListener { buttonView, isChecked ->
            edtTxtSpecialDeliveryInstruction?.visibility =
                if (isChecked) View.VISIBLE else View.GONE
        }

        switchGiftInstructions?.setOnCheckedChangeListener { buttonView, isChecked ->
            edtTxtGiftInstructions?.visibility =
                if (isChecked) View.VISIBLE else View.GONE
        }
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

    private fun initializeGrid(
        availableDeliverySlotsResponse: AvailableDeliverySlotsResponse?,
        weekNumber: Int
    ) {
        val deliverySlots = availableDeliverySlotsResponse?.sortedJoinDeliverySlots?.get(weekNumber)
        expandableGrid.apply {
            createTimingsGrid(deliverySlots?.hourSlots)
            createDatesGrid(deliverySlots?.headerDates)
            createTimeSlotGridView(deliverySlots, weekNumber)
        }
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

    private fun getAvailableDeliverySlots() {
        checkoutAddAddressNewUserViewModel.getAvailableDeliverySlots().observe(viewLifecycleOwner, {
            when (it.responseStatus) {
                ResponseStatus.SUCCESS -> {
                    loadingBar.visibility = View.GONE
                    if (it.data != null) {
                        selectedSlotResponse = it.data as? AvailableDeliverySlotsResponse
                        initializeGrid(selectedSlotResponse, 0)
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

    fun setSlotSelection(weekNumber: Int, position: Int, isSelected: Boolean) {
        val hrsSlotSize =
            selectedSlotResponse?.sortedJoinDeliverySlots?.get(weekNumber)?.hourSlots?.size ?: 0
        val weekPosition = position / hrsSlotSize
        val remainder = position % hrsSlotSize
        selectedSlotResponse = expandableGrid.setAllSlotSelection(selectedSlotResponse, false)
        selectedSlotResponse?.sortedJoinDeliverySlots?.get(weekNumber)?.week?.get(weekPosition)?.slots?.get(
            remainder
        )?.selected = isSelected
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.previousImgBtn -> {
                initializeGrid(selectedSlotResponse, 0)
            }
            R.id.nextImgBtn -> {
                initializeGrid(selectedSlotResponse, 1)
            }
        }
    }
}