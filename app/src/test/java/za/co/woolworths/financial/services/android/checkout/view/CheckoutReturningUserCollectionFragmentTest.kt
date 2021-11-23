package za.co.woolworths.financial.services.android.checkout.view

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.TelephonyManager
import android.view.View
import android.widget.TextView
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import com.facebook.shimmer.ShimmerFrameLayout
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.powermock.modules.junit4.PowerMockRunner
import za.co.woolworths.financial.services.android.checkout.interactor.CheckoutAddAddressNewUserInteractor
import za.co.woolworths.financial.services.android.checkout.service.network.CheckoutAddAddressNewUserApiHelper
import za.co.woolworths.financial.services.android.checkout.viewmodel.CheckoutAddAddressNewUserViewModel
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.network.ApiInterface
import za.co.woolworths.financial.services.android.models.network.RetrofitConfig
import za.co.woolworths.financial.services.android.models.network.StorePickupInfoBody
import za.co.woolworths.financial.services.android.util.OneTimeObserver
import za.co.woolworths.financial.services.android.utils.TestCoroutineRule
import za.co.woolworths.financial.services.android.utils.mock
import za.co.woolworths.financial.services.android.utils.setFinalStatic


/**
 * Created by Kunal Uttarwar on 22/11/21.
 */

@ExperimentalCoroutinesApi
@RunWith(PowerMockRunner::class)
class CheckoutReturningUserCollectionFragmentTest : Fragment() {

    @get:Rule
    val testInstantTaskExecutorRule: TestRule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private lateinit var checkoutReturningUserCollectionFragment: CheckoutReturningUserCollectionFragment
    private lateinit var checkoutAddAddressNewUserViewModel: CheckoutAddAddressNewUserViewModel
    private lateinit var checkoutAddAddressNewUserInteractor: CheckoutAddAddressNewUserInteractor
    private lateinit var instrumentationContext: Context
    private lateinit var mockApiInterface: ApiInterface
    private val packageName = "za.co.woolworths.financial.services.android.models"

    @Before
    fun init() {
        checkoutReturningUserCollectionFragment =
            mock(CheckoutReturningUserCollectionFragment::class.java, CALLS_REAL_METHODS)

        instrumentationContext = mock(Context::class.java, RETURNS_DEEP_STUBS)
        mockApiInterface = mock()
        RetrofitConfig.mApiInterface = mockApiInterface
        mock(Build::class.java)
        mock(RetrofitConfig::class.java)

        WoolworthsApplication.testSetInstance(mock())
        `when`(WoolworthsApplication.getInstance().getPackageName()).thenReturn(packageName)
        val packageInfo: PackageInfo = mock()
        packageInfo.versionName = packageName
        val packageManager: PackageManager = mock()
        `when`(WoolworthsApplication.getInstance().packageManager).thenReturn(packageManager)
        `when`(packageManager.getPackageInfo(packageName, 0)).thenReturn(packageInfo)
        WoolworthsApplication.testSetContext(instrumentationContext)
        setFinalStatic(Build::class.java.getField("MANUFACTURER"), "Woolworths")
        setFinalStatic(Build::class.java.getField("MODEL"), "Zensar")
        val telephonyManager: TelephonyManager = mock(
            TelephonyManager::class.java,
            RETURNS_DEEP_STUBS
        )
        `when`(instrumentationContext.getSystemService(Context.TELEPHONY_SERVICE)).thenReturn(
            telephonyManager
        )
        `when`(telephonyManager.getNetworkOperatorName()).thenReturn("Airtel")

        checkoutAddAddressNewUserInteractor = CheckoutAddAddressNewUserInteractor(
            mock(CheckoutAddAddressNewUserApiHelper::class.java, CALLS_REAL_METHODS)
        )
        checkoutAddAddressNewUserViewModel =
            CheckoutAddAddressNewUserViewModel(checkoutAddAddressNewUserInteractor)
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
        verify(checkoutReturningUserCollectionFragment, Mockito.times(1))
            .initializeFoodSubstitution()
        verify(checkoutReturningUserCollectionFragment, Mockito.times(1))
            .initializeDeliveryInstructions()
    }

    @Ignore
    fun checkStorePickupInforResponse_Success() = runBlocking {
        val mockData: LiveData<Any> = mock()
        val body = StorePickupInfoBody()
        body.apply {
            firstName = "test"
            navSuburbId = "123"
            primaryContactNo = "1234567890"
            storeId = "st1005"
            taxiOpted = true
            vehicleColour = "black"
            vehicleModel = "BMW"
        }
        `when`(checkoutAddAddressNewUserInteractor.getStorePickupInfo(body)).thenReturn(mockData)
        doNothing().`when`(checkoutReturningUserCollectionFragment).initShimmerView()
        checkoutAddAddressNewUserViewModel.getStorePickupInfo(body).observeOnce {
            it
        }
    }

    fun <T> LiveData<T>.observeOnce(onChangeHandler: (T) -> Unit) {
        val observer = OneTimeObserver(handler = onChangeHandler)
        observe(observer, observer)
    }
}