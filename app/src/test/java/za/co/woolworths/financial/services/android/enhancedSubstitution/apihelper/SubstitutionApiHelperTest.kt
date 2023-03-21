package za.co.woolworths.financial.services.android.enhancedSubstitution.apihelper

import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import za.co.woolworths.financial.services.android.models.network.ApiInterface

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
    fun test_withNullOrEmptyResponse_getSubstitutions() = runTest {
        val mockResponse = MockResponse()
        mockResponse.setBody("{}")
        mockWebServer.enqueue(mockResponse)

        val response = apiHelper.getSubstitution(anyString(), anyString(), "123456")
        mockWebServer.takeRequest()

        Assert.assertEquals(null , response.body()?.data?.isNullOrEmpty())
    }

    @Test
    fun test_SubstitutionResponse_getSubstitutions() = runTest {
        val mockResponse = MockResponse()
        mockResponse.setResponseCode(200)
        mockResponse.setBody(RESPONSE)

        mockWebServer.enqueue(mockResponse)

        val response = apiHelper.getSubstitution(anyString(), anyString(), "123456")
        mockWebServer.takeRequest()

        Assert.assertEquals(1 , response.body()?.data?.size)
        Assert.assertEquals(USER_CHOICE , response.body()?.data?.get(0)?.substitutionSelection)
    }

    @Test
    fun test_wrongResponse_getSubstitutions() = runTest {
        val mockResponse = MockResponse()
        mockResponse.setResponseCode(200)
        mockResponse.setBody(EMPTY_RESPONSE)

        mockWebServer.enqueue(mockResponse)

        val response = apiHelper.getSubstitution(anyString(), anyString(), "123456")
        mockWebServer.takeRequest()

        Assert.assertEquals(1 , response.body()?.data?.size)
        Assert.assertEquals(USER_CHOICE , response.body()?.data?.get(0)?.substitutionSelection)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    companion object {
        private val RESPONSE = "{\n" +
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

        private val USER_CHOICE = "USER_CHOICE"

        private val EMPTY_RESPONSE = "{\"substitutionInfo\": \"\",\"links\": [],\"substitutionSelection\": \"\"}"
    }
}