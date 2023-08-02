package za.co.woolworths.financial.services.android.ui.fragments.product.shop.usecase

import com.awfs.coordination.R
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockedStatic
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.`when`
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.cart.DeliveryDetails
import za.co.woolworths.financial.services.android.models.dto.voucher_and_promo_code.DiscountDetails
import za.co.woolworths.financial.services.android.models.network.Resource
import za.co.woolworths.financial.services.android.models.network.Status
import za.co.woolworths.financial.services.android.recommendations.data.repository.RecommendationsRepository
import za.co.woolworths.financial.services.android.recommendations.data.response.getresponse.RecommendationResponse
import za.co.woolworths.financial.services.android.recommendations.data.response.request.Event
import za.co.woolworths.financial.services.android.recommendations.data.response.request.RecommendationRequest
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.usecase.dummydata.SubmittedOrdersResponseDummy
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.usecase.fake.RecommendationsRepositoryImplFake
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.utils.mock


@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(PowerMockRunner::class)
@PrepareForTest(Utils::class)
class SubmitRecommendationsUseCaseTest {

    private var submitRecommendationsUseCase =
        SubmitRecommendationsUseCase(RecommendationsRepositoryImplFake())
    private var utilStaticMock: MockedStatic<Utils>? = null

    @Before
    fun setUp() {
        utilStaticMock = mockStatic(Utils::class.java)
        WoolworthsApplication.testSetInstance(mock())
    }

    @After
    fun tearDown() {
        utilStaticMock?.close()
    }

    @Test
    fun `submit the recommendation data with success in response`() = runTest {
        `when`(Utils.getMonetateId()).thenReturn("some-dummy-monetate-id")
        val submitOrderResponse = SubmittedOrdersResponseDummy.validSubmittedOrderResponse()

        val response = submitRecommendationsUseCase.invoke(submitOrderResponse)

        assertEquals(response.status, Status.SUCCESS)
        assertNotNull(response.data)
        assertEquals(response.data?.httpCode, 200)
    }

    @Test
    fun `submit the recommendation data success with shipping and discount details added in the request`() =
        runTest {
            `when`(Utils.getMonetateId()).thenReturn("some-dummy-monetate-id")
            val submitOrderResponse = SubmittedOrdersResponseDummy.validSubmittedOrderResponse()
            submitOrderResponse.deliveryDetails = DeliveryDetails().apply { shippingAmount = 200.0 }
            submitOrderResponse.orderSummary?.orderId = "order-id-101"
            submitOrderResponse.orderSummary?.discountDetails = DiscountDetails(
                totalDiscount = 10.0,
                otherDiscount = 5.0,
                voucherDiscount = 5.0,
                promoCodeDiscount = 0.0,
                wrewardsDiscount = 0.0,
                companyDiscount = 0.0,
                totalOrderDiscount = 20.0
            )

            submitRecommendationsUseCase =
                SubmitRecommendationsUseCase(object : RecommendationsRepository {
                    override suspend fun getRecommendationResponse(recommendationRequest: RecommendationRequest?): Resource<RecommendationResponse> {

                        assertAllValuesSubmittedInRecommendationRequest(recommendationRequest)

                        val validRequest = RecommendationsRepositoryImplFake.verifyValidRequest(
                            recommendationRequest
                        )
                        return if (validRequest) {
                            Resource.success(
                                RecommendationResponse(
                                    httpCode = 200,
                                    actions = null,
                                    monetateId = null,
                                    response = null,
                                    title = null
                                )
                            )
                        } else {
                            Resource.error(R.string.error_unknown, null)
                        }
                    }
                })

            val response = submitRecommendationsUseCase(submitOrderResponse)

            assertEquals(response.status, Status.SUCCESS)
            assertNotNull(response.data)
            assertEquals(response.data?.httpCode, 200)
        }

    @Test
    fun `submit the recommendation data with failure when monetated id is null`() = runTest {
        `when`(Utils.getMonetateId()).thenReturn(null)
        val submitOrderResponse = SubmittedOrdersResponseDummy.validSubmittedOrderResponse()

        val response = submitRecommendationsUseCase.invoke(submitOrderResponse)

        assertEquals(response.status, Status.ERROR)
        assertEquals(response.data, null)
    }

