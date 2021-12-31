package com.example.myblog.service;

import com.example.myblog.model.Post;
import com.example.myblog.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.DateUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;
import java.util.Optional;

@Service
public class PostService {

    @PersistenceContext
    EntityManager entityManager;
    
    @Autowired
    PostRepository postRepository;

    public List<Post> getPostsbyUserID(Integer id){
        Query query = entityManager.createNamedQuery("Post.findByUserId");
        query.setParameter("userId", id);
        return query.getResultList();
    }


    public Post findById(Integer id) {
        return postRepository.findById(id).get();
    }

    public List<Post> findAll() {
        return (List<Post>)postRepository.findAll();
    }

    public void save(Post post) {
        post.setCreateDate(DateUtils.createNow().getTime());
        postRepository.save(post);
    }
}
