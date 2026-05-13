package ch.fitnesslab.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import reactor.core.publisher.Sinks
import java.util.concurrent.atomic.AtomicBoolean

class UpdateSubscriptionQueryTest {
    @Test
    fun `waitForUpdateOf activates subscription before running action`() {
        val subscribed = AtomicBoolean(false)
        val sink = Sinks.many().unicast().onBackpressureBuffer<Any>()
        val publisher =
            sink
                .asFlux()
                .doOnSubscribe {
                    subscribed.set(true)
                    sink.tryEmitNext("initial")
                }

        val result =
            waitForUpdateOf(publisher, durationInSeconds = 1) {
                assertTrue(subscribed.get())
                sink.tryEmitNext("update")
                "done"
            }

        assertEquals("done", result)
    }

    @Test
    fun `waitForUpdatesOf waits for all expected updates`() {
        val sink = Sinks.many().unicast().onBackpressureBuffer<Any>()
        val publisher =
            sink
                .asFlux()
                .doOnSubscribe {
                    sink.tryEmitNext("initial")
                }

        val result =
            waitForUpdatesOf(publisher, updateCount = 2, durationInSeconds = 1) {
                sink.tryEmitNext("first update")
                sink.tryEmitNext("second update")
                42
            }

        assertEquals(42, result)
    }
}
