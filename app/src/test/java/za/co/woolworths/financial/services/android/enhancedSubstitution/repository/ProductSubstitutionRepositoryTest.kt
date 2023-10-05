package za.co.woolworths.financial.services.android.enhancedSubstitution.repository

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.powermock.api.mockito.PowerMockito
import retrofit2.Response
import za.co.woolworths.financial.services.android.enhancedSubstitution.EnhanceSubstitutionHelperTest
import za.co.woolworths.financial.services.android.enhancedSubstitution.EnhanceSubstitutionHelperTest.Companion.COMMARCE_ITEM_ID
import za.co.woolworths.financial.services.android.enhancedSubstitution.EnhanceSubstitutionHelperTest.Companion.SKU_ID
import za.co.woolworths.financial.services.android.enhancedSubstitution.EnhanceSubstitutionHelperTest.Companion.STORE_ID
import za.co.woolworths.financial.services.android.enhancedSubstitution.EnhanceSubstitutionHelperTest.Companion.SUBSTITUTION_ID
import za.co.woolworths.financial.services.android.enhancedSubstitution.apihelper.SubstitutionApiHelperTest
import za.co.woolworths.financial.services.android.enhancedSubstitution.service.model.*
import za.co.woolworths.financial.services.android.enhancedSubstitution.service.network.SubstitutionApiHelper
import za.co.woolworths.financial.services.android.enhancedSubstitution.service.repository.ProductSubstitutionRepository
import za.co.woolworths.financial.services.android.models.dto.FormException
import za.co.woolworths.financial.services.android.models.dto.SkuInventory
import za.co.woolworths.financial.services.android.models.dto.SkusInventoryForStoreResponse
import za.co.woolworths.financial.services.android.models.network.Status
import za.co.woolworths.financial.services.android.util.KotlinUtils.Companion.PROD_ID
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager

@ExperimentalCoroutinesApi
class ProductSubstitutionRepositoryTest {

    @Mock
    private lateinit var productSubstitution: ProductSubstitution

    @Mock
    private lateinit var skusInventoryForStoreResponse: SkusInventoryForStoreResponse

    @Mock
    private lateinit var addSubstitutionResponse: AddSubstitutionResponse

    @Mock
    private lateinit var substitutionApiHelper: SubstitutionApiHelper

    private lateinit var mockFirebaseManager: MockedStatic<FirebaseManager>
    private lateinit var mockFirebaseApp: MockedStatic<FirebaseApp>
    private lateinit var mockFirebaseCrashlytics: MockedStatic<FirebaseCrashlytics>
    private lateinit var mockContext: Context

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        mockFirebaseManager = mockStatic(FirebaseManager::class.java)
        mockFirebaseApp = mockStatic(FirebaseApp::class.java)
        mockFirebaseCrashlytics = mockStatic(FirebaseCrashlytics::class.java)
        mockContext = mock(Context::class.java, RETURNS_DEEP_STUBS)

        `when`(FirebaseApp.initializeApp(any(Context::class.java)))
            .thenReturn(mock(FirebaseApp::class.java))
        `when`(FirebaseCrashlytics.getInstance())
            .thenReturn(mock(FirebaseCrashlytics::class.java))

    }

    @After
    fun tearDown() {
        mockFirebaseManager.close()
        mockFirebaseApp.close()
        mockFirebaseCrashlytics.close()
    }

    @Test
    fun getSubstitutions_successWithCorrectResponse() = runTest {
        val dataList = mutableListOf<Data>()
        val data = Data(
            listOf(),
            SubstitutionInfo(displayName = "Free Range Jumbo Eggs 6 pk", id = "20068905"),
            substitutionSelection = "USER_CHOICE"
        )
        dataList.add(data)
        val response =
           Response(
                code = "-1",
                desc = "success"
            )
        val productSubstitution =
            ProductSubstitution(data = dataList, httpCode = 200, response = response)
        `when`(substitutionApiHelper.getProductSubstitution(PROD_ID))
            .thenReturn(Response.success(productSubstitution))
        val productSubstitutionRepository = ProductSubstitutionRepository(substitutionApiHelper)
        val result = productSubstitutionRepository.getProductSubstitution(PROD_ID)
        Assert.assertEquals(1, result.data?.data?.size)
        Assert.assertEquals(
            "Free Range Jumbo Eggs 6 pk",
            result.data?.data?.get(0)?.substitutionInfo?.displayName
        )
    }

    @Test
    fun getSubstitutions_returnWithEmptyList() = runTest {
        `when`(substitutionApiHelper.getProductSubstitution(PROD_ID))
            .thenReturn(Response.success(productSubstitution))
        val sut = ProductSubstitutionRepository(substitutionApiHelper)
        val result = sut.getProductSubstitution(PROD_ID)
        Assert.assertEquals(0, result.data?.data?.size)
    }

    @Test
    fun getSubstitutions_withError() = runTest {
        `when`(substitutionApiHelper.getProductSubstitution(PROD_ID))
            .thenReturn(Response.error(504, "".toResponseBody()))
        val productSubstitutionRepository = ProductSubstitutionRepository(substitutionApiHelper)
        val result = productSubstitutionRepository.getProductSubstitution(PROD_ID)
        Assert.assertEquals(Status.ERROR, result.status)
    }

    @Test
    fun getSubstituion_WithException() =
        runBlocking {
            val errorMessage = "Error Message from server"
            PowerMockito.`when`(substitutionApiHelper.getProductSubstitution(PROD_ID)).thenThrow(
                RuntimeException(errorMessage)
            )
            val productSubstitutionRepository = ProductSubstitutionRepository(substitutionApiHelper)
            val result = productSubstitutionRepository.getProductSubstitution(PROD_ID)
            Assert.assertEquals(result.status, Status.ERROR)
        }

    @Test
    fun getSubstituion_WithJSONSyntexException() =
        runBlocking {
            PowerMockito.`when`(substitutionApiHelper.getProductSubstitution(PROD_ID)).thenThrow(
                JsonSyntaxException(WRONG_JSON_RESPONSE)
            )
            val productSubstitutionRepository = ProductSubstitutionRepository(substitutionApiHelper)
            val result = productSubstitutionRepository.getProductSubstitution(PROD_ID)
            Assert.assertEquals(result.status, Status.ERROR)
            Assert.assertNotEquals(WRONG_JSON_RESPONSE, CORRECT_JSON_RESPONSE)
        }

    @Test
    fun fetchInventory_returnCorrrectResponse() = runTest {

        val skusInventoryForStoreResponse = SkusInventoryForStoreResponse()
        skusInventoryForStoreResponse.storeId = STORE_ID
        val skuInventoryList = mutableListOf<SkuInventory?>()
        val skuInventory = SkuInventory()
        skuInventory.sku = SKU_ID
        skuInventory.quantity = 15
        skuInventoryList.add(0, skuInventory)
        skusInventoryForStoreResponse.skuInventory = skuInventoryList
        `when`(substitutionApiHelper.fetchInventoryForKiboProducts(STORE_ID, SKU_ID))
            .thenReturn(Response.success(skusInventoryForStoreResponse))
        val productSubstitutionRepository = ProductSubstitutionRepository(substitutionApiHelper)
        val result = productSubstitutionRepository.fetchInventoryForKiboProducts(STORE_ID, SKU_ID)
        Assert.assertNotNull(skusInventoryForStoreResponse)
        Assert.assertEquals(1, result.data?.skuInventory?.size)
        Assert.assertEquals(STORE_ID, result.data?.storeId)
        Assert.assertEquals(15, result.data?.skuInventory?.get(0)?.quantity)
    }

    @Test
    fun fetchInventory_returnEmptyResponse() = runTest {
        `when`(substitutionApiHelper.fetchInventoryForKiboProducts(STORE_ID, SKU_ID))
            .thenReturn(Response.success(skusInventoryForStoreResponse))
        val sut = ProductSubstitutionRepository(substitutionApiHelper)
        val result = sut.fetchInventoryForKiboProducts(STORE_ID, SKU_ID)

        Assert.assertEquals(null, result.data?.skuInventory)
    }

    @Test
    fun fetchInventory_withError() = runTest {
        `when`(substitutionApiHelper.fetchInventoryForKiboProducts(STORE_ID, SKU_ID))
            .thenReturn(Response.error(504, "".toResponseBody()))
        val productSubstitutionRepository = ProductSubstitutionRepository(substitutionApiHelper)
        val result = productSubstitutionRepository.fetchInventoryForKiboProducts(STORE_ID, SKU_ID)
        Assert.assertEquals(Status.ERROR, result.status)
    }

    @Test
    fun addSubstitute_returnCorrrectResponse() = runTest {

        val addSubstitutionRequest = AddSubstitutionRequest(
            SubstitutionApiHelperTest.USER_CHOICE, SUBSTITUTION_ID, COMMARCE_ITEM_ID
        )
        val dataList = mutableListOf<DataX>()
        val substitutionList = mutableListOf<Any>()
        val formExceptions = mutableListOf<FormException>()
        dataList.add(DataX(substitutionList, formExceptions))
        val response =
           Response(
                "-1",
                "success"
            )
        val addSubstitutionResponse = AddSubstitutionResponse(dataList, 200, response)
        `when`(substitutionApiHelper.addSubstitution(addSubstitutionRequest))
            .thenReturn(Response.success(addSubstitutionResponse))
        val productSubstitutionRepository = ProductSubstitutionRepository(substitutionApiHelper)
        val result = productSubstitutionRepository.addSubstitution(addSubstitutionRequest)
        Assert.assertNotNull(addSubstitutionResponse)
        Assert.assertEquals(1, result.data?.data?.size)
    }

    @Test
    fun addSubstitutions_returnEmptyResponse() = runTest {
        val addSubstitutionRequest = AddSubstitutionRequest(
            SubstitutionApiHelperTest.USER_CHOICE,
            SUBSTITUTION_ID,
            COMMARCE_ITEM_ID
        )

        `when`(substitutionApiHelper.addSubstitution(addSubstitutionRequest))
            .thenReturn(Response.success(addSubstitutionResponse))
        val sut = ProductSubstitutionRepository(substitutionApiHelper)
        val result = sut.addSubstitution(addSubstitutionRequest)
        Assert.assertEquals(0, result.data?.data?.size)
    }

    @Test
    fun addSubstitutions_withError() = runTest {
        val addSubstitutionRequest = AddSubstitutionRequest(
            SubstitutionApiHelperTest.USER_CHOICE,
            SUBSTITUTION_ID,
            COMMARCE_ITEM_ID
        )
        `when`(substitutionApiHelper.addSubstitution(addSubstitutionRequest))
            .thenReturn(Response.error(504, "".toResponseBody()))
        val productSubstitutionRepository = ProductSubstitutionRepository(substitutionApiHelper)
        val result = productSubstitutionRepository.addSubstitution(addSubstitutionRequest)
        Assert.assertEquals(Status.ERROR, result.status)
    }

    @Test
    fun kiboProducts_returnEmptyResponse() = runTest {
        val getKiboProductRequest = EnhanceSubstitutionHelperTest.prepareKiboProductRequest()

        `when`(substitutionApiHelper.fetchKiboProducts(getKiboProductRequest))
            .thenReturn(Response.success(EnhanceSubstitutionHelperTest.prepareKiboResponseWithEmptyList()))
        val sut = ProductSubstitutionRepository(substitutionApiHelper)
        val result = sut.fetchKiboProducts(getKiboProductRequest)
        Assert.assertEquals(0, result.data?.data?.responses?.getOrNull(0)?.actions?.getOrNull(0)?.items?.size)
    }

    @Test
    fun  kiboProducts_withError() = runTest {
        val kiboProductRequest = EnhanceSubstitutionHelperTest.prepareKiboProductRequest()
        `when`(substitutionApiHelper.fetchKiboProducts(kiboProductRequest))
            .thenReturn(Response.error(504, "".toResponseBody()))
        val productSubstitutionRepository = ProductSubstitutionRepository(substitutionApiHelper)
        val result = productSubstitutionRepository.fetchKiboProducts(kiboProductRequest)
        Assert.assertEquals(Status.ERROR, result.status)
    }


    @Test
    fun getKiboRequest_returnCorrrectResponse() = runTest {

        val getKiboProductRequest = EnhanceSubstitutionHelperTest.prepareKiboProductRequest()
        val kiboProductResponse = prepareKiboResponse()
        `when`(substitutionApiHelper.fetchKiboProducts(getKiboProductRequest))
            .thenReturn(Response.success(kiboProductResponse))
        val productSubstitutionRepository = ProductSubstitutionRepository(substitutionApiHelper)
        val result = productSubstitutionRepository.fetchKiboProducts(getKiboProductRequest)
        Assert.assertNotNull(kiboProductResponse)
        Assert.assertEquals(1, result.data?.data?.responses?.size)
        Assert.assertEquals("White Thick Slice Bread 700 g", result.data?.data?.responses?.getOrNull(0)?.actions?.getOrNull(0)?.items?.getOrNull(0)?.title)
    }

    fun prepareKiboResponse(): KiboProductResponse {
        val item = Item("", "1", title = "White Thick Slice Bread 700 g" , imageLink = "", price = 32.6f, defaultPrice =32.6, ratings = 5.0 )
        val items = ArrayList<Item>()
        items.add(item)
        val action = Action(items)
        val actionList = ArrayList<Action>()
        actionList.add(action)
        val actionResponse = ActionResponse( actionList, "1234")
        val actionResponseList = ArrayList<ActionResponse>()
        actionResponseList.add(actionResponse)
        val itemResponse = ItemResponse(actionResponseList)
        val response =
            Response(
                "-1",
                "success"
            )
        return KiboProductResponse(itemResponse, response, 200)
    }

    companion object {
        const val WRONG_JSON_RESPONSE = "{\n" +
                "    \"response\": {\n" +
                "        \"code\": \"-1\",\n" +
                "        \"desc\": \"Success\"\n" +
                "    },\n" +
                "    \"data\": \n" +
                "        {\n" +
                "            \"substitutionInfo\": {},\n" +
                "            \"links\": [],\n" +
                "            \"substitutionSelection\": \"\"\n" +
                "        }\n" +
                "    ,\n" +
                "    \"httpCode\": 200\n" +
                "}"

        const val CORRECT_JSON_RESPONSE = "{\n" +
                "    \"response\": {\n" +
                "        \"code\": \"-1\",\n" +
                "        \"desc\": \"Success\"\n" +
                "    },\n" +
                "    \"data\": [\n" +
                "        {\n" +
                "            \"substitutionInfo\": {},\n" +
                "            \"links\": [],\n" +
                "            \"substitutionSelection\": \"\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"httpCode\": 200\n" +
                "}"
    }
}
