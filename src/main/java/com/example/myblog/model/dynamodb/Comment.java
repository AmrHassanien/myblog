package com.example.myblog.model.dynamodb;

import com.example.myblog.AppConfig;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@DynamoDbBean
public class Comment {

    public static final String COMMENT_TITLE_ATTRIBUTE_NAME = "commentTitle";
    public static final String COMMENT_TEXT_ATTRIBUTE_NAME = "commentText";
    public static final String COMMENT_ID_ATTRIBUTE_NAME = "commentId";
    private static final String DYNAMODB_COMMENT_ROW_PRIMARYKEY_VALUE = "comment";
    public static String COMMENT_NAME_ATTRIBUTE_NAME = "commentName";
    public static String COMMENT_CREATEDATE_ATTRIBUTE_NAME = "commentCreateDate";

    private String id;

    private String name;

    private String title;

    private String text;

    private String createDate;

    private Post post;

    public Comment(){
        this.id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public String getPostId() {
        if(post!= null){
           return post.getId();
        }
        return "";
    }
    public String getUserId(){
        return DYNAMODB_COMMENT_ROW_PRIMARYKEY_VALUE+ AppConfig.SPERATOR + this.getId();
    }
}
