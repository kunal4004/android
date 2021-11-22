package za.co.woolworths.financial.services.android.checkout.view

import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.facebook.shimmer.ShimmerFrameLayout
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.powermock.modules.junit4.PowerMockRunner
import za.co.woolworths.financial.services.android.checkout.viewmodel.CheckoutAddAddressNewUserViewModel
import za.co.woolworths.financial.services.android.utils.mock

/**
 * Created by Kunal Uttarwar on 22/11/21.
 */

@ExperimentalCoroutinesApi
@RunWith(PowerMockRunner::class)
class CheckoutReturningUserCollectionFragmentTest : Fragment() {

    private lateinit var checkoutReturningUserCollectionFragment: CheckoutReturningUserCollectionFragment
    private lateinit var checkoutAddAddressNewUserViewModel: CheckoutAddAddressNewUserViewModel

    @Before
    fun init() {
        checkoutReturningUserCollectionFragment =
            mock(CheckoutReturningUserCollectionFragment::class.java, Mockito.CALLS_REAL_METHODS)
        checkoutAddAddressNewUserViewModel = mock()
        checkoutReturningUserCollectionFragment.testSetViewModelInstance(
            checkoutAddAddressNewUserViewModel
        )
    }

    @Test
    fun checkStopShimmerView() {
        var shimmerComponentArray: List<Pair<ShimmerFrameLayout, View>>
        var mockShimmerFrameLayout: ShimmerFrameLayout = mock()
        var mockTextView: TextView = mock(TextView::class.java)
        shimmerComponentArray = listOf(
            Pair(
                mockShimmerFrameLayout,
                mockTextView
            )
        )
        checkoutReturningUserCollectionFragment.testSetShimmerArray(shimmerComponentArray)
        checkoutReturningUserCollectionFragment.startShimmerView()
        checkoutReturningUserCollectionFragment.stopShimmerView()
        Mockito.verify(checkoutReturningUserCollectionFragment, Mockito.times(1))
            .initializeFoodSubstitution()
        Mockito.verify(checkoutReturningUserCollectionFragment, Mockito.times(1))
            .initializeDeliveryInstructions()
    }
}