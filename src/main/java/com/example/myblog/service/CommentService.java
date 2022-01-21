package com.example.myblog.service;

import com.example.myblog.model.dynamodb.Comment;
import com.example.myblog.repository.dynamodb.CommentDynamoDbRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.DateUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Service
public class CommentService {

    @Autowired
    CommentDynamoDbRepository commentRepository;

    public List<Comment> getAllComments(String postId) {

        return commentRepository.FindByPostId(postId);
    }

    public void save(Comment comment) {
        comment.setCreateDate(DateUtils.createNow().getTime().toString());
        commentRepository.save(comment);
    }
   /* @PersistenceContext
    EntityManager entityManager;
    


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

    */
}
