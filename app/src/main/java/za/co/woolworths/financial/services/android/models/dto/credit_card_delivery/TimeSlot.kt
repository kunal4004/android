package za.co.woolworths.financial.services.android.models.dto.credit_card_delivery

data class TimeSlot(var date: String, val availableTimeslots: List<String>) {
    override fun toString(): String {
        return date
    }
}