package za.co.woolworths.financial.services.android.enhancedSubstitution.repository

import androidx.lifecycle.MutableLiveData
import androidx.paging.PagingSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import za.co.woolworths.financial.services.android.enhancedSubstitution.service.network.SubstitutionApiHelper
import za.co.woolworths.financial.services.android.enhancedSubstitution.service.repository.SubstitutionPagingSource
import za.co.woolworths.financial.services.android.models.dto.PagingResponse
import za.co.woolworths.financial.services.android.models.dto.ProductList
import za.co.woolworths.financial.services.android.models.dto.ProductView
import za.co.woolworths.financial.services.android.models.dto.ProductsRequestParams

@ExperimentalCoroutinesApi
class SubstitutionPagingSourceTest {

    @Mock
    private lateinit var apiHelper: SubstitutionApiHelper

    @Mock
    private lateinit var _pagingResponse: MutableLiveData<PagingResponse?>

    private lateinit var substitutionPagingSource: SubstitutionPagingSource
    private lateinit var requestParams: ProductsRequestParams

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        requestParams = ProductsRequestParams(
                "apple",
                ProductsRequestParams.SearchType.SEARCH,
                ProductsRequestParams.ResponseType.DETAIL,
                0)
        substitutionPagingSource = SubstitutionPagingSource(apiHelper, requestParams, _pagingResponse)
    }

    @Test
    fun getSearch_loadFailWithError() = runTest {
        val error = RuntimeException("404", Throwable())
        given(apiHelper.getSearchedProducts(requestParams)).willThrow(error)
        val expectedResult = PagingSource.LoadResult.Error<Int, ProductList>(error)
        assertEquals(
                expectedResult,
                substitutionPagingSource.load(
                PagingSource.LoadParams.Refresh(key = 0, loadSize = 1, placeholdersEnabled = false)))
    }


    @Test
    fun getSearch_loadWithCorrectResponse() = runTest {
        val productView = ProductView()
        val productCollection = ArrayList<ProductList>()
        productCollection.add(ProductList(
                isRnREnabled = true,
                averageRating = "5.0",
                reviewCount = "1",
                productId = "20016371",
                productName = "Golden Delicious Apples 4 pk",
                externalImageRefV2 = "https://assets.woolworthsstatic.co.za/Golden-Delicious-Apples-4-pk-20016371.jpg?V=6QG$&o=eyJidWNrZXQiOiJ3dy1vbmxpbmUtaW1hZ2UtcmVzaXplIiwia2V5IjoiaW1hZ2VzL2VsYXN0aWNlcmEvcHJvZHVjdHMvaGVyby8yMDE5LTA2LTE5LzIwMDE2MzcxX2hlcm8uanBnIn0&",
                imagePath = "https://www-win-qa.woolworths.co.za/images/elasticera/products/hero/2019-06-19/20016371_hero.jpg",
                isLiquor = false,
                price = 21.99F,
                productType = "foodProducts",
                sku = "20016371",
        ))
        productView.products = productCollection
        given(apiHelper.getSearchedProducts(requestParams)).willReturn(productView)
        val expectedResult = PagingSource.LoadResult.Page(
                data = productView.products,
                prevKey = null,
                nextKey = null
        )
        assertEquals(
                expectedResult, substitutionPagingSource.load(
                PagingSource.LoadParams.Refresh(
                        key = 0,
                        loadSize = 1,
                        placeholdersEnabled = false
                )
        )
        )
    }
}