package tech.harrynull

import com.apurebase.kgraphql.GraphQL
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun Application.module() {
    install(GraphQL) {
        playground = true
        buildSchema()
    }
}

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

