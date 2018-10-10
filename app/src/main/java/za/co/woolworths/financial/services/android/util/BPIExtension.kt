package za.co.woolworths.financial.services.android.util

import android.content.Intent
import android.support.annotation.AnimRes
import android.support.annotation.IdRes
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import com.google.gson.Gson
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.BPIOverview
import za.co.woolworths.financial.services.android.models.dto.InsuranceType
import za.co.woolworths.financial.services.android.ui.activities.BalanceProtectionActivity
import java.util.*

/**
 * Method to add the fragment. The [fragment] is added to the container view with id
 * [containerViewId] and a [tag]. The operation is performed by the supportFragmentManager.
 * This method checks if fragment exists.
 * @return the fragment added.
 */
fun <T : Fragment> AppCompatActivity.addFragment(fragment: T?,
                                                 tag: String,
                                                 allowStateLoss: Boolean = false,
                                                 @IdRes containerViewId: Int,
                                                 @AnimRes enterAnimation: Int = 0,
                                                 @AnimRes exitAnimation: Int = 0,
                                                 @AnimRes popEnterAnimation: Int = 0,
                                                 @AnimRes popExitAnimation: Int = 0): T? {
    if (!existsFragmentByTag(tag)) {
        val ft = supportFragmentManager.beginTransaction()
        ft.setCustomAnimations(enterAnimation, exitAnimation, popEnterAnimation, popExitAnimation)
        ft.add(containerViewId, fragment, tag)
        if (!supportFragmentManager.isStateSaved) {
            ft.commit()
        } else if (allowStateLoss) {
            ft.commitAllowingStateLoss()
        }
        return fragment
    }
    return findFragmentByTag(tag) as T
}

/**
 * Method to replace the fragment. The [fragment] is added to the container view with id
 * [containerViewId] and a [tag]. The operation is performed by the supportFragmentManager.
 */
fun Fragment.replaceFragment(fragment: Fragment,
                             tag: String,
                             allowStateLoss: Boolean = false,
                             @IdRes containerViewId: Int,
                             @AnimRes enterAnimation: Int = 0,
                             @AnimRes exitAnimation: Int = 0,
                             @AnimRes popEnterAnimation: Int = 0,
                             @AnimRes popExitAnimation: Int = 0) {
    if (activity != null) {
        val ft = activity.supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(enterAnimation, exitAnimation, popEnterAnimation, popExitAnimation)
                .addToBackStack(fragment.javaClass.name)
                .replace(containerViewId, fragment, tag)
        if (!activity.supportFragmentManager.isStateSaved) {
            ft.commit()
        } else if (allowStateLoss) {
            ft.commitAllowingStateLoss()
        }
    }
}

/**
 * Method to check if fragment exists. The operation is performed by the supportFragmentManager.
 */
fun AppCompatActivity.existsFragmentByTag(tag: String): Boolean {
    return supportFragmentManager.findFragmentByTag(tag) != null
}

/**
 * Method to get fragment by tag. The operation is performed by the supportFragmentManager.
 */
fun AppCompatActivity.findFragmentByTag(tag: String): Fragment? {
    return supportFragmentManager.findFragmentByTag(tag)
}

fun Fragment.navigateToBalanceProtectionActivity() {
    if (activity == null) return
    val deathCoverIntent = Intent(activity, BalanceProtectionActivity::class.java)
    startActivity(deathCoverIntent)
    activity.overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left)
}

// Set top and bottom margin for bpi overview adapter row
fun View.setOverviewConstraint(position: Int, topMargin: Int?, bottomMargin: Int?) {
    val marginTop = context.resources.getDimension(topMargin!!).toInt()
    val marginBottom = context.resources.getDimension(bottomMargin!!).toInt()
    val params = layoutParams as ViewGroup.MarginLayoutParams
    params.setMargins(params.leftMargin, if (position == 0) marginTop else params.topMargin, params.rightMargin, if (position == 3) marginBottom else params.bottomMargin)
    layoutParams = params
}

fun Fragment.createBPIList(): MutableList<BPIOverview>? {
    return Arrays.asList(BPIOverview(resources.getString(R.string.bpi_balance_protection_title), resources.getString(R.string.bpi_balance_protection_desc), R.drawable.icon_balance_protection_overview, resources.getStringArray(R.array.bpi_balance_protection_benefits), InsuranceType(), R.drawable.bg_header_balance_protection), BPIOverview(resources.getString(R.string.bpi_partner_cover_title), resources.getString(R.string.bpi_partner_cover_desc), R.drawable.icon_partner_cover, resources.getStringArray(R.array.bpi_partner_cover_benefits), InsuranceType(), R.drawable.bg_header_partner_cover), BPIOverview(resources.getString(R.string.bpi_additional_death_cover_title), resources.getString(R.string.bpi_additional_death_cover_desc), R.drawable.icon_additional_death_cover, resources.getStringArray(R.array.bpi_additional_death_cover), InsuranceType(), R.drawable.bg_header_additional_death_cover), BPIOverview(resources.getString(R.string.bpi_additional_death_cover_for_partner_title), resources.getString(R.string.bpi_additional_death_cover_for_partner_desc), R.drawable.icon_additional_death_cover_for_partner, resources.getStringArray(R.array.bpi_additional_death_cover_for_partner), InsuranceType(), R.drawable.bg_header_additional_death_cover_for_partner))
}

fun Fragment.getInsuranceType(): MutableList<InsuranceType>? {
    if (arguments != null) {
        if (arguments.containsKey("accountInfo")) {
            val account: Account? = Gson().fromJson(arguments.get("accountInfo") as String, Account::class.java)
            return account!!.insuranceTypes
        }
    }
    return null
}

fun Fragment.updateBPIList(): MutableList<BPIOverview>? {
    val bpiList = createBPIList()
    val insuranceListType = getInsuranceType()!!
    for (insuranceType in insuranceListType) {
        for (bpi in bpiList!!) {
            if (bpi.overviewTitle == insuranceType.description) {
                val type: InsuranceType = bpi.insuranceType!!
                type.covered = insuranceType.covered
                type.description = insuranceType.description
                type.effectiveDate = insuranceType.effectiveDate
            }
        }
    }
    return bpiList
}
