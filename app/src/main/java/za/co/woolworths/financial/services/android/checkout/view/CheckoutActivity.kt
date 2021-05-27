package za.co.woolworths.financial.services.android.checkout.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.activity_checkout.*

/**
 * Created by Kunal Uttarwar on 26/05/21.
 */
class CheckoutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)
        loadNavHostFragment()
    }

    private fun loadNavHostFragment() {
        val navHostFragment = navHostFragment as NavHostFragment
        val graph =
            navHostFragment.navController.navInflater.inflate(R.navigation.nav_graph_checkout)
        if (true)
            graph.startDestination = R.id.CheckoutAddAddressNewUserFragment
        else
            graph.startDestination = R.id.CheckoutAddAddressReturningUserFragment
        findNavController(R.id.navHostFragment)?.graph = graph
    }
}