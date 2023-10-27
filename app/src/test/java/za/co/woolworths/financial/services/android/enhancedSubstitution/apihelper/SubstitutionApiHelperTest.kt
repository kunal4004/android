package za.co.woolworths.financial.services.android.enhancedSubstitution.apihelper

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import za.co.woolworths.financial.services.android.enhancedSubstitution.EnhanceSubstitutionHelperTest.Companion.COMMARCE_ITEM_ID
import za.co.woolworths.financial.services.android.enhancedSubstitution.EnhanceSubstitutionHelperTest.Companion.DEVICE_TOKEN
import za.co.woolworths.financial.services.android.enhancedSubstitution.EnhanceSubstitutionHelperTest.Companion.PRODUCT_ID
import za.co.woolworths.financial.services.android.enhancedSubstitution.EnhanceSubstitutionHelperTest.Companion.SESSION_TOKEN
import za.co.woolworths.financial.services.android.enhancedSubstitution.EnhanceSubstitutionHelperTest.Companion.SKU_ID
import za.co.woolworths.financial.services.android.enhancedSubstitution.EnhanceSubstitutionHelperTest.Companion.STORE_ID
import za.co.woolworths.financial.services.android.enhancedSubstitution.EnhanceSubstitutionHelperTest.Companion.SEARCH_TYPE
import za.co.woolworths.financial.services.android.enhancedSubstitution.EnhanceSubstitutionHelperTest.Companion.RESPONSE_TYPE
import za.co.woolworths.financial.services.android.enhancedSubstitution.EnhanceSubstitutionHelperTest.Companion.DELIVERY_TYPE
import za.co.woolworths.financial.services.android.enhancedSubstitution.EnhanceSubstitutionHelperTest.Companion.SUBSTITUTION_ID

import za.co.woolworths.financial.services.android.enhancedSubstitution.service.model.AddSubstitutionRequest
import za.co.woolworths.financial.services.android.enhancedSubstitution.service.model.KiboProductRequest
import za.co.woolworths.financial.services.android.enhancedSubstitution.service.model.Product
import za.co.woolworths.financial.services.android.models.network.ApiInterface
import za.co.woolworths.financial.services.android.util.Utils

@ExperimentalCoroutinesApi
class SubstitutionApiHelperTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiHelper: ApiInterface

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        apiHelper = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiInterface::class.java)
    }

    @Test
    fun getSubstitutionsRequest_returnNullOrEmptyResponse_() = runTest {
        val mockResponse = MockResponse()
        mockResponse.setBody("{}")
        mockWebServer.enqueue(mockResponse)
        val response = apiHelper.getSubstitution(SESSION_TOKEN, DEVICE_TOKEN, PRODUCT_ID)
        mockWebServer.takeRequest()
        Assert.assertEquals(null , response.body()?.data)
    }

    @Test
    fun getSubstitutionsRequest_returnCorrectSubstitutionResponse() = runTest {
        val mockResponse = MockResponse()
        mockResponse.setResponseCode(200)
        mockResponse.setBody(RESPONSE)
        mockWebServer.enqueue(mockResponse)
        val response = apiHelper.getSubstitution(SESSION_TOKEN, DEVICE_TOKEN, PRODUCT_ID)
        mockWebServer.takeRequest()
        Assert.assertEquals(1, response.body()?.data?.size)
        Assert.assertEquals(USER_CHOICE, response.body()?.data?.getOrNull(0)?.substitutionSelection)
    }

    @Test
    fun getSubstitutionsRequest_returnWrongResponse()= runTest {
        val mockResponse = MockResponse()
        mockResponse.setResponseCode(200)
        mockResponse.setBody(WRONG_RESPONSE)
        mockWebServer.enqueue(mockResponse)
        val response = apiHelper.getSubstitution(SESSION_TOKEN, DEVICE_TOKEN, PRODUCT_ID)
        mockWebServer.takeRequest()
        Assert.assertEquals(0, response.body()?.data?.size)
    }


    @Test
    fun getInventorRequest_returnCorrectInventoryResponse() = runTest {
        val mockResponse = MockResponse()
        mockResponse.setResponseCode(200)
        mockResponse.setBody(INVENTORY_RESPONSE)
        mockWebServer.enqueue(mockResponse)
        val response =
            apiHelper.fetchDashInventorySKUForStore(SESSION_TOKEN, DEVICE_TOKEN, STORE_ID, SKU_ID)
        mockWebServer.takeRequest()
        Assert.assertEquals(1, response.body()?.skuInventory?.size)
        Assert.assertNotNull(response.body())
    }

    @Test
    fun getInventoryRequest_returnNullOrEmptyResponse() = runTest {
        val mockResponse = MockResponse()
        mockResponse.setBody("{}")
        mockWebServer.enqueue(mockResponse)
        val response =
            apiHelper.fetchDashInventorySKUForStore(SESSION_TOKEN, DEVICE_TOKEN, STORE_ID, SKU_ID)
        mockWebServer.takeRequest()
        Assert.assertNotNull(response.body())
        Assert.assertNotEquals("473", response.body()?.storeId)
    }

    @Test
    fun getInventoryRequest_returnWrongResponse() = runTest {
        val mockResponse = MockResponse()
        mockResponse.setResponseCode(200)
        mockResponse.setBody(WRONG_INVENTORY_RESPONSE)
        mockWebServer.enqueue(mockResponse)
        val response =
            apiHelper.fetchDashInventorySKUForStore(SESSION_TOKEN, DEVICE_TOKEN, STORE_ID, SKU_ID)
        mockWebServer.takeRequest()
        Assert.assertEquals(0, response.body()?.skuInventory?.size)
    }

    @Test
    fun addSubstitutionRequest_returnEmptyResponse() = runTest {
        val mockResponse = MockResponse()
        mockResponse.setBody("{}")
        mockWebServer.enqueue(mockResponse)
        val addSubstitutionRequest =
            AddSubstitutionRequest(USER_CHOICE, SUBSTITUTION_ID, COMMARCE_ITEM_ID)
        val response =
            apiHelper.addSubstitution(SESSION_TOKEN, DEVICE_TOKEN, addSubstitutionRequest)
        mockWebServer.takeRequest()
        Assert.assertNotNull(response.body())
        Assert.assertEquals(true, response.body()?.data.isNullOrEmpty())
    }

    @Test
    fun addSubstitutionRequest_returnWrongResponse() = runTest {
        val mockResponse = MockResponse()
        mockResponse.setResponseCode(200)
        mockResponse.setBody(WRONG_ADD_SUBSTITUTION_RESPONSE)
        val addSubstitutionRequest =
            AddSubstitutionRequest(USER_CHOICE, SUBSTITUTION_ID, COMMARCE_ITEM_ID)
        mockWebServer.enqueue(mockResponse)
        val response =
            apiHelper.addSubstitution(SESSION_TOKEN, DEVICE_TOKEN, addSubstitutionRequest)
        mockWebServer.takeRequest()
        Assert.assertEquals(0, response.body()?.data?.size)
    }


    @Test
    fun getSearchApiRequest_returnNullOrEmptyResponse() = runTest {
        val mockResponse = MockResponse()
        mockResponse.setBody("{}")
        mockWebServer.enqueue(mockResponse)
        val response = apiHelper.getSearchedProducts(
            userAgent = "",
            "",
            "",
            "",
            SESSION_TOKEN,
            DEVICE_TOKEN,
            "",
            SEARCH_TYPE,
            RESPONSE_TYPE,
            0,
            Utils.PAGE_SIZE,
            "",
            "",
            "",
            STORE_ID,
            false,
            DELIVERY_TYPE,
            "105-plist3620006-false-true"
        )
        mockWebServer.takeRequest()
        Assert.assertEquals(null, response.pagingResponse)
        Assert.assertEquals(null, response.products)
    }

    @Test
    fun getSearchAndSortFilterApi_withWrongRequest_returnWrongResponse() = runTest {
        /*search term is empty*/
        val mockResponse = MockResponse()
        mockResponse.setResponseCode(200)
        mockResponse.setBody(WRONG_SEARCH_API_Response)
        mockWebServer.enqueue(mockResponse)
        val response = apiHelper.getSearchedProducts(
            userAgent = "",
            "",
            "",
            "",
            SESSION_TOKEN,
            DEVICE_TOKEN,
            "",
            SEARCH_TYPE,
            RESPONSE_TYPE,
            0,
            Utils.PAGE_SIZE,
            "",
            "",
            "",
            STORE_ID,
            false,
            DELIVERY_TYPE,
            ""
        )
        mockWebServer.takeRequest()
        Assert.assertEquals(0, response.products?.size)
    }

    @Test
    fun getSearchApi_returnProductViewResponse() = runTest {
        val mockResponse = MockResponse()
        mockResponse.setResponseCode(200)
        mockResponse.setBody(SEARCH_RESPONSE)
        mockWebServer.enqueue(mockResponse)
        val response = apiHelper.getSearchedProducts(
            userAgent = "",
            "",
            "",
            "",
            SESSION_TOKEN,
            DEVICE_TOKEN,
            "",
            SEARCH_TYPE,
            RESPONSE_TYPE,
            0,
            Utils.PAGE_SIZE,
            "",
            "",
            "",
            STORE_ID,
            false,
            DELIVERY_TYPE,
            ""
        )
        mockWebServer.takeRequest()
        Assert.assertEquals(1, response.products?.size)
    }

    @Test
    fun getkiboApi_returnProductViewResponse() = runTest {
        val mockResponse = MockResponse()
        mockResponse.setResponseCode(200)
        mockResponse.setBody(KIBO_RESPONSE)
        mockWebServer.enqueue(mockResponse)
        val product = Product("20018702","20018702")
        val list = ArrayList<Product>()
        list.add(product)
        val getKiboRequest = KiboProductRequest("plist3620006", list)
        val response = apiHelper.getKiboProductsFromResponse(
            SESSION_TOKEN,
            DEVICE_TOKEN,
            getKiboRequest
        )
        mockWebServer.takeRequest()
        Assert.assertEquals(false, response.body()?.data?.responses?.isEmpty())
        Assert.assertEquals(1, response.body()?.data?.responses?.getOrNull(0)?.actions?.getOrNull(0)?.items?.size)
    }

    @Test
    fun getkiboApi_returnWrongProductViewResponse() = runTest {
        val mockResponse = MockResponse()
        mockResponse.setResponseCode(200)
        mockResponse.setBody(WRONG_KIBO_RESPONSE)
        mockWebServer.enqueue(mockResponse)
        val product = Product("20018702","20018702")
        val list = ArrayList<Product>()
        list.add(product)
        val kiboProductRequest = KiboProductRequest("plist3620006", list)
        val response = apiHelper.getKiboProductsFromResponse(
            SESSION_TOKEN,
            DEVICE_TOKEN,
            kiboProductRequest
        )
        mockWebServer.takeRequest()
        Assert.assertEquals(true,
            response.body()?.data?.responses?.getOrNull(0)?.actions?.getOrNull(0)?.items.isNullOrEmpty())
    }

    @Test
    fun getkiboApi_returnNullResponse() = runTest {
        val mockResponse = MockResponse()
        mockResponse.setResponseCode(200)
        mockResponse.setBody("{}")
        mockWebServer.enqueue(mockResponse)
        val product = Product("20018702","20018702")
        val list = ArrayList<Product>()
        list.add(product)
        val kiboProductRequest = KiboProductRequest("plist3620006", list)
        val response = apiHelper.getKiboProductsFromResponse(
            SESSION_TOKEN,
            DEVICE_TOKEN,
            kiboProductRequest
        )
        mockWebServer.takeRequest()
        Assert.assertEquals(null,
            response.body()?.data)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    companion object {
        private const val RESPONSE = "{\n" +
                "    \"response\": {\n" +
                "        \"code\": \"-1\",\n" +
                "        \"desc\": \"Success\"\n" +
                "    },\n" +
                "    \"data\": [\n" +
                "        {\n" +
                "            \"substitutionInfo\": {\n" +
                "                \"displayName\": \"Free Range Jumbo Eggs 6 pk\",\n" +
                "                \"id\": \"20068905\"\n" +
                "            },\n" +
                "            \"links\": [],\n" +
                "            \"substitutionSelection\": \"USER_CHOICE\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"httpCode\": 200\n" +
                "}"

        const val USER_CHOICE = "USER_CHOICE"

        private const val WRONG_RESPONSE = "{\n" +
                "    \"response\": {\n" +
                "        \"code\": \"-1\",\n" +
                "        \"desc\": \"Success\"\n" +
                "    },\n" +
                "    \"data\": [\n" +
                "       \n" +
                "    ],\n" +
                "    \"httpCode\": 200\n" +
                "}"

        private const val INVENTORY_RESPONSE = "{\n" +
                "    \"storeId\": \"473\",\n" +
                "    \"skuInventory\": [\n" +
                "        {\n" +
                "            \"sku\": \"6001009025692\",\n" +
                "            \"quantity\": 17\n" +
                "        }\n" +
                "    ],\n" +
                "    \"response\": {\n" +
                "        \"code\": \"-1\",\n" +
                "        \"desc\": \"Success\"\n" +
                "    },\n" +
                "    \"httpCode\": 200\n" +
                "}"

        private const val WRONG_INVENTORY_RESPONSE = "{\n" +
                "    \"storeId\": \"\",\n" +
                "    \"skuInventory\": [\n" +
                "      \n" +
                "    ],\n" +
                "    \"response\": {\n" +
                "      \n" +
                "    },\n" +
                "    \"httpCode\": 200\n" +
                "}"

        private const val WRONG_ADD_SUBSTITUTION_RESPONSE = "{\n" +
                "    \"response\": {\n" +
                "        \"code\": \"-1\",\n" +
                "        \"desc\": \"Success\"\n" +
                "    },\n" +
                "    \"data\": [\n" +
                "       \n" +
                "        \n" +
                "    ],\n" +
                "    \"httpCode\": 200\n" +
                "}"

        private const val WRONG_SEARCH_API_Response = "{\n" +
                "    \"isBanners\": false,\n" +
                "    \"products\": [],\n" +
                "    \"pagingResponse\": {\n" +
                "        \"pageSize\": 60,\n" +
                "        \"pageOffset\": 0,\n" +
                "        \"numItemsOnPage\": 0,\n" +
                "        \"numItemsInTotal\": 0,\n" +
                "        \"numPages\": 0\n" +
                "    },\n" +
                "    \"sortOptions\": [\n" +
                "        {\n" +
                "            \"sortOption\": \"Sort by\",\n" +
                "            \"label\": \"Sort by\",\n" +
                "            \"selected\": false\n" +
                "        },\n" +
                "        {\n" +
                "            \"sortOption\": \"NEW|0\",\n" +
                "            \"label\": \"New In\",\n" +
                "            \"selected\": false\n" +
                "        },\n" +
                "        {\n" +
                "            \"sortOption\": \"p_salesRank|1\",\n" +
                "            \"label\": \"Best Sellers\",\n" +
                "            \"selected\": false\n" +
                "        },\n" +
                "        {\n" +
                "            \"sortOption\": \"p_pl10|0\",\n" +
                "            \"label\": \"Price Low-High\",\n" +
                "            \"selected\": false\n" +
                "        },\n" +
                "        {\n" +
                "            \"sortOption\": \"p_pl10|1\",\n" +
                "            \"label\": \"Price High-Low\",\n" +
                "            \"selected\": false\n" +
                "        },\n" +
                "        {\n" +
                "            \"sortOption\": \"p_displayName|1\",\n" +
                "            \"label\": \"Name Z-A\",\n" +
                "            \"selected\": false\n" +
                "        },\n" +
                "        {\n" +
                "            \"sortOption\": \"p_displayName|0\",\n" +
                "            \"label\": \"Name A-Z\",\n" +
                "            \"selected\": false\n" +
                "        },\n" +
                "        {\n" +
                "            \"sortOption\": \"p_averageRating|1||p_reviewCount|1||p_salesRank|0\",\n" +
                "            \"label\": \"Top Rated\",\n" +
                "            \"selected\": false\n" +
                "        }\n" +
                "    ],\n" +
                "    \"navigation\": [],\n" +
                "    \"history\": {\n" +
                "        \"searchCrumbs\": [\n" +
                "            {\n" +
                "                \"terms\": \"\\\"\\\"\",\n" +
                "                \"matchMode\": \"ALLPARTIAL\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"categoryDimensions\": []\n" +
                "    },\n" +
                "    \"richText\": \"\",\n" +
                "    \"response\": {\n" +
                "        \"code\": \"-1\",\n" +
                "        \"desc\": \"Success\"\n" +
                "    },\n" +
                "    \"httpCode\": 200\n" +
                "}"

        private const val SEARCH_RESPONSE = "{\n" +
                "    \"isBanners\": false,\n" +
                "    \"products\": [\n" +
                "        {\n" +
                "            \"productId\": \"6001009022882\",\n" +
                "            \"productName\": \"Balsamic Vinegar Of Modena - 4 Grape 250 ml\",\n" +
                "            \"externalImageRefV2\": \"https://assets.woolworthsstatic.co.za/Balsamic-Vinegar-Of-Modena-4-Grape-250-ml-6001009022882.jpg?V=ci0T&o=eyJidWNrZXQiOiJ3dy1vbmxpbmUtaW1hZ2UtcmVzaXplIiwia2V5IjoiaW1hZ2VzL2VsYXN0aWNlcmEvcHJvZHVjdHMvaGVyby8yMDIwLTA5LTI4LzYwMDEwMDkwMjI4ODJfaGVyby5qcGcifQ&\",\n" +
                "            \"isLiquor\": false,\n" +
                "            \"price\": \"59.99\",\n" +
                "            \"sku\": \"6001009022882\",\n" +
                "            \"promotions\": []\n" +
                "        }\n" +
                "    ],\n" +
                "    \"pagingResponse\": {\n" +
                "        \"pageSize\": 60,\n" +
                "        \"pageOffset\": 0,\n" +
                "        \"numItemsOnPage\": 2,\n" +
                "        \"numItemsInTotal\": 2,\n" +
                "        \"numPages\": 1\n" +
                "    },\n" +
                "    \"response\": {\n" +
                "        \"code\": \"-1\",\n" +
                "        \"desc\": \"Success\"\n" +
                "    },\n" +
                "    \"httpCode\": 200\n" +
                "}"

        private const val KIBO_RESPONSE = "{\n" +
                "    \"data\": {\n" +
                "        \"responses\": [\n" +
                "            {\n" +
                "                \"actions\": [\n" +
                "                    {\n" +
                "                        \"items\": [\n" +
                "                            {\n" +
                "                                \"_affinity\": 58.61,\n" +
                "                                \"slotIndex\": 1,\n" +
                "                                \"itemGroupId\": \"20018702\",\n" +
                "                                \"id\": \"20018702\",\n" +
                "                                \"ratings\": 0,\n" +
                "                                \"title\": \"White Thick Slice Bread 700 g\",\n" +
                "                                \"imageLink\": \"https://assets.woolworthsstatic.co.za/White-Thick-Slice-Bread-700-g-20018702.jpg?V=W7DN&o=eyJidWNrZXQiOiJ3dy1vbmxpbmUtaW1hZ2UtcmVzaXplIiwia2V5IjoiaW1hZ2VzL2VsYXN0aWNlcmEvcHJvZHVjdHMvaGVyby8yMDE0LTA2LTA1LzIwMDE4NzAyX2hlcm8uanBnIn0&\",\n" +
                "                                \"priceRange\": \"FALSE\",\n" +
                "                                \"link\": \"/prod/Food/Bread-Bakery-Desserts/Bread-Rolls-Wraps-Bagels/Bread/White-Bread/White-Thick-Slice-Bread-700-g/_/A-20018702\",\n" +
                "                                \"plist3620006\": 21,\n" +
                "\"plist3620009\": 16.69,\n" +
                "                                \"plist3620008\": 16.69\n" +
                "                            }\n" +
                "                        ]\n" +
                "\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    \"response\": {\n" +
                "        \"code\": \"-1\",\n" +
                "        \"desc\": \"Success\"\n" +
                "    },\n" +
                "    \"httpCode\": 200\n" +
                "}"

        private const val WRONG_KIBO_RESPONSE = "{\n" +
                "    \"data\": {\n" +
                "        \"responses\": [\n" +
                "            {\n" +
                "                \"actions\": [\n" +
                "                    {\n" +
                "                        \"items\": [\n" +
                "                        ]\n" +
                "\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    \"response\": {\n" +
                "        \"code\": \"-1\",\n" +
                "        \"desc\": \"Success\"\n" +
                "    },\n" +
                "    \"httpCode\": 200\n" +
                "}"
    }
}