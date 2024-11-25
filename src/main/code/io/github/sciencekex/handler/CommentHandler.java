package io.github.sciencekex.handler;

import io.github.sciencekex.model.Article;
import io.github.sciencekex.model.Comment;
import io.github.sciencekex.model.User;
import io.github.sciencekex.repository.ArticleRepository;
import io.github.sciencekex.repository.CommentRepository;
import io.github.sciencekex.repository.UserRepository;
import io.github.sciencekex.security.Secured;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.util.HashMap;
import java.util.Map;

@Path("/comments")
public class CommentHandler {

    @Inject
    private CommentRepository commentRepository;

    @Inject
    private UserRepository userRepository;

    @Inject
    private ArticleRepository articleRepository;

    @POST
    @Path("/")
    @Secured({"user", "admin"})
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createComment(Comment comment, @Context SecurityContext securityContext) {
        User user = userRepository.findByID(Integer.valueOf(securityContext.getUserPrincipal().getName()));
        comment.setUser(user);
        Article article = articleRepository.findByID(comment.getArticleId());
        comment.setArticle(article);
        comment.setCreatedAt(System.currentTimeMillis() / 1000);
        commentRepository.create(comment);
        Map<String, Object> res = new HashMap<>();
        res.put("code", Response.Status.OK);
        return Response.status(Response.Status.OK).entity(res).build();
    }

    @DELETE
    @Path("/{id}")
    @Secured({"admin"})
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteComment(@PathParam("id") Integer commentId) {
            commentRepository.delete(commentId);
            Map<String, Object> res = new HashMap<>();
            res.put("code", Response.Status.OK.getStatusCode());
            res.put("message", "Comment deleted successfully");
            return Response.ok().entity(res).build();
    }
}
