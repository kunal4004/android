package za.co.woolworths.financial.services.android.checkout.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.checkout_delivery_time_slot_selection_fragment.*
import kotlinx.android.synthetic.main.layout_native_checkout_delivery_food_substitution.*
import za.co.woolworths.financial.services.android.checkout.view.adapter.DeliveryGridViewAdapter
import za.co.woolworths.financial.services.android.checkout.viewmodel.DeliveryGridModel


/**
 * Created by Kunal Uttarwar on 27/05/21.
 */
class CheckoutAddAddressReturningUserFragment : Fragment(), View.OnClickListener {

    companion object{
        const val GRID_COLUMNS_COUNT = 3
    }


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
        initializeGrid()
        previousImgBtn.setOnClickListener(this)
        nextImgBtn.setOnClickListener(this)
    }

    private fun initializeGrid() {
        val deliveryGridList: ArrayList<DeliveryGridModel> = ArrayList()
        deliveryGridList. add(DeliveryGridModel("DSA", R.color.selected_address_background_color, 1, false))
        deliveryGridList.add(DeliveryGridModel("JAVA", R.color.selected_address_background_color, 2, false))
        deliveryGridList.add(DeliveryGridModel("C++", R.color.selected_address_background_color, 3, false))
        deliveryGridList.add(DeliveryGridModel("Python", R.color.selected_address_background_color, 4, false))
        deliveryGridList.add(DeliveryGridModel("Javascript", R.color.selected_address_background_color, 5, false))
        deliveryGridList.add(DeliveryGridModel("DSA", R.color.selected_address_background_color, 6, false))

        val adapter = context?.let { DeliveryGridViewAdapter(it, R.layout.delivery_grid_card_item, deliveryGridList) }
        timeSlotsGridView.numColumns = GRID_COLUMNS_COUNT
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

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.previousImgBtn -> {

            }
            R.id.nextImgBtn -> {

            }
        }
    }
}