package io.github.sciencekex.handler;

import io.github.sciencekex.model.User;
import io.github.sciencekex.model.request.UserLoginRequest;
import io.github.sciencekex.repository.UserRepository;
import io.github.sciencekex.security.Secured;
import io.github.sciencekex.util.BCryptUtil;
import io.github.sciencekex.util.JwtUtil;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/users")
public class UserHandler {

    @Inject
    private UserRepository userRepository;

    //->:创建一个用于传输的用户DTO，不包含密码
    private Map<String, Object> sanitizeUser(User user) {
        Map<String, Object> sanitizedUser = new HashMap<>();
        sanitizedUser.put("id", user.getId());
        sanitizedUser.put("username", user.getUsername());
        sanitizedUser.put("nickname", user.getNickname());
        sanitizedUser.put("role", user.getRole());
        return sanitizedUser;
    }

    @GET
    @Path("/")
    @Secured({"user", "admin"})
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllUsers() {
        List<User> users = userRepository.findAll();
        //->:转换用户列表，移除密码信息
        List<Map<String, Object>> sanitizedUsers = users.stream()
            .map(this::sanitizeUser)
            .collect(Collectors.toList());

        Map<String, Object> res = new HashMap<>();
        res.put("code", Response.Status.OK);
        res.put("data", sanitizedUsers);
        return Response.status(Response.Status.OK).entity(res).build();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserById(@PathParam("id") int id) {
        User user = userRepository.findByID(id);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(sanitizeUser(user)).build();
    }

    @POST
    @Path("/login")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response login(UserLoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername());
        if (user == null) {
            Map<String, Object> res = new HashMap<>();
            res.put("code", Response.Status.BAD_REQUEST);
            return Response.status(Response.Status.BAD_REQUEST).entity(res).build();
        }
        if (!BCryptUtil.checkPassword(request.getPassword(), user.getPassword())) {
            Map<String, Object> res = new HashMap<>();
            res.put("code", Response.Status.BAD_REQUEST);
            res.put("msg", "wrong");
            return Response.status(Response.Status.BAD_REQUEST).entity(res).build();
        }
        String token = JwtUtil.generateToken(user.getId());
        Map<String, Object> res = new HashMap<>();
        res.put("code", Response.Status.OK);
        res.put("token", token);
        res.put("data", sanitizeUser(user));  //->:使用净化后的用户数据
        return Response.status(Response.Status.OK).entity(res).build();
    }

    @POST
    @Path("/register")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response register(User user) {
        user.setRole("user");
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            user.setRole("admin");
        }
        user.setPassword(BCryptUtil.hashPassword(user.getPassword()));
        userRepository.create(user);
        Map<String, Object> res = new HashMap<>();
        res.put("code", Response.Status.CREATED);
        return Response.status(Response.Status.CREATED).entity(res).build();
    }
}