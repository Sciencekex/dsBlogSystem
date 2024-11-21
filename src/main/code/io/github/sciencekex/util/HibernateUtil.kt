package io.github.sciencekex.util

import jakarta.persistence.EntityManager
import jakarta.persistence.EntityManagerFactory
import jakarta.persistence.Persistence

object HibernateUtil {
    private var emf: EntityManagerFactory? = null


    fun init() {
        emf = Persistence.createEntityManagerFactory("default")
    }


    @JvmStatic
    @Throws(IllegalAccessException::class)
    fun copyNonNullProperties(source: Any, target: Any?) {
        val fields = source.javaClass.getDeclaredFields()
        for (field in fields) {
            field.setAccessible(true)
            val value = field.get(source)
            if (value != null) {
                field.set(target, value)
            }
        }
    }

    fun getEntityManagerFactory(): EntityManagerFactory {
        return emf!!
    }


    @JvmStatic
    fun getEntityManager(): EntityManager? {
        return emf!!.createEntityManager()
    }
}
