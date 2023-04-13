package za.co.woolworths.financial.services.android.enhancedSubstitution.repository

import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import retrofit2.Response
import za.co.woolworths.financial.services.android.enhancedSubstitution.EnhanceSubstitutonHelperTest.Companion.COMMARCE_ITEM_ID
import za.co.woolworths.financial.services.android.enhancedSubstitution.EnhanceSubstitutonHelperTest.Companion.SKU_ID
import za.co.woolworths.financial.services.android.enhancedSubstitution.EnhanceSubstitutonHelperTest.Companion.STORE_ID
import za.co.woolworths.financial.services.android.enhancedSubstitution.EnhanceSubstitutonHelperTest.Companion.SUBSTITUTION_ID
import za.co.woolworths.financial.services.android.enhancedSubstitution.apihelper.SubstitutionApiHelper
import za.co.woolworths.financial.services.android.enhancedSubstitution.apihelper.SubstitutionApiHelperTest
import za.co.woolworths.financial.services.android.enhancedSubstitution.model.*
import za.co.woolworths.financial.services.android.models.dto.FormException
import za.co.woolworths.financial.services.android.models.dto.SkuInventory
import za.co.woolworths.financial.services.android.models.dto.SkusInventoryForStoreResponse
import za.co.woolworths.financial.services.android.models.network.Status
import za.co.woolworths.financial.services.android.util.KotlinUtils.Companion.PROD_ID

class ProductSubstitutionRepositoryTest {

    @Mock
    private lateinit var productSubstitution: ProductSubstitution

    @Mock
    private lateinit var skusInventoryForStoreResponse: SkusInventoryForStoreResponse

    @Mock
    private lateinit var addSubstitutionResponse: AddSubstitutionResponse

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
        Mockito.`when`(substitutionApiHelper.fetchInventoryForSubstitution(STORE_ID, SKU_ID)).thenReturn(Response.success(skusInventoryForStoreResponse))
        val sut = ProductSubstitutionRepository(substitutionApiHelper)
        val result = sut.getInventoryForSubstitution(STORE_ID, SKU_ID)

        Assert.assertEquals(null, result.data?.skuInventory)
    }

    @Test
    fun test_fetchInventory() = runTest {

        val skusInventoryForStoreResponse = SkusInventoryForStoreResponse()
        skusInventoryForStoreResponse.storeId = STORE_ID
        val skuInventoryList = mutableListOf<SkuInventory?>()
        val skuInventory = SkuInventory()
        skuInventory.sku = SKU_ID
        skuInventory.quantity = 15
        skuInventoryList.add(0, skuInventory)
        skusInventoryForStoreResponse.skuInventory = skuInventoryList
        Mockito.`when`(substitutionApiHelper.fetchInventoryForSubstitution(STORE_ID, SKU_ID)).thenReturn(Response.success(skusInventoryForStoreResponse))
        val productSubstitutionRepository = ProductSubstitutionRepository(substitutionApiHelper)
        val result = productSubstitutionRepository.getInventoryForSubstitution(STORE_ID, SKU_ID)
        Assert.assertNotNull(skusInventoryForStoreResponse)
        Assert.assertEquals(1, result.data?.skuInventory?.size)
        Assert.assertEquals(STORE_ID, result.data?.storeId)
        Assert.assertEquals(15, result.data?.skuInventory?.get(0)?.quantity)
    }

    @Test
    fun test_error_fetchInventory() = runTest {
        Mockito.`when`(substitutionApiHelper.fetchInventoryForSubstitution(STORE_ID, SKU_ID)).thenReturn(Response.error(504, "".toResponseBody()))
        val productSubstitutionRepository = ProductSubstitutionRepository(substitutionApiHelper)
        val result = productSubstitutionRepository.getInventoryForSubstitution(STORE_ID, SKU_ID)
        Assert.assertEquals(Status.ERROR, result.status)
    }

    @Test
    fun test_emptyResponse_addSubstitutions() = runTest {
        val addSubstitutionRequest = AddSubstitutionRequest(SubstitutionApiHelperTest.USER_CHOICE, SUBSTITUTION_ID, COMMARCE_ITEM_ID)

        Mockito.`when`(substitutionApiHelper.addSubstitution(addSubstitutionRequest)).thenReturn(Response.success(addSubstitutionResponse))
        val sut = ProductSubstitutionRepository(substitutionApiHelper)
        val result = sut.addSubstitution(addSubstitutionRequest)
        Assert.assertEquals(0, result.data?.data?.size)
    }

    @Test
    fun test_addSubstitute() = runTest {

        val addSubstitutionRequest = AddSubstitutionRequest(
                SubstitutionApiHelperTest.USER_CHOICE, SUBSTITUTION_ID, COMMARCE_ITEM_ID)
        val dataList = mutableListOf<DataX>()
        val substitutionList = mutableListOf<Any>()
        val formExcepions = mutableListOf<FormException>()
        dataList.add(DataX(substitutionList, formExcepions))
        val response = Response("-1","success" )
        val addSubstitutionResponse = AddSubstitutionResponse(dataList, 200, response)
        Mockito.`when`(substitutionApiHelper.addSubstitution(addSubstitutionRequest)).thenReturn(Response.success(addSubstitutionResponse))
        val productSubstitutionRepository = ProductSubstitutionRepository(substitutionApiHelper)
        val result = productSubstitutionRepository.addSubstitution(addSubstitutionRequest)
        Assert.assertNotNull(addSubstitutionResponse)
        Assert.assertEquals(1 , result.data?.data?.size)
    }

    @Test
    fun test_error_addSubstitutions() = runTest {
        val addSubstitutionRequest = AddSubstitutionRequest(SubstitutionApiHelperTest.USER_CHOICE, SUBSTITUTION_ID, COMMARCE_ITEM_ID)

        Mockito.`when`(substitutionApiHelper.addSubstitution(addSubstitutionRequest)).thenReturn(Response.error(504, "".toResponseBody()))
        val productSubstitutionRepository = ProductSubstitutionRepository(substitutionApiHelper)
        val result = productSubstitutionRepository.addSubstitution(addSubstitutionRequest)
        Assert.assertEquals(Status.ERROR, result.status)
    }
}