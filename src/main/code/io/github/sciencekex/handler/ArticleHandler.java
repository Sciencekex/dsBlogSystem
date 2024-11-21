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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/articles")
public class ArticleHandler {

    @Inject
    private ArticleRepository articleRepository;

    @Inject
    private CommentRepository commentRepository;

    @Inject
    private UserRepository userRepository;

    //->:重用之前的sanitizeUser方法
    private Map<String, Object> sanitizeUser(User user) {
        Map<String, Object> sanitizedUser = new HashMap<>();
        sanitizedUser.put("id", user.getId());
        sanitizedUser.put("username", user.getUsername());
        sanitizedUser.put("nickname", user.getNickname());
        sanitizedUser.put("role", user.getRole());
        return sanitizedUser;
    }

    //->:创建一个处理文章的方法，确保作者信息被净化
    private Map<String, Object> sanitizeArticle(Article article) {
        Map<String, Object> sanitizedArticle = new HashMap<>();
        sanitizedArticle.put("id", article.getId());
        sanitizedArticle.put("title", article.getTitle());
        sanitizedArticle.put("content", article.getContent());
        sanitizedArticle.put("author", sanitizeUser(article.getAuthor()));
        sanitizedArticle.put("author_id", article.getAuthorId());
        sanitizedArticle.put("created_at", article.getCreatedAt());
        return sanitizedArticle;
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllArticles() {
        List<Article> articles = articleRepository.findAll();
        //->:处理文章列表，移除密码信息
        List<Map<String, Object>> sanitizedArticles = articles.stream()
            .map(this::sanitizeArticle)
            .collect(Collectors.toList());
        
        Map<String, Object> res = new HashMap<>();
        res.put("code", Response.Status.OK);
        res.put("data", sanitizedArticles);
        return Response.status(Response.Status.OK).entity(res).build();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getArticleById(@PathParam("id") Integer id) {
        Article article = articleRepository.findByID(id);
        if (article == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        Map<String, Object> res = new HashMap<>();
        res.put("code", Response.Status.OK);
        res.put("data", sanitizeArticle(article));
        return Response.status(Response.Status.OK).entity(res).build();
    }

    @GET
    @Path("/{id}/comments")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getComments(@PathParam("id") Integer id) {
        List<Comment> comments = commentRepository.findByArticleId(id);
        Map<String, Object> res = new HashMap<>();
        res.put("code", Response.Status.OK);
        res.put("data", comments);
        return Response.status(Response.Status.OK).entity(res).build();
    }

    @POST
    @Path("/")
    @Secured({"admin"})
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createArticle(Article article, @Context SecurityContext securityContext) {
        User author = userRepository.findByID(Integer.valueOf(securityContext.getUserPrincipal().getName()));
        article.setAuthor(author);
        article.setCreatedAt(System.currentTimeMillis() / 1000);
        articleRepository.create(article);
        Map<String, Object> res = new HashMap<>();
        res.put("code", Response.Status.OK);
        return Response.status(Response.Status.OK).entity(res).build();
    }

    @PUT
    @Path("/")
    @Secured({"admin"})
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateArticle(Article article, @Context SecurityContext securityContext) {
        article.setAuthorId(Integer.valueOf(securityContext.getUserPrincipal().getName()));
        articleRepository.update(article);
        Map<String, Object> res = new HashMap<>();
        res.put("code", Response.Status.OK);
        return Response.status(Response.Status.OK).entity(res).build();
    }
}