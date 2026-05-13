package ch.fitnesslab.utils

import org.reactivestreams.Publisher
import reactor.core.Disposable
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration

fun <T> waitForUpdateOf(
    subscription: Publisher<*>,
    durationInSeconds: Long = 5,
    action: () -> T,
): T =
    waitForUpdatesOf(
        subscriptions = listOf(subscription),
        durationInSeconds = durationInSeconds,
        action = action,
    )

fun <T> waitForUpdatesOf(
    subscriptions: Iterable<Publisher<*>>,
    durationInSeconds: Long = 5,
    action: () -> T,
): T {
    val timeout = Duration.ofSeconds(durationInSeconds)
    val activeSubscriptions = subscriptions.map { it.activateUpdateSubscription() }

    try {
        // Axon 5 dispatches subscription queries lazily; wait for the initial result before commands emit updates.
        activeSubscriptions.forEach { it.initialResult.block(timeout) }

        val result = action()

        activeSubscriptions.forEach { it.update.block(timeout) }

        return result
    } finally {
        activeSubscriptions.forEach { it.dispose() }
    }
}

fun <T> waitForUpdatesOf(
    subscription: Publisher<*>,
    updateCount: Long,
    durationInSeconds: Long = 5,
    action: () -> T,
): T {
    if (updateCount <= 0) {
        return action()
    }

    val timeout = Duration.ofSeconds(durationInSeconds)
    val activeSubscription = subscription.activateUpdateSubscription(updateCount)

    try {
        // Axon 5 dispatches subscription queries lazily; wait for the initial result before commands emit updates.
        activeSubscription.initialResult.block(timeout)

        val result = action()

        activeSubscription.update.block(timeout)

        return result
    } finally {
        activeSubscription.dispose()
    }
}

private fun Publisher<*>.activateUpdateSubscription(updateCount: Long = 1): ActiveUpdateSubscription {
    val sharedSubscription = Flux.from(this).publish().autoConnect(2)
    val update =
        sharedSubscription
            .skip(1)
            .take(updateCount)
            .then()
            .cache()

    return ActiveUpdateSubscription(
        initialResult = sharedSubscription.next(),
        update = update,
        updateSubscription = update.subscribe(),
    )
}

private data class ActiveUpdateSubscription(
    val initialResult: Mono<*>,
    val update: Mono<Void>,
    val updateSubscription: Disposable,
) {
    fun dispose() {
        updateSubscription.dispose()
    }
}
