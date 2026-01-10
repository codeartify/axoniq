package ch.fitnesslab.utils

import org.axonframework.queryhandling.SubscriptionQueryResult
import java.time.Duration

fun <V, U> waitForUpdateOf(
    subscription: SubscriptionQueryResult<MutableList<V>, U>,
    durationInSeconds: Long = 5,
) {
    subscription.updates().blockFirst(Duration.ofSeconds(durationInSeconds))
}
