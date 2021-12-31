package com.example.myblog.service;

import com.example.myblog.model.Comment;
import com.example.myblog.model.Post;
import com.example.myblog.repository.CommentRepository;
import com.example.myblog.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.DateUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Service
public class CommentService {

    @PersistenceContext
    EntityManager entityManager;
    
    @Autowired
    CommentRepository commentRepository;

    public List<Comment> getAllComments(Integer postId){
        Query query = entityManager.createNamedQuery("Comment.findByPostId");
        query.setParameter("postId", postId);
        return query.getResultList();
    }


    public Comment findById(Integer id) {
        return commentRepository.findById(id).get();
    }

    public List<Comment> findAll() {
        return (List<Comment>)commentRepository.findAll();
    }

    public void save(Comment comment) {
        comment.setCreateDate(DateUtils.createNow().getTime());
        commentRepository.save(comment);
    }
}
