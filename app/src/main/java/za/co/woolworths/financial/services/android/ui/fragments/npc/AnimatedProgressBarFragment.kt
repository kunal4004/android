package za.co.woolworths.financial.services.android.ui.fragments.npc

import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuInflater
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.extension.addFragment
import za.co.woolworths.financial.services.android.ui.extension.findFragmentByTag
import androidx.fragment.app.Fragment
import za.co.woolworths.financial.services.android.contracts.IProgressAnimationState

open class AnimatedProgressBarFragment : MyCardExtension(), IProgressAnimationState {

    fun showLoader() {
        (activity as? AppCompatActivity)?.addFragment(
                fragment = ProgressStateFragment.newInstance(this),
                tag = ProgressStateFragment::class.java.simpleName,
                containerViewId = R.id.flProgressIndicator
        )
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
    }

    fun progressState(): ProgressStateFragment? = (activity as? AppCompatActivity)?.findFragmentByTag(ProgressStateFragment::class.java.simpleName) as? ProgressStateFragment

    override fun onAnimationEnd(cardIsBlocked: Boolean) {
    }

    override fun onDestroy() {
        super.onDestroy()
        activity?.supportFragmentManager?.apply {
            if (findFragmentById(R.id.flProgressIndicator) != null) {
                findFragmentById(R.id.flProgressIndicator)?.let { beginTransaction().remove(it).commitAllowingStateLoss() }
            }
        }
    }
}