package io.github.sciencekex.util

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.SignatureAlgorithm
import java.security.Key
import java.util.Date
import javax.crypto.spec.SecretKeySpec

object JwtUtil {
    private const val SECRET_KEY = "20111111201111112011111120111111"
    private val key: Key = SecretKeySpec(SECRET_KEY.toByteArray(), SignatureAlgorithm.HS256.getJcaName())


    @JvmStatic
    fun generateToken(userId: Int): String? {
        val expirationTimeMillis = (1000 * 60 * 60 * 2).toLong()
        val expirationDate = Date(System.currentTimeMillis() + expirationTimeMillis)

        return Jwts.builder().setSubject(userId.toString()).signWith(key).setExpiration(expirationDate).compact()
    }


    @Throws(MalformedJwtException::class)
    fun validateToken(token: String?): String? {
        val claims: Claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody()
        return claims.getSubject()
    }
}
