package za.co.woolworths.financial.services.android.ui.fragments.click_and_collect

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.unsellable_items_fragment.*
import za.co.woolworths.financial.services.android.checkout.view.CheckoutActivity
import za.co.woolworths.financial.services.android.geolocation.viewmodel.UnSellableItemsLiveData
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.ui.adapters.UnsellableItemsListAdapter
import za.co.woolworths.financial.services.android.util.Utils

class UnsellableItemsFragment : Fragment(), View.OnClickListener {

    var selectedSuburb: Suburb? = null
    var selectedProvince: Province? = null
    var bundle: Bundle? = null
    private var fromScreenName: String? = ""
    private var commerceItems: ArrayList<UnSellableCommerceItem>? = null
    var navController: NavController? = null
    companion object {
        const val KEY_ARGS_BUNDLE = "bundle"
        const val KEY_ARGS_SUBURB = "SUBURB"
        const val KEY_ARGS_PROVINCE = "PROVINCE"
        const val KEY_ARGS_SCREEN_NAME = "SCREEN_NAME"
        const val KEY_ARGS_UNSELLABLE_COMMERCE_ITEMS = "UnSellableCommerceItems"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bundle = arguments?.getBundle(KEY_ARGS_BUNDLE)
        bundle?.apply {
            selectedSuburb = Utils.jsonStringToObject(getString(KEY_ARGS_SUBURB), Suburb::class.java) as Suburb?
            selectedProvince = Utils.jsonStringToObject(getString(KEY_ARGS_PROVINCE), Province::class.java) as Province?
            commerceItems = Gson().fromJson(getString(KEY_ARGS_UNSELLABLE_COMMERCE_ITEMS), object : TypeToken<List<UnSellableCommerceItem>>() {}.type)
            fromScreenName = getString(KEY_ARGS_SCREEN_NAME)
        }
        (activity as? CheckoutActivity)?.apply {
            showBackArrowWithoutTitle()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.unsellable_items_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        removeItems?.setOnClickListener(this)
        if(activity is CheckoutActivity) {
            initCheckoutUnsellableItemsView()
        } else {
            changeStore?.apply {
                visibility = View.VISIBLE
                paintFlags = Paint.UNDERLINE_TEXT_FLAG
                setOnClickListener(this@UnsellableItemsFragment)
            }
        }

        loadUnsellableItems()
    }

    /**
     * This function will get called when navigated from checkout page
     */
    private fun initCheckoutUnsellableItemsView() {
        changeStore?.visibility = View.INVISIBLE
        unsellableItemsFragmentRelativeLayout?.background =
            context?.let { ContextCompat.getDrawable(it, R.color.white) }
    }

    private fun loadUnsellableItems() {
        rcvItemsList?.layoutManager = LinearLayoutManager(activity)
        commerceItems?.let { rcvItemsList?.adapter = UnsellableItemsListAdapter(it) }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.changeStore -> {
                /*CANCEl button */
                UnSellableItemsLiveData.value = false
                confirmRemoveItems()
            }
            //R.id.removeItems ->  executeSetSuburb()
            R.id.removeItems -> {
                UnSellableItemsLiveData.value = true
                confirmRemoveItems()
            }
        }
    }

    private fun confirmRemoveItems() {
         navController?.navigateUp()
    }
}