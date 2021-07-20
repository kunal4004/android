package za.co.woolworths.financial.services.android.checkout.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.layout_native_checkout_delivery_instructions.*
import za.co.woolworths.financial.services.android.util.Utils
import kotlinx.android.synthetic.main.checkout_delivery_time_slot_selection_fragment.*
import kotlinx.android.synthetic.main.layout_native_checkout_delivery_food_substitution.*

/**
 * Created by Kunal Uttarwar on 27/05/21.
 */
class CheckoutAddAddressReturningUserFragment : Fragment(), View.OnClickListener {

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
        previousImgBtn.setOnClickListener(this)
        nextImgBtn.setOnClickListener(this)
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