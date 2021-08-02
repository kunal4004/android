package za.co.woolworths.financial.services.android.checkout.view

import android.R.attr.font
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.awfs.coordination.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.checkout_add_address_new_user.*
import kotlinx.android.synthetic.main.checkout_add_address_retuning_user.*
import kotlinx.android.synthetic.main.checkout_delivery_time_slot_selection_fragment.*
import kotlinx.android.synthetic.main.layout_delivering_to_details.*
import kotlinx.android.synthetic.main.layout_native_checkout_delivery_food_substitution.*
import kotlinx.android.synthetic.main.layout_native_checkout_delivery_instructions.*
import za.co.woolworths.financial.services.android.checkout.interactor.CheckoutAddAddressNewUserInteractor
import za.co.woolworths.financial.services.android.checkout.service.network.AvailableDeliverySlotsResponse
import za.co.woolworths.financial.services.android.checkout.service.network.CheckoutAddAddressNewUserApiHelper
import za.co.woolworths.financial.services.android.checkout.service.network.CheckoutMockApiHelper
import za.co.woolworths.financial.services.android.checkout.service.network.Slot
import za.co.woolworths.financial.services.android.checkout.service.network.*
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddressConfirmationFragment.Companion.SAVED_ADDRESS_KEY
import za.co.woolworths.financial.services.android.checkout.view.adapter.DeliverySlotsGridViewAdapter
import za.co.woolworths.financial.services.android.checkout.view.adapter.SlotsDateGridViewAdapter
import za.co.woolworths.financial.services.android.checkout.view.adapter.SlotsTimeGridViewAdapter
import za.co.woolworths.financial.services.android.checkout.viewmodel.CheckoutAddAddressNewUserViewModel
import za.co.woolworths.financial.services.android.checkout.viewmodel.ViewModelFactory
import za.co.woolworths.financial.services.android.service.network.ResponseStatus
import za.co.woolworths.financial.services.android.util.Utils


/**
 * Created by Kunal Uttarwar on 27/05/21.
 */
class CheckoutAddAddressReturningUserFragment : Fragment(), View.OnClickListener {

    private lateinit var checkoutAddAddressNewUserViewModel: CheckoutAddAddressNewUserViewModel
    private val expandableGrid = ExpandableGrid(this)
    private var selectedSlotResponse: AvailableDeliverySlotsResponse? = null
    private var selectedFoodSlot = Slot()

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
        initializeDeliveringToView()
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

    private fun initializeDeliveringToView() {
        arguments?.apply {
            context?.let { context ->
                val savedAddress = getSerializable(SAVED_ADDRESS_KEY) as? SavedAddressResponse
                savedAddress?.let { savedAddresses ->

                    val deliveringToAddress = SpannableStringBuilder()
                    // default address nickname
                    val defaultAddressNickname =
                        SpannableString(
                            savedAddresses.defaultAddressNickname + " " + context.getString(
                                R.string.bullet
                            ) + " "
                        )
                    val typeface = ResourcesCompat.getFont(context, R.font.myriad_pro_semi_bold)
                    defaultAddressNickname.setSpan(
                        StyleSpan(typeface!!.style),
                        0, defaultAddressNickname.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    defaultAddressNickname.setSpan(ForegroundColorSpan(Color.BLACK), 0, defaultAddressNickname.length
                        , Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                    deliveringToAddress.append(defaultAddressNickname)

                    // Extract default address display name
                    savedAddresses.addresses?.forEach { address ->
                        if(savedAddresses.defaultAddressNickname.equals(address.nickname)){
                            val addressName = SpannableString(address?.displayName)
                            val typeface1 = ResourcesCompat.getFont(context, R.font.myriad_pro_regular)
                            addressName.setSpan(
                                StyleSpan(typeface1!!.style),
                                0, addressName.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                            deliveringToAddress.append(addressName)
                            return@forEach
                        }
                    }
                    tvNativeCheckoutDeliveringValue?.text = deliveringToAddress

                    checkoutDeliveryDetailsLayout?.setOnClickListener(this@CheckoutAddAddressReturningUserFragment)

                }
            }
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
                    /*if (it.data != null) {
                       selectedSlotResponse = it.data as? AvailableDeliverySlotsResponse
                        initializeGrid(selectedSlotResponse, 0)
                    }*/

                    //use mock data from json file
                    val jsonFileString = Utils.getJsonDataFromAsset(
                        activity?.applicationContext,
                        "mocks/confirmDelivery_Response.json"
                    )
                    val mockDeliverySlotResponse: AvailableDeliverySlotsResponse = Gson().fromJson(
                        jsonFileString,
                        object : TypeToken<AvailableDeliverySlotsResponse>() {}.type
                    )
                    selectedSlotResponse = mockDeliverySlotResponse
                    initializeGrid(selectedSlotResponse, 0)
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

    fun getSelectedSlotResponse(): AvailableDeliverySlotsResponse? {
        return selectedSlotResponse
    }

    fun setSelectedSlotResponse(availableDeliverySlotsResponse: AvailableDeliverySlotsResponse?) {
        selectedSlotResponse = availableDeliverySlotsResponse
    }

    fun setSelectedFoodSlot(selectedSlot: Slot){
        this.selectedFoodSlot = selectedSlot
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.previousImgBtn -> {
                initializeGrid(selectedSlotResponse, 0)
            }
            R.id.nextImgBtn -> {
                initializeGrid(selectedSlotResponse, 1)
            }
            R.id.checkoutDeliveryDetailsLayout -> {
                view?.findNavController()?.navigate(
                    R.id.action_CheckoutAddAddressReturningUserFragment_to_checkoutAddressConfirmationFragment, arguments)
            }
        }
    }
}