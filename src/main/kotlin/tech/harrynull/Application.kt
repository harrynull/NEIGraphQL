package tech.harrynull

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import tech.harrynull.plugins.*

fun Application.module(testing: Boolean = false) {
    install(GraphQL) {
        configureRouting()
        playground = true
        schema {
            query("hello") {
                resolver { -> "World" }
            }
        }
    }
}

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

