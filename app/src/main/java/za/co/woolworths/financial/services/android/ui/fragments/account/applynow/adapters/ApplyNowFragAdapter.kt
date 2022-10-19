package za.co.woolworths.financial.services.android.ui.fragments.account.applynow.adapters

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import za.co.woolworths.financial.services.android.ui.fragments.account.applynow.fragments.ARG_POSITION
import za.co.woolworths.financial.services.android.ui.fragments.account.applynow.fragments.ApplyNowFragment

class ApplyNowFragAdapter(fragment: FragmentActivity,val count: Int) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = count

    override fun createFragment(position: Int): Fragment {
        val fragment = ApplyNowFragment()
        fragment.arguments = Bundle().apply {
            putInt(ARG_POSITION, position )
        }
        return fragment
    }
}