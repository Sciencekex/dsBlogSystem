package io.github.sciencekex.repository;

import io.github.sciencekex.model.Comment;
import io.github.sciencekex.util.HibernateUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;

import java.util.List;

@ApplicationScoped
public class CommentRepository {

    public List<Comment> findByArticleId(Integer id) throws PersistenceException {
        EntityManager em = HibernateUtil.getEntityManager();
        List<Comment> comments = null;
        try {
            em.getTransaction().begin();
            comments = em
                    .createQuery("SELECT c FROM Comment c WHERE c.articleId = :articleId", Comment.class)
                    .setParameter("articleId", id)
                    .getResultList();
            em.getTransaction().commit();
        } catch (PersistenceException e) {
            em.getTransaction().rollback();
            throw new RuntimeException("", e);
        } finally {
            em.close();
        }
        return comments;
    }

    public void create(Comment comment) throws PersistenceException {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(comment);
            em.getTransaction().commit();
        } catch (PersistenceException e) {
            em.getTransaction().rollback();
            throw new RuntimeException("", e);
        } finally {
            em.close();
        }
    }


    public Comment findById(Long id) throws PersistenceException {
        EntityManager em = HibernateUtil.getEntityManager();
        Comment comment = null;
        try {
            em.getTransaction().begin();
            comment = em.find(Comment.class, id);
            em.getTransaction().commit();
        } catch (PersistenceException e) {
            em.getTransaction().rollback();
            throw new RuntimeException("Failed to find comment", e);
        } finally {
            em.close();
        }
        return comment;
    }


    public void delete(Integer id) throws PersistenceException {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            Comment comment = em.find(Comment.class, id);
            em.remove(comment);
            em.getTransaction().commit();
        } catch (PersistenceException e) {
            em.getTransaction().rollback();
            throw new RuntimeException("Failed to delete comment", e);
        } finally {
            em.close();
        }
    }
}
