package com.codeartify.axoniq

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AxoniqApplication

fun main(args: Array<String>) {
	runApplication<AxoniqApplication>(*args)
}
