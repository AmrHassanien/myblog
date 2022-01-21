package com.example.myblog.service;

import com.example.myblog.controller.PostController;
import com.example.myblog.model.dynamodb.Post;
import com.example.myblog.repository.dynamodb.PostDynamoDbRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.DateUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Service
public class PostService {
    
    @Autowired
    PostDynamoDbRepository postRepository;

  //  public List<Post> getPostsbyUserID(Integer id){
    //    Query query = postRepository.createNamedQuery("Post.findByUserId");
      //  query.setParameter("userId", id);
        //return query.getResultList();
   // }


    public Post findById(String id) {
       return postRepository.findById(id).get(0);
    }

    public List<Post> findAll() {
        return (List<Post>) postRepository.findAll();
    }

    public void save(Post post) {
        post.setCreateDate(DateUtils.createNow().getTime().toString());
        postRepository.save(post);
    }


    public List<Post> getPostsbyUserID(String id) {
        return postRepository.findByUserId(id);
    }

    public byte[] getPostImage(String postId) {
        return postRepository.getPostImage(postId);
    }
}
