package za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated

import android.app.Dialog
import android.content.res.Resources
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.crashlytics.android.Crashlytics
import com.daasuu.bl.BubbleLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_prodcut_nutritional_information.*
import za.co.woolworths.financial.services.android.models.dto.NutritionalInformationDetails
import za.co.woolworths.financial.services.android.models.dto.NutritionalInformationFilterOption
import za.co.woolworths.financial.services.android.models.dto.NutritionalTableItem
import za.co.woolworths.financial.services.android.ui.adapters.NutritionalInformationFilterAdapter
import za.co.woolworths.financial.services.android.ui.adapters.NutritionalInformationListAdapter
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.util.Utils

class ProductNutritionalInformationFragment : Fragment(), NutritionalInformationFilterAdapter.FilterOptionSelection {
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_prodcut_nutritional_information, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    fun initViews() {
        activity?.apply {
            nutritionalInformationList.layoutManager = LinearLayoutManager(this)
        }
        filterOptionSelector.setOnClickListener { showFilterOption() }
        configureUI()
    }

    fun configureUI() {

        nutritionalDataList = buildNutritionalInfoData()
        filterOptions = buildFilterOptions(nutritionalDataList)

        if (filterOptions.isNotEmpty()) {
            nutritionalInformationList.adapter = adapter
            filterOptions[0].isSelected = true
            onOptionSelected(filterOptions[0])
        }
    }

    private fun buildNutritionalInfoData(): HashMap<String, List<NutritionalTableItem>> {
        try {
            nutritionalInfo?.apply {
                nutritionalTable.forEach {
                    nutritionalDataList.putAll(Gson().fromJson(it, object : TypeToken<HashMap<String, List<NutritionalTableItem>>>() {}.type))
                }
            }
        } catch (e: Exception) {
            Crashlytics.logException(e)
        }

        return nutritionalDataList
    }

    private fun buildFilterOptions(nutritionalDataList: HashMap<String, List<NutritionalTableItem>>): ArrayList<NutritionalInformationFilterOption> {
        nutritionalDataList.keys.reversed().forEach {
            filterOptions.add(NutritionalInformationFilterOption(it))
        }
        return filterOptions
    }

    override fun onOptionSelected(filterOption: NutritionalInformationFilterOption) {
        filterOptionDialog?.dismiss()
        filterOptionSelector.text = filterOption.name
        nutritionalDataList[filterOption.name]?.let { adapter.updateData(it) }
    }

    private fun showFilterOption() {
        filterOptionDialog = activity?.let { activity -> Dialog(activity) }
        filterOptionDialog?.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            val view = layoutInflater.inflate(R.layout.nutrition_filter_option_popup_view, null)
            val rcvSortOptions = view.findViewById<RecyclerView>(R.id.sortOptionsList)
            val popupLayout = view.findViewById<BubbleLayout>(R.id.popupLayout)
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

}