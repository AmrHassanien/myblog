package com.example.myblog.repository;


import com.example.myblog.model.Comment;
import org.springframework.data.repository.CrudRepository;


// This will be AUTO IMPLEMENTED by Spring into a Bean called userRepository
// CRUD refers Create, Read, Update, Delete

public interface CommentRepository extends CrudRepository<Comment, Integer> {

}