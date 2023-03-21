package za.co.woolworths.financial.services.android.enhancedSubstitution.repository

import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import retrofit2.Response
import za.co.woolworths.financial.services.android.enhancedSubstitution.apihelper.SubstitutionApiHelper
import za.co.woolworths.financial.services.android.enhancedSubstitution.model.Data
import za.co.woolworths.financial.services.android.enhancedSubstitution.model.ProductSubstitution
import za.co.woolworths.financial.services.android.enhancedSubstitution.model.SubstitutionInfo
import za.co.woolworths.financial.services.android.models.network.Status

class ProductSubstitutionRepositoryTest {

    @Mock
    private lateinit var productSubstitution: ProductSubstitution

    @Mock
    private lateinit var substitutionApiHelper: SubstitutionApiHelper

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun test_emptylist_getSubstitutions() = runTest {
        Mockito.`when`(substitutionApiHelper.getProductSubstitution(PROD_ID)).thenReturn(Response.success(productSubstitution))
        val sut = ProductSubstitutionRepository(substitutionApiHelper)
        val result = sut.getProductSubstitution(PROD_ID)
        Assert.assertEquals(0, result.data?.data?.size)
    }

    @Test
    fun test_getSubstitutions() = runTest {
        val dataList = mutableListOf<Data>()
        val data = Data(listOf<Any>(), SubstitutionInfo(displayName = "Free Range Jumbo Eggs 6 pk", id = "20068905"), substitutionSelection = "USER_CHOICE")
        dataList.add(data)
        val response = za.co.woolworths.financial.services.android.enhancedSubstitution.model.Response(code = "-1" , desc = "success")

        val productSubstitution = ProductSubstitution(data= dataList, httpCode = 200, response = response)
        Mockito.`when`(substitutionApiHelper.getProductSubstitution(PROD_ID)).thenReturn(Response.success(productSubstitution))
        val sut = ProductSubstitutionRepository(substitutionApiHelper)
        val result = sut.getProductSubstitution(PROD_ID)
        Assert.assertEquals(1, result.data?.data?.size)
        Assert.assertEquals("Free Range Jumbo Eggs 6 pk", result.data?.data?.get(0)?.substitutionInfo?.displayName)
    }

    @Test
    fun test_error_getSubstitutions() = runTest {

        Mockito.`when`(substitutionApiHelper.getProductSubstitution(PROD_ID)).thenReturn(Response.error(504, "".toResponseBody()))
        val sut = ProductSubstitutionRepository(substitutionApiHelper)
        val result = sut.getProductSubstitution(PROD_ID)
        Assert.assertEquals(Status.ERROR, result.status)
    }

    companion object {
        private val PROD_ID = "6009195203504"
    }
}