package za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated

import android.app.Dialog
import android.content.res.Resources
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.awfs.coordination.databinding.FragmentProdcutNutritionalInformationBinding
import com.daasuu.bl.BubbleLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dto.NutritionalInformationDetails
import za.co.woolworths.financial.services.android.models.dto.NutritionalInformationFilterOption
import za.co.woolworths.financial.services.android.models.dto.NutritionalTableItem
import za.co.woolworths.financial.services.android.ui.adapters.NutritionalInformationFilterAdapter
import za.co.woolworths.financial.services.android.ui.adapters.NutritionalInformationListAdapter
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import za.co.woolworths.financial.services.android.util.binding.BaseFragmentBinding

class ProductNutritionalInformationFragment : BaseFragmentBinding<FragmentProdcutNutritionalInformationBinding>(FragmentProdcutNutritionalInformationBinding::inflate), NutritionalInformationFilterAdapter.FilterOptionSelection {
    var nutritionalInfo: NutritionalInformationDetails? = null
    private var nutritionalDataList: HashMap<String, List<NutritionalTableItem>> = HashMap()
    private var adapter: NutritionalInformationListAdapter = NutritionalInformationListAdapter()
    private var filterOptions: ArrayList<NutritionalInformationFilterOption> = arrayListOf()
    private var filterOptionDialog: Dialog? = null

    companion object {
        fun newInstance(nutritionalInfo: String?) = ProductNutritionalInformationFragment().withArgs {
            putString("NUTRITIONAL_INFO", nutritionalInfo)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.apply {
            nutritionalInfo = Utils.jsonStringToObject(getString("NUTRITIONAL_INFO"), NutritionalInformationDetails::class.java) as NutritionalInformationDetails?
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        setUniqueIds()
    }

    fun initViews() {
        activity?.apply {
            binding.nutritionalInformationList.layoutManager = LinearLayoutManager(this)
        }
        binding.filterOptionSelector.setOnClickListener {
            showFilterOption()
        }
        configureUI()
    }

    fun configureUI() {

        nutritionalDataList = buildNutritionalInfoData()
        filterOptions = buildFilterOptions(nutritionalDataList)

        if (filterOptions.isNotEmpty()) {
            binding.nutritionalInformationList.adapter = adapter
            filterOptions[0].isSelected = true
            onOptionSelected(filterOptions[0])
        }
    }

    private fun buildNutritionalInfoData(): HashMap<String, List<NutritionalTableItem>> {
        try {
            nutritionalInfo?.apply {
                nutritionalTable.forEach {
                  val data:HashMap<String, List<NutritionalTableItem>> =  Gson().fromJson(it, object : TypeToken<HashMap<String, List<NutritionalTableItem>>>() {}.type)
                    data.keys.toTypedArray()[0].let {key->
                        data[key]?.let { it1 -> nutritionalDataList.put(key, it1) }
                    }
                }
            }
        } catch (e: Exception) {
            FirebaseManager.logException(e)
        }

        return nutritionalDataList
    }

    private fun buildFilterOptions(nutritionalDataList: HashMap<String, List<NutritionalTableItem>>): ArrayList<NutritionalInformationFilterOption> {
        nutritionalDataList.keys.reversed().forEach {
            filterOptions.add(NutritionalInformationFilterOption(it))
        }
        return filterOptions
    }

    override fun onOptionSelected(selectedSortedOption: NutritionalInformationFilterOption) {
        val arguments = HashMap<String, String>()
        arguments[FirebaseManagerAnalyticsProperties.PropertyNames.NUTRITIONAL_INFORMATION_FILTER_OPTION] = selectedSortedOption.name
        activity?.apply { Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.SHOP_PRODUCTDETAIL_NUTRITIONAL_INFORMATION, arguments, this) }
        filterOptionDialog?.dismiss()
        binding.filterOptionSelector?.text = selectedSortedOption.name
        nutritionalDataList[selectedSortedOption.name]?.let { adapter.updateData(it) }
    }

    private fun showFilterOption() {
        filterOptionDialog = activity?.let { activity -> Dialog(activity) }
        filterOptionDialog?.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            val view = layoutInflater.inflate(R.layout.nutrition_filter_option_popup_view, null)
            val rcvSortOptions = view.findViewById<RecyclerView>(R.id.sortOptionsList)
            val popupLayout = view.findViewById<BubbleLayout>(R.id.popupLayout)
            view.findViewById<View>(R.id.emptyView).setOnClickListener { this.dismiss() }
            popupLayout.arrowPosition = (Resources.getSystem().displayMetrics.widthPixels * 3 / 4).toFloat()
            rcvSortOptions?.layoutManager = activity?.let { activity -> LinearLayoutManager(activity) }
            rcvSortOptions?.adapter = NutritionalInformationFilterAdapter(filterOptions, this@ProductNutritionalInformationFragment)
            setContentView(view)
            window?.apply {
                setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
                setBackgroundDrawableResource(R.color.transparent)
                setGravity(Gravity.TOP)
            }

            setTitle(null)
            setCancelable(true)
            show()
        }
    }

    private fun setUniqueIds() {
        binding.apply {
            pageTitle?.contentDescription = getString(R.string.pdp_nutritionalInformationTitle)
            description?.contentDescription = getString(R.string.pdp_descriptionTitle)
            filterOptionSelector?.contentDescription = getString(R.string.pdp_filterOptionSelector)
            nutritionalInformationList?.contentDescription = getString(R.string.pdp_nutritionLayout)
        }
    }

}