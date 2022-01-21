package com.example.myblog.model.dynamodb;

import com.example.myblog.AppConfig;
import com.example.myblog.model.dynamodb.User;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

import javax.persistence.*;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

@DynamoDbBean
public class Post {

    public static final String POST_TITLE_ATTRIBUTE_NAME = "postTitle";
    public static final String POST_TEXT_ATTRIBUTE_NAME = "postText";
    public static final String POST_IMAGE_ATTRIBUTE_NAME = "postImage";
    public static final String POST_DATE_ATTRIBUTE_NAME = "postDate";


    private String id;

    private String title;

    private byte[] image;

    private String text;

    private User user;

    private String createDate;

    public Post() {
        this.id = UUID.randomUUID().toString();
    }

    @DynamoDbSortKey
    @DynamoDbAttribute(AppConfig.DYNAMODB_SORT_KEY_ATTRIBUTE_NAME)
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    @DynamoDbPartitionKey
    @DynamoDbAttribute(AppConfig.DYNAMODB_PRIMARY_KEY_ATTRIBUTE_NAME)
    public String getUserId(){
        if(user!=null){
            return user.getId();
        }else
            return null;
    }


    public Post(String postId) {
        this.id = postId;
    }

    @DynamoDbAttribute(POST_TITLE_ATTRIBUTE_NAME)
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @DynamoDbAttribute(POST_IMAGE_ATTRIBUTE_NAME)
    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    @DynamoDbAttribute(POST_TEXT_ATTRIBUTE_NAME)
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @DynamoDbAttribute(POST_DATE_ATTRIBUTE_NAME)
    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "Post{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", image=" + Arrays.toString(image) +
                ", text='" + text + '\'' +
                ", user=" + user +
                ", createDate='" + createDate + '\'' +
                '}';
    }
}
