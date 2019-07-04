package za.co.woolworths.financial.services.android.ui.fragments.npc

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.replace_card_fragment.*
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.StoreDetails
import za.co.woolworths.financial.services.android.ui.activities.WStockFinderActivity
import za.co.woolworths.financial.services.android.ui.activities.card.MyCardDetailActivity
import za.co.woolworths.financial.services.android.util.Utils

class GetReplacementCardFragment : MyCardExtension() {

    companion object {
        fun newInstance() = GetReplacementCardFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.replace_card_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let { Utils.updateStatusBarBackground(it) }
        updateToolbarBg()
        tvAlreadyHaveCard?.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        tvAlreadyHaveCard?.setOnClickListener { (activity as? AppCompatActivity)?.apply { navigateToLinkNewCardActivity(this) } }
        btnParticipatingStores?.setOnClickListener { navigateToParticipatingStores() }
    }

    private fun updateToolbarBg() {
        (activity as? MyCardDetailActivity)?.apply {
            hideToolbarTitle()
            changeToolbarBackground(R.color.white)
        }
    }

    private fun navigateToParticipatingStores() {
        activity?.apply {
            val location = "[\n" +
                    "    {\n" +
                    "      \"id\": 127,\n" +
                    "      \"name\": \"Canal Walk\",\n" +
                    "      \"latitude\": -33.8944,\n" +
                    "      \"longitude\": 18.5107,\n" +
                    "      \"distance\": \"74.45614687697031\",\n" +
                    "      \"phoneNumber\": \"0860 022 002\",\n" +
                    "      \"address\": \"Canal Walk Shopping Centre,Century Boulevard,Century City,7441\",\n" +
                    "      \"planningRegionID\": 10,\n" +
                    "      \"offerings\": [\n" +
                    "        {\n" +
                    "          \"offering\": \"Country Road\",\n" +
                    "          \"type\": \"Brand\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"offering\": \"Trenery\",\n" +
                    "          \"type\": \"Brand\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"offering\": \"Beauty\",\n" +
                    "          \"type\": \"Department\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"offering\": \"Clothing\",\n" +
                    "          \"type\": \"Department\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"offering\": \"Financial Services\",\n" +
                    "          \"type\": \"Department\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"offering\": \"Foods\",\n" +
                    "          \"type\": \"Department\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"offering\": \"Homeware\",\n" +
                    "          \"type\": \"Department\"\n" +
                    "        }\n" +
                    "      ],\n" +
                    "      \"times\": [\n" +
                    "        {\n" +
                    "          \"day\": \"Today\",\n" +
                    "          \"hours\": \"09h00 - 21h00\",\n" +
                    "          \"exception\": false\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"day\": \"Thursday\",\n" +
                    "          \"hours\": \"09h00 - 21h00\",\n" +
                    "          \"exception\": false\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"day\": \"Friday\",\n" +
                    "          \"hours\": \"09h00 - 21h00\",\n" +
                    "          \"exception\": false\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"day\": \"Saturday\",\n" +
                    "          \"hours\": \"09h00 - 21h00\",\n" +
                    "          \"exception\": false\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"day\": \"Sunday\",\n" +
                    "          \"hours\": \"09h00 - 21h00\",\n" +
                    "          \"exception\": false\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"day\": \"Monday\",\n" +
                    "          \"hours\": \"09h00 - 21h00\",\n" +
                    "          \"exception\": false\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"day\": \"Tuesday\",\n" +
                    "          \"hours\": \"09h00 - 21h00\",\n" +
                    "          \"exception\": false\n" +
                    "        }\n" +
                    "      ],\n" +
                    "      \"status\": \"GREEN\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"id\": 272,\n" +
                    "      \"name\": \"Colosseum\",\n" +
                    "      \"latitude\": -33.8929,\n" +
                    "      \"longitude\": 18.5058,\n" +
                    "      \"distance\": \"480.6295141390415\",\n" +
                    "      \"phoneNumber\": \"0860 022 002\",\n" +
                    "      \"address\": \"The Colosseum Shopping Centre,Century Blvd,Century City,7441\",\n" +
                    "      \"planningRegionID\": 10,\n" +
                    "      \"offerings\": [\n" +
                    "        {\n" +
                    "          \"offering\": \"Foods\",\n" +
                    "          \"type\": \"Department\"\n" +
                    "        }\n" +
                    "      ],\n" +
                    "      \"times\": [\n" +
                    "        {\n" +
                    "          \"day\": \"Today\",\n" +
                    "          \"hours\": \"08h00 - 20h00\",\n" +
                    "          \"exception\": false\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"day\": \"Thursday\",\n" +
                    "          \"hours\": \"08h00 - 20h00\",\n" +
                    "          \"exception\": false\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"day\": \"Friday\",\n" +
                    "          \"hours\": \"08h00 - 20h00\",\n" +
                    "          \"exception\": false\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"day\": \"Saturday\",\n" +
                    "          \"hours\": \"08h00 - 20h00\",\n" +
                    "          \"exception\": false\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"day\": \"Sunday\",\n" +
                    "          \"hours\": \"08h00 - 20h00\",\n" +
                    "          \"exception\": false\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"day\": \"Monday\",\n" +
                    "          \"hours\": \"08h00 - 20h00\",\n" +
                    "          \"exception\": false\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"day\": \"Tuesday\",\n" +
                    "          \"hours\": \"08h00 - 20h00\",\n" +
                    "          \"exception\": false\n" +
                    "        }\n" +
                    "      ],\n" +
                    "      \"status\": \"GREEN\"\n" +
                    "    }\n" +
                    "  ]"

            val listType = object : TypeToken<List<StoreDetails>>() {}.type
            WoolworthsApplication.getInstance().wGlobalState.storeDetailsArrayList = Gson().fromJson(location, listType)
            val intentInStoreFinder = Intent(this, WStockFinderActivity::class.java)
            intentInStoreFinder.putExtra("PRODUCT_NAME", getString(R.string.participating_stores))
            intentInStoreFinder.putExtra("CONTACT_INFO", getString(R.string.participating_store_desc))

            startActivity(intentInStoreFinder)
            overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left)
        }
    }

    override fun onResume() {
        super.onResume()
        hideKeyboard()
    }
}