package za.co.woolworths.financial.services.android.ui.fragments.click_and_collect

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.edit_delivery_location_fragment.*
import za.co.woolworths.financial.services.android.models.dto.Province
import za.co.woolworths.financial.services.android.models.dto.Suburb
import za.co.woolworths.financial.services.android.ui.adapters.ProvinceDropdownAdapter
import za.co.woolworths.financial.services.android.ui.views.actionsheet.ErrorDialogFragment

class EditDeliveryLocationFragment : Fragment(), EditDeliveryLocationContract.EditDeliveryLocationView, View.OnClickListener {

    var navController: NavController? = null
    var bundle: Bundle? = null
    var regions: List<Province>? = null
    var presenter: EditDeliveryLocationContract.EditDeliveryLocationPresenter? = null
    var selectedProvince: Province? = null
    var selectedSuburb: Suburb? = null
    var dataList = arrayOf("vsfvsfsfsfsa", "dasfdsdsdsadsa", "sfsfsfscs", "sfsfsdfsdsdfsdfsw")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.edit_delivery_location_fragment, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = EditDeliveryLocationPresenterImpl(this, EditDeliveryLocationInteractorImpl())
        bundle = arguments?.getBundle("bundle")
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
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.confirmLocation -> {
                navController?.navigate(R.id.action_to_editDeliveryLocationConfirmationFragment, bundleOf("bundle" to bundle))
            }
            R.id.selectProvince, R.id.tvSelectedProvince -> {
                if (selectedSuburb != null) resetSuburbSelection()
                getProvinces()
            }
            R.id.selectSuburb, R.id.tvSelectedSuburb -> {
                if (selectedProvince == null) return
                getSuburbs()
            }
        }
    }

    override fun onGetProvincesSuccess(regions: List<Province>) {
        this.regions = regions
        hideGetProvincesProgress()
        var adapter = activity?.let { ProvinceDropdownAdapter(it, 0, regions, ::onProvinceSelected) }
        tvSelectedProvince.setAdapter(adapter)
        tvSelectedProvince.showDropDown()
    }

    override fun onGetProvincesFailure() {
        hideGetProvincesProgress()
        showErrorDialog()
    }

    override fun onGetSuburbsSuccess(suburbs: List<Suburb>) {
        hideGetSuburbProgress()
    }

    override fun onGetSuburbsFailure() {
        hideGetSuburbProgress()
        showErrorDialog()
    }

    override fun onGenericFailure() {
        hideGetSuburbProgress()
        hideGetProvincesProgress()
        showErrorDialog()
    }

    override fun getProvinces() {
        showGetProvincesProgress()
        presenter?.initGetProvinces()
    }

    override fun getSuburbs() {
        showGetSuburbProgress()
        selectedProvince?.id?.let { presenter?.initGetSuburbs(it) }
    }

    override fun showGetProvincesProgress() {
        dropdownGetProvinces?.visibility = View.GONE
        progressGetProvinces?.visibility = View.VISIBLE
    }

    override fun showGetSuburbProgress() {
        dropdownGetSuburb?.visibility = View.GONE
        progressGetSuburb?.visibility = View.VISIBLE
    }

    override fun hideGetProvincesProgress() {
        progressGetProvinces?.visibility = View.GONE
        dropdownGetProvinces?.visibility = View.VISIBLE
    }

    override fun hideGetSuburbProgress() {
        progressGetSuburb?.visibility = View.GONE
        dropdownGetSuburb?.visibility = View.VISIBLE
    }

    override fun showErrorDialog() {
        val dialog = ErrorDialogFragment.newInstance(activity?.resources?.getString(R.string.general_error_desc)
                ?: "")
        (activity as? AppCompatActivity)?.supportFragmentManager?.beginTransaction()?.let { fragmentTransaction -> dialog.show(fragmentTransaction, ErrorDialogFragment::class.java.simpleName) }
    }

    private fun onProvinceSelected(province: Province?) {
        this.selectedProvince = province
        tvSelectedProvince?.setText(province?.name)
        tvSelectedProvince?.dismissDropDown()
    }


    private fun resetSuburbSelection() {
        selectedSuburb = null
        tvSelectedSuburb.setText(activity?.resources?.getString(R.string.select_a_suburb))
    }
}