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
import za.co.woolworths.financial.services.android.models.dto.SkuInventory
import za.co.woolworths.financial.services.android.models.dto.SkusInventoryForStoreResponse
import za.co.woolworths.financial.services.android.models.network.Status

class ProductSubstitutionRepositoryTest {

    @Mock
    private lateinit var productSubstitution: ProductSubstitution

    @Mock
    private lateinit var skusInventoryForStoreResponse: SkusInventoryForStoreResponse

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
        val productSubstitutionRepository = ProductSubstitutionRepository(substitutionApiHelper)
        val result = productSubstitutionRepository.getProductSubstitution(PROD_ID)
        Assert.assertEquals(1, result.data?.data?.size)
        Assert.assertEquals("Free Range Jumbo Eggs 6 pk", result.data?.data?.get(0)?.substitutionInfo?.displayName)
    }

    @Test
    fun test_error_getSubstitutions() = runTest {
        Mockito.`when`(substitutionApiHelper.getProductSubstitution(PROD_ID)).thenReturn(Response.error(504, "".toResponseBody()))
        val productSubstitutionRepository = ProductSubstitutionRepository(substitutionApiHelper)
        val result = productSubstitutionRepository.getProductSubstitution(PROD_ID)
        Assert.assertEquals(Status.ERROR, result.status)
    }

    @Test
    fun test_emptyResponse_fetchInventory() = runTest {
        Mockito.`when`(substitutionApiHelper.fetchInventoryForSubstitution("473", SKU_ID)).thenReturn(Response.success(skusInventoryForStoreResponse))
        val sut = ProductSubstitutionRepository(substitutionApiHelper)
        val result = sut.getInventoryForSubstitution("473", SKU_ID)
        Assert.assertEquals(null, result.data?.skuInventory)
    }

    @Test
    fun test_fetchInventory() = runTest {

        val skusInventoryForStoreResponse = SkusInventoryForStoreResponse()
        skusInventoryForStoreResponse.storeId = "473"
        val skuInventoryList = mutableListOf<SkuInventory?>()
        val skuInventory = SkuInventory()
        skuInventory.sku = "6001009025692"
        skuInventory.quantity = 15
        skuInventoryList.add(0, skuInventory)
        skusInventoryForStoreResponse.skuInventory = skuInventoryList
        Mockito.`when`(substitutionApiHelper.fetchInventoryForSubstitution(STORE_ID, SKU_ID)).thenReturn(Response.success(skusInventoryForStoreResponse))
        val productSubstitutionRepository = ProductSubstitutionRepository(substitutionApiHelper)
        val result = productSubstitutionRepository.getInventoryForSubstitution(STORE_ID, SKU_ID)
        Assert.assertNotNull(skusInventoryForStoreResponse)
        Assert.assertEquals(1, result.data?.skuInventory?.size)
        Assert.assertEquals("473", result.data?.storeId)
        Assert.assertEquals(15, result.data?.skuInventory?.get(0)?.quantity)
    }

    @Test
    fun test_error_fetchInventory() = runTest {
        Mockito.`when`(substitutionApiHelper.fetchInventoryForSubstitution("473", SKU_ID)).thenReturn(Response.error(504, "".toResponseBody()))
        val productSubstitutionRepository = ProductSubstitutionRepository(substitutionApiHelper)
        val result = productSubstitutionRepository.getInventoryForSubstitution("473", SKU_ID)
        Assert.assertEquals(Status.ERROR, result.status)
    }

    companion object {
        private  val PROD_ID:String? = "6009195203504"
        private  val SKU_ID:String = "6001009025692"
        private  val STORE_ID:String = "473"
    }
}