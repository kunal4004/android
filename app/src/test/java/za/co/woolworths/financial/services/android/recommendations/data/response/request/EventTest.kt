package za.co.woolworths.financial.services.android.recommendations.data.response.request

import org.junit.Assert
import org.junit.Test

class EventTest {

    @Test
    fun `event type test`() {
        val event = Event(
            eventType = "eventRec",
            url = "/recUrl",
            pageType = "EmptyCart",
            categories = arrayListOf("food"),
            products = arrayListOf(ProductX(productId = "123456")),
            cartLines = arrayListOf(CartProducts(pid = "654321", currency = "ZAR", sku = "abcd", value = 1.2)),
            orderId = "orderId",
            purchaseLines = emptyList(),
            userAgent = "deviceUserAgent",
            ipAddress = "deviceIP",
            recClicks = null,
            recImpressions = null
        )
        Assert.assertFalse(event.eventType.isNullOrEmpty())
        Assert.assertEquals(event.eventType, "eventRec")
        Assert.assertEquals(event.url, "/recUrl")
        Assert.assertEquals(event.pageType, "EmptyCart")
        Assert.assertEquals(event.categories!![0], "food")
        Assert.assertEquals(event.products!![0].productId, "123456")
        Assert.assertEquals(event.cartLines!![0]!!.pid, "654321")
        Assert.assertEquals(event.orderId, "orderId")
        Assert.assertEquals(event.userAgent, "deviceUserAgent")
        Assert.assertEquals(event.ipAddress, "deviceIP")
        Assert.assertTrue(event.purchaseLines!!.isEmpty())
        Assert.assertNull(event.recClicks)
        Assert.assertNull(event.recImpressions)
    }

    @Test
    fun `recommendation pageview data test with data`() {
        val pageType = "pageType"
        val eventType = "event1"
        val url = "/dummyUrl"
        val recommendation = Recommendation.PageView(
            eventType = eventType,
            pageType = pageType,
            url = url
        )
        Assert.assertEquals(recommendation.pageType, pageType)
        Assert.assertEquals(recommendation.eventType, eventType)
        Assert.assertEquals(recommendation.url, url)
    }

    @Test
    fun `recommendation shopping list event data test`() {
        val eventType = "event1"
        val productId = "product1"
        val recommendation = Recommendation.ShoppingListEvent(
            eventType = eventType,
            products = arrayListOf(Product(productId = productId))
        )
        Assert.assertEquals(recommendation.eventType, eventType)
        Assert.assertEquals(recommendation.products!![0].productId, productId)
    }
}