package io.github.sciencekex.security

import io.github.sciencekex.model.User
import io.github.sciencekex.repository.UserRepository
import io.github.sciencekex.util.JwtUtil
import jakarta.annotation.Priority
import jakarta.inject.Inject
import jakarta.ws.rs.NotAuthorizedException
import jakarta.ws.rs.Priorities
import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.container.ContainerRequestFilter
import jakarta.ws.rs.container.ResourceInfo
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.HttpHeaders
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.SecurityContext
import jakarta.ws.rs.ext.Provider
import java.io.IOException
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Method
import java.security.Principal
import java.util.logging.Logger

@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
class AuthenticationFilter : ContainerRequestFilter {

    @Context
    private lateinit var resourceInfo: ResourceInfo

    @Inject
    private lateinit var userRepository: UserRepository

    override fun filter(containerRequestContext: ContainerRequestContext) {
        val resourceMethod: Method = resourceInfo.resourceMethod
        val methodRoles: List<String> = extractRoles(resourceMethod)

        val authorizationHeader: String? = containerRequestContext.getHeaderString(HttpHeaders.AUTHORIZATION)

        if (authorizationHeader.isNullOrEmpty() || !authorizationHeader.startsWith("Bearer ")) {
            throw NotAuthorizedException("invalid_authorization_header")
        }

        val token: String = authorizationHeader.removePrefix("Bearer ").trim()
        val id: Int = try {
            JwtUtil.validateToken(token).toInt()
        } catch (e: Exception) {
            containerRequestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build())
            return
        }

        containerRequestContext.securityContext = object : SecurityContext {
            override fun getUserPrincipal(): Principal = Principal { id.toString() }
            override fun isUserInRole(role: String): Boolean = false
            override fun isSecure(): Boolean = false
            override fun getAuthenticationScheme(): String = ""
        }

        val user: User = userRepository.findByID(id)
        if (methodRoles.any { it == user.role }) {
            return
        }

        containerRequestContext.abortWith(Response.status(Response.Status.FORBIDDEN).build())
    }

    private fun extractRoles(annotatedElement: AnnotatedElement?): List<String> {
        val secured = annotatedElement?.getAnnotation(Secured::class.java)
        return secured?.value?.toList() ?: emptyList()
    }

    companion object {
        private val LOGGER: Logger = Logger.getLogger(AuthenticationFilter::class.java.name)
    }
}
