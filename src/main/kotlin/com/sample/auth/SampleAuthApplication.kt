package com.sample.auth

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class SampleAuthApplication

fun main(args: Array<String>) {
    runApplication<SampleAuthApplication>(*args)
}
