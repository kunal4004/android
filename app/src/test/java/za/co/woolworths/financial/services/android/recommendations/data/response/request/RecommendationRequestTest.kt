package za.co.woolworths.financial.services.android.recommendations.data.response.request

import org.junit.Assert
import org.junit.Test

class RecommendationRequestTest {

    @Test
    fun `recommendation request data test with empty data`() {
        val recommendationRequest = RecommendationRequest(
            events = emptyList(),
            monetateId = ""
        )
        Assert.assertEquals(recommendationRequest.monetateId, "")
        Assert.assertTrue(recommendationRequest.events!!.isEmpty())
    }

    @Test
    fun `recommendation request data test with data`() {
        val monetateId = "id1"
        val eventType = "event1"
        val recommendationRequest = RecommendationRequest(
            events = arrayListOf(Event(eventType = eventType)),
            monetateId = monetateId
        )
        Assert.assertEquals(recommendationRequest.monetateId, monetateId)
        Assert.assertTrue(recommendationRequest.events!!.isNotEmpty())
        Assert.assertEquals((recommendationRequest.events!![0] as Event).eventType, eventType)
    }
}