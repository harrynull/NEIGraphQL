package tech.harrynull

import com.apurebase.kgraphql.GraphQL
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.cors.routing.*

fun Application.module() {
    install(GraphQL) {
        playground = true
        buildSchema()
    }
    install(CORS) {
        anyHost()
        allowMethod(HttpMethod.Options)
        allowHeaders { true }
        allowHeader("Content-Type")
    }
}

fun main() {
    embeddedServer(Netty, port = 8003, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

