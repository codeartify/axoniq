package ch.fitnesslab.utils

import org.reactivestreams.Publisher
import reactor.core.publisher.Flux
import java.time.Duration

fun waitForUpdateOf(
    subscription: Publisher<*>,
    durationInSeconds: Long = 5,
) {
    Flux
        .from(subscription)
        .skip(1)
        .blockFirst(Duration.ofSeconds(durationInSeconds))
}
