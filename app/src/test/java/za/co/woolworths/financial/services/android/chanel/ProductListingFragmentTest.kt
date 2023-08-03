package za.co.woolworths.financial.services.android.chanel

import androidx.fragment.app.Fragment
import org.junit.Assert
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.CALLS_REAL_METHODS
import org.mockito.junit.MockitoJUnitRunner
import org.powermock.api.mockito.PowerMockito
import za.co.woolworths.financial.services.android.models.dto.ProductList
import za.co.woolworths.financial.services.android.models.dto.ProductView
import za.co.woolworths.financial.services.android.models.dto.brandlandingpage.Navigation
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.fragments.product.grid.ProductListingFragment
import za.co.woolworths.financial.services.android.utils.mock


/**
 * Created by Kunal Uttarwar on 10/02/22.
 */

@RunWith(MockitoJUnitRunner::class)
class ProductListingFragmentTest : Fragment() {
    private lateinit var productListingFragment: ProductListingFragment

    @Before
    fun init() {
        productListingFragment =
            Mockito.mock(ProductListingFragment::class.java, CALLS_REAL_METHODS)
    }

    // TODO UNIT TEST: The following code is not aligned with recent implementation, and needs to be updated.
    @Ignore
    @Test
    fun checkIfChanelMethodGetsCalled() {
        val mockProductView = ProductView()
        mockProductView.isBanners = true
        mockProductView.dynamicBanners = mock()
        mockProductView.pageHeading = "test"

        productListingFragment.onLoadProductSuccess(mockProductView, false)
        Mockito.verify(productListingFragment, Mockito.times(1))
            .updateToolbarTitle()
        Assert.assertEquals(mockProductView.pageHeading, productListingFragment.toolbarTitleText)
    }

    // TODO UNIT TEST: The following code is not aligned with recent implementation, and needs to be updated.
    @Ignore
    @Test
    fun openProductDetailsViewTest() {
        val productList: ProductList = mock()
        productListingFragment.openProductDetailsView(productList, "", "")
        Mockito.verify(productListingFragment, Mockito.times(1))
            .openProductDetailView(productList,"","")
    }

    @Ignore
    fun clickCategoryListViewCellTest() {
        val mockActivity = Mockito.mock(BottomNavigationActivity::class.java, CALLS_REAL_METHODS)
        mockActivity.mNavController = mock()
        Mockito.`when`(productListingFragment.activity)
            .thenReturn(mockActivity)
        val mockNavState: Navigation =
            Mockito.mock(Navigation::class.java, CALLS_REAL_METHODS)
        mockNavState.navigationState = "state"

        val mockFragment: ProductListingFragment = mock()
        PowerMockito.whenNew(ProductListingFragment::class.java).withAnyArguments()
            .thenReturn(mockFragment)
        Mockito.doNothing().`when`(mockActivity.pushFragment(mockFragment))
        productListingFragment.clickCategoryListViewCell(mockNavState, "", "", true)
        Mockito.verify(mockActivity, Mockito.times(1))
            .pushFragment(mockFragment)
    }
}