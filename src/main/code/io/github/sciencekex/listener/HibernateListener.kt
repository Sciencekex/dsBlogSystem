package io.github.sciencekex.listener

import io.github.sciencekex.util.HibernateUtil
import jakarta.persistence.PersistenceException
import jakarta.servlet.ServletContextEvent
import jakarta.servlet.ServletContextListener
import jakarta.servlet.annotation.WebListener

@WebListener
class HibernateListener : ServletContextListener {
    override fun contextInitialized(sce: ServletContextEvent?) {
        try {
            HibernateUtil.init()
        } catch (ex: PersistenceException) {
            System.err.println("Database connection failed. Server will shut down.")
            shutdownServer()
        }
    }

    override fun contextDestroyed(sce: ServletContextEvent?) {
        if (HibernateUtil.getEntityManagerFactory() != null && HibernateUtil.getEntityManagerFactory().isOpen()) {
            HibernateUtil.getEntityManagerFactory().close()
        }
    }

    private fun shutdownServer() {
        System.exit(1)
    }
}
