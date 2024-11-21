package io.github.sciencekex.handler

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path

@Path("/")
class IndexHandler {
    @GET
    fun index(): String {
        return "Hello World!"
    }
}
