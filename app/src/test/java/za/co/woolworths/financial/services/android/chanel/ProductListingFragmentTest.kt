package za.co.woolworths.financial.services.android.chanel

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.powermock.modules.junit4.PowerMockRunner
import za.co.woolworths.financial.services.android.models.dto.ProductList
import za.co.woolworths.financial.services.android.models.dto.ProductView
import za.co.woolworths.financial.services.android.ui.fragments.product.grid.ProductListingFragment
import za.co.woolworths.financial.services.android.utils.TestCoroutineRule
import za.co.woolworths.financial.services.android.utils.mock


/**
 * Created by Kunal Uttarwar on 10/02/22.
 */

@ExperimentalCoroutinesApi
@RunWith(PowerMockRunner::class)
class ProductListingFragmentTest : Fragment() {
    @get:Rule
    val testInstantTaskExecutorRule: TestRule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private lateinit var productListingFragment: ProductListingFragment
    private lateinit var instrumentationContext: Context

    @Before
    fun init() {
        productListingFragment =
            Mockito.mock(ProductListingFragment::class.java, Mockito.CALLS_REAL_METHODS)
        instrumentationContext = Mockito.mock(Context::class.java, Mockito.RETURNS_DEEP_STUBS)
    }

    @Test
    fun checkIfChanelMethodGetsCalled() {
        val mockProductView = ProductView()
        mockProductView.isBanners = true
        mockProductView.dynamicBanners = mock()
        mockProductView.pageHeading = "test"

        productListingFragment.onLoadProductSuccess(mockProductView, false)
        Mockito.verify(productListingFragment, Mockito.times(1))
            .setTitle()
        Assert.assertEquals(mockProductView.pageHeading, productListingFragment.toolbarTitleText)
    }

    @Test
    fun openProductDetailsViewTest() {
        val productList: ProductList = mock()
        productListingFragment.openProductDetailsView(productList)
        Mockito.verify(productListingFragment, Mockito.times(1))
            .openProductDetailView(productList)
    }
}