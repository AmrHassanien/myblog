package com.example.myblog.controller;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.example.myblog.model.Comment;
import com.example.myblog.model.Post;
import com.example.myblog.model.User;
import com.example.myblog.repository.PostRepository;
import com.example.myblog.service.CommentService;
import com.example.myblog.service.PostService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/*
    This controller is responsible to handle all requests related to posts and is mapped to /post url
 */
@Controller
@RequestMapping(path="/post")
public class PostController {

    @Autowired
    private PostService postService;

    @Autowired
    private CommentService commentService;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /*
      show all posts for a certain user
     */
    @GetMapping("/showAllByUser/{id}")
    public String showAllByUser(@PathVariable("id") Integer id, Model model, HttpServletRequest request) {
        List<Post> posts = postService.getPostsbyUserID(id);
        model.addAttribute("posts", posts);
        model.addAttribute("user_id",id);
        return "allUserPosts";
    }

    /*
        open create a post page for the specified user
     */
    @GetMapping("/addpost/{userId}")
    public String addPost(@PathVariable("userId") Integer userId, Model model, HttpServletRequest request) {
        Post post = new Post();
        User user = new User();
        user.setId(userId);
        post.setUser(user);
        model.addAttribute("post", post);
        return "addPost";
    }

    /*
      saves a new post for a specific user
     */
    @PostMapping("/savepost/{userId}")
    public String savePost(@PathVariable("userId") Integer userId, @RequestParam("title") String title, @RequestParam("textbody") String text, Model model, HttpServletRequest request,final @RequestParam("image") MultipartFile file) {
        Post post = new Post();
        post.setTitle(title);
        post.setText(text);
        post.setUser(new User(userId));
        try {
            post.setImage(file.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.postService.save(post);
          model.addAttribute("success_message", "New post has been saved successfully.");
          return "user_message";
    }

    /*
     Display an image when mapped with a src attribute of img tag
     */
    @GetMapping(value = "/image/{image_id}")
    @ResponseBody
    public void getImage(@PathVariable("image_id") Integer imageId, HttpServletResponse response) throws IOException {
        byte[] imageContent = postService.findById(imageId).getImage();
        response.setContentType("image/jpeg, image/jpg, image/png, image/gif");
        response.getOutputStream().write(imageContent);
        response.getOutputStream().close();
    }
    /*
     returns a page to display the full content of a post and its comments
     */
    @GetMapping("/view/{postId}")
    public String viewPost(@PathVariable("postId") Integer postId, Model model, HttpServletRequest request) {
        Post post = postService.findById(postId);
        List<Comment> comments = commentService.getAllComments(postId);
        model.addAttribute("post", post);
        model.addAttribute("comment", new Comment());
        model.addAttribute("comments", comments);
        return "viewPost";
    }

    /*
     saves a new post for a specific user
    */
    @PostMapping("/addComment/{postId}")
    public String saveComment(@PathVariable("postId") Integer postId, @ModelAttribute("comment") Comment comment, Model model, HttpServletRequest request) {
        comment.setPost(new Post(postId));
        commentService.save(comment);
        model.addAttribute("success_message", "New comment has been added successfully.");
        return "user_message";
    }

}