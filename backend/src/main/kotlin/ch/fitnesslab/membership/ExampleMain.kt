package ch.fitnesslab.membership

data class Event(
    val id: Int,
    val paymentId: Int,
    val type: String,
)

enum class Status {
    AUTHORIZED,
    CAPTURED,
}

class Payment(
    id: Int,
) {
    val id: Int = id
    var status: Status = Status.AUTHORIZED

    fun capture() {
        this.status = Status.CAPTURED
    }
}

fun main(args: Array<String>) {
    val payments =
        mutableMapOf(
            1 to Payment(1),
        )

    val pendingCapture = mutableListOf<Event>()

    val events =
        listOf(
            Event(1, 1, "SHIPMENT_CREATED"),
            Event(1, 2, "SHIPMENT_CREATED"),
        )

    val inbox = mutableMapOf<Event, Boolean>()

    events.forEach {
        val isProcessed = inbox.get(it)
        if (isProcessed == true) return

        val payment = payments.get(it.paymentId)

        if (payment == null) {
            pendingCapture.add(it)
            inbox[it] = true
            println("Payment not yet authorised but should be captured")
        }

        payment?.capture()
        inbox[it] = true
        println("Payment captured")
    }

    // simulate payment authorised
    payments[2] = Payment(2)

    // reapply events that could not be processed
}
