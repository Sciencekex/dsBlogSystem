package io.github.sciencekex.util

import org.mindrot.jbcrypt.BCrypt

object BCryptUtil {
    @JvmStatic
    fun hashPassword(plainPassword: String?): String {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt())
    }


    @JvmStatic
    fun checkPassword(plainPassword: String?, hashedPassword: String?): Boolean {
        return BCrypt.checkpw(plainPassword, hashedPassword)
    }
}