    @Test
    fun `submit the recommendation data with failure when order id is null`() = runTest {
        `when`(Utils.getMonetateId()).thenReturn("some-dummy-monetate-id")
        val submitOrderResponse = SubmittedOrdersResponseDummy.validSubmittedOrderResponse()
        submitOrderResponse.orderSummary?.orderId = null

        val response = submitRecommendationsUseCase.invoke(submitOrderResponse)

        assertEquals(response.status, Status.ERROR)
        assertEquals(response.data, null)
    }

    @Test
    fun `submit the recommendation data with failure when ordered items is null`() = runTest {
        `when`(Utils.getMonetateId()).thenReturn("some-dummy-monetate-id")
        val submitOrderResponse = SubmittedOrdersResponseDummy.validSubmittedOrderResponse()
        submitOrderResponse.items = null

        val response = submitRecommendationsUseCase.invoke(submitOrderResponse)

        assertEquals(response.status, Status.ERROR)
        assertEquals(response.data, null)
    }

    private fun assertAllValuesSubmittedInRecommendationRequest(
        recommendationRequest: RecommendationRequest?
    ) {
        val currency = "ZAR"
        assertEquals(recommendationRequest?.monetateId, "some-dummy-monetate-id")
        assertEquals(recommendationRequest?.events?.size, 4)

        val eventPageView = recommendationRequest?.events?.get(0) as? Event
        assertEquals(eventPageView?.eventType, "monetate:context:PageView")
        assertEquals(eventPageView?.url, "/orderDetails")
        assertEquals(eventPageView?.pageType, "purchase")

        val eventPurchase = recommendationRequest?.events?.get(1) as? Event

        assertEquals(eventPurchase?.orderId, "order-id-101")
        assertEquals(eventPurchase?.eventType, "monetate:context:Purchase")
        assertEquals(eventPurchase?.purchaseLines?.size, 6)
        assertEquals(eventPurchase?.url, null)
        assertEquals(eventPurchase?.cartLines, null)
        assertEquals(eventPurchase?.categories, null)
        assertEquals(eventPurchase?.pageType, null)
        assertEquals(eventPurchase?.products, null)

        val foodProduct1 = eventPurchase?.purchaseLines?.filter { it?.pid == "food-product-1" }?.get(0)
        assertEquals(foodProduct1?.pid, "food-product-1")
        assertEquals(foodProduct1?.sku, "food-product-1")
        assertEquals(foodProduct1?.currency, currency)
        assertEquals(foodProduct1?.quantity, 1)
        assertEquals(foodProduct1?.value, 15.0)


        val foodProduct2 = eventPurchase?.purchaseLines?.filter { it?.pid == "food-product-2" }?.get(0)
        assertEquals(foodProduct2?.pid, "food-product-2")
        assertEquals(foodProduct2?.sku, "food-product-2")
        assertEquals(foodProduct2?.currency, currency)
        assertEquals(foodProduct2?.quantity, 2)
        assertEquals(foodProduct2?.value, 20.0)


        val otherProduct1 = eventPurchase?.purchaseLines?.filter { it?.pid == "other-product-1" }?.get(0)
        assertEquals(otherProduct1?.pid, "other-product-1")
        assertEquals(otherProduct1?.sku, "other-product-1")
        assertEquals(otherProduct1?.currency, currency)
        assertEquals(otherProduct1?.quantity, 1)
        assertEquals(otherProduct1?.value, 25.0)

        val otherProduct2 = eventPurchase?.purchaseLines?.filter { it?.pid == "other-product-2" }?.get(0)
        assertEquals(otherProduct2?.pid, "other-product-2")
        assertEquals(otherProduct2?.sku, "other-product-2")
        assertEquals(otherProduct2?.currency, currency)
        assertEquals(otherProduct2?.quantity, 2)
        assertEquals(otherProduct2?.value, 30.0)

        val shippingItem = eventPurchase?.purchaseLines?.filter { it?.pid == "SHIPPING" }?.get(0)
        assertEquals(shippingItem?.pid, "SHIPPING")
        assertEquals(shippingItem?.sku, "SHIPPING")
        assertEquals(shippingItem?.currency, currency)
        assertEquals(shippingItem?.quantity, 1)
        assertEquals(shippingItem?.value, 200.0)

        val discountItem = eventPurchase?.purchaseLines?.filter { it?.pid == "DISCOUNT" }?.get(0)
        assertEquals(discountItem?.pid, "DISCOUNT")
        assertEquals(discountItem?.sku, "DISCOUNT")
        assertEquals(discountItem?.currency, currency)
        assertEquals(discountItem?.quantity, 1)
        assertEquals(discountItem?.value, 20.0)
    }
}