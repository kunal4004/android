package za.co.woolworths.financial.services.android.ui.fragments.click_and_collect

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.edit_delivery_location_fragment.*
import za.co.woolworths.financial.services.android.models.dto.Province
import za.co.woolworths.financial.services.android.models.dto.ShoppingDeliveryLocation
import za.co.woolworths.financial.services.android.models.dto.Suburb
import za.co.woolworths.financial.services.android.ui.activities.click_and_collect.EditDeliveryLocationActivity
import za.co.woolworths.financial.services.android.ui.activities.click_and_collect.EditDeliveryLocationActivity.Companion.DELIVERY_TYPE
import za.co.woolworths.financial.services.android.ui.adapters.ProvinceDropdownAdapter
import za.co.woolworths.financial.services.android.ui.adapters.SuburbDropdownAdapter
import za.co.woolworths.financial.services.android.ui.views.actionsheet.ErrorDialogFragment
import za.co.woolworths.financial.services.android.util.DeliveryType
import za.co.woolworths.financial.services.android.util.Utils

class EditDeliveryLocationFragment : Fragment(), EditDeliveryLocationContract.EditDeliveryLocationView, View.OnClickListener {

    var navController: NavController? = null
    var bundle: Bundle? = null
    var regions: List<Province>? = null
    var presenter: EditDeliveryLocationContract.EditDeliveryLocationPresenter? = null
    var selectedProvince: Province? = null
    var selectedSuburb: Suburb? = null
    var deliveryType: DeliveryType = DeliveryType.DELIVERY

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.edit_delivery_location_fragment, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = EditDeliveryLocationPresenterImpl(this, EditDeliveryLocationInteractorImpl())
        bundle = arguments?.getBundle("bundle")
        bundle?.apply {
            deliveryType = DeliveryType.valueOf(getString(DELIVERY_TYPE, DeliveryType.DELIVERY.name))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        confirmLocation?.setOnClickListener(this)
        selectProvince?.setOnClickListener(this)
        selectSuburb?.setOnClickListener(this)
        tvSelectedProvince?.setOnClickListener(this)
        tvSelectedSuburb?.setOnClickListener(this)
        tvSelectedProvince?.keyListener = null
        tvSelectedSuburb?.keyListener = null
        delivery?.setOnClickListener(this)
        clickAndCollect?.setOnClickListener(this)
        confirmLocation?.setOnClickListener(this)
        setDeliveryOption(deliveryType)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.confirmLocation -> {
                selectedSuburb?.id?.let {
                    showSetSuburbProgressBar()
                    presenter?.initSetSuburb(it)
                }
            }
            R.id.selectProvince, R.id.tvSelectedProvince -> {
                if (selectedSuburb != null) resetSuburbSelection()
                getProvinces()
            }
            R.id.selectSuburb, R.id.tvSelectedSuburb -> {
                if (selectedProvince == null) return
                getSuburbs()
            }
            R.id.delivery -> setDeliveryOption(DeliveryType.DELIVERY)
            R.id.clickAndCollect -> setDeliveryOption(DeliveryType.STORE_PICKUP)
        }
    }

    override fun onGetProvincesSuccess(regions: List<Province>) {
        this.regions = regions
        hideGetProvincesProgress()
        val adapter = activity?.let { ProvinceDropdownAdapter(it, 0, regions, ::onProvinceSelected) }
        tvSelectedProvince.setAdapter(adapter)
        tvSelectedProvince.showDropDown()
    }

    override fun onGetProvincesFailure() {
        hideGetProvincesProgress()
        showErrorDialog()
    }

    override fun onGetSuburbsSuccess(suburbs: List<Suburb>) {
        hideGetSuburbProgress()
        val adapter = activity?.let { SuburbDropdownAdapter(it, 0, suburbs, ::onSuburbSelected) }
        tvSelectedSuburb.setAdapter(adapter)
        tvSelectedSuburb.showDropDown()
    }

    override fun onGetSuburbsFailure() {
        hideGetSuburbProgress()
        showErrorDialog()
    }

    override fun onGenericFailure() {
        hideGetSuburbProgress()
        hideGetProvincesProgress()
        hideSetSuburbProgressBar()
        showErrorDialog()
    }

    override fun getProvinces() {
        showGetProvincesProgress()
        presenter?.initGetProvinces()
    }

    override fun getSuburbs() {
        showGetSuburbProgress()
        selectedProvince?.id?.let { presenter?.initGetSuburbs(it, deliveryType) }
    }

    override fun showGetProvincesProgress() {
        dropdownGetProvinces?.visibility = View.INVISIBLE
        progressGetProvinces?.visibility = View.VISIBLE
    }

    override fun showGetSuburbProgress() {
        dropdownGetSuburb?.visibility = View.INVISIBLE
        progressGetSuburb?.visibility = View.VISIBLE
    }

    override fun hideGetProvincesProgress() {
        progressGetProvinces?.visibility = View.INVISIBLE
        dropdownGetProvinces?.visibility = View.VISIBLE
    }

    override fun hideGetSuburbProgress() {
        progressGetSuburb?.visibility = View.INVISIBLE
        dropdownGetSuburb?.visibility = View.VISIBLE
    }

    override fun showErrorDialog() {
        val dialog = ErrorDialogFragment.newInstance(activity?.resources?.getString(R.string.general_error_desc)
                ?: "")
        (activity as? AppCompatActivity)?.supportFragmentManager?.beginTransaction()?.let { fragmentTransaction -> dialog.show(fragmentTransaction, ErrorDialogFragment::class.java.simpleName) }
    }

    override fun onSetSuburbSuccess() {
        hideSetSuburbProgressBar()
        Utils.savePreferredDeliveryLocation(ShoppingDeliveryLocation(selectedProvince, selectedSuburb))
        bundle?.putString(DELIVERY_TYPE, deliveryType.name)
        bundle?.putString("SUBURB", Utils.toJson(selectedSuburb))
        navController?.navigate(R.id.action_to_editDeliveryLocationConfirmationFragment, bundleOf("bundle" to bundle))
    }

    override fun onSetSuburbFailure() {
        hideSetSuburbProgressBar()
        showErrorDialog()
    }

    private fun onProvinceSelected(province: Province?) {
        this.selectedProvince = province
        tvSelectedProvince?.setText(province?.name)
        tvSelectedProvince?.dismissDropDown()
    }

    private fun onSuburbSelected(suburb: Suburb?) {
        this.selectedSuburb = suburb
        tvSelectedSuburb?.setText(suburb?.name)
        tvSelectedSuburb?.dismissDropDown()
        validateConfirmLocationButtonAvailability()
    }


    private fun resetSuburbSelection() {
        selectedSuburb = null
        tvSelectedSuburb.setText(activity?.resources?.getString(if (deliveryType == DeliveryType.DELIVERY) R.string.select_a_suburb else R.string.select_a_store))
    }

    private fun setDeliveryOption(type: DeliveryType) {
        deliveryType = type
        when (type) {
            DeliveryType.DELIVERY -> {
                clickAndCollect?.setBackgroundResource(R.drawable.delivery_type_store_pickup_un_selected_bg)
                delivery?.setBackgroundResource(R.drawable.onde_dp_black_border_bg)
                if (selectedSuburb != null) {
                    if (selectedSuburb?.storePickup == true) {
                        resetSuburbSelection()
                    }
                } else {
                    tvSelectedSuburb.setText(activity?.resources?.getString(R.string.select_a_suburb))
                }
            }
            DeliveryType.STORE_PICKUP -> {
                clickAndCollect?.setBackgroundResource(R.drawable.onde_dp_black_border_bg)
                delivery?.setBackgroundResource(R.drawable.delivery_type_delivery_un_selected_bg)
                if (selectedSuburb != null) {
                    if (selectedSuburb?.storePickup == false) {
                        resetSuburbSelection()
                    }
                } else {
                    tvSelectedSuburb.setText(activity?.resources?.getString(R.string.select_a_store))
                }
            }
        }
        validateConfirmLocationButtonAvailability()
    }

    override fun validateConfirmLocationButtonAvailability() {
        confirmLocation?.isEnabled = (selectedProvince != null && selectedSuburb != null)
    }

    override fun hideSetSuburbProgressBar() {
        progressSetSuburb.visibility = View.INVISIBLE
        activity?.apply {
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }

    override fun showSetSuburbProgressBar() {
        progressSetSuburb.visibility = View.VISIBLE
        activity?.apply {
            window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }
}