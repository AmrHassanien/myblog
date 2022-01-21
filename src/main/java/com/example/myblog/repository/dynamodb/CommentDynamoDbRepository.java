package com.example.myblog.repository.dynamodb;

import com.example.myblog.model.dynamodb.Comment;
import com.example.myblog.model.dynamodb.Post;
import com.example.myblog.model.dynamodb.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.myblog.AppConfig.*;
import static com.example.myblog.AppConfig.DYNAMODB_SINGLE_TABLE_NAME;

@Repository
public class CommentDynamoDbRepository {


    @Autowired
    private DynamoDbEnhancedClient dynamoDbenhancedClient;

    @Autowired
    private DynamoDbClient dynamoDbClient;

    // Store the user item in the database
    public void save(final Comment comment) {
        HashMap<String, AttributeValue> itemValues = new HashMap<String, AttributeValue>();

        // Add all content to the table
        itemValues.put(DYNAMODB_PRIMARY_KEY_ATTRIBUTE_NAME, AttributeValue.builder().s(comment.getUserId()).build());
        itemValues.put(Comment.COMMENT_ID_ATTRIBUTE_NAME, AttributeValue.builder().s(comment.getId()).build());
        itemValues.put(DYNAMODB_SORT_KEY_ATTRIBUTE_NAME, AttributeValue.builder().s(comment.getPostId()).build());
        itemValues.put(Comment.COMMENT_NAME_ATTRIBUTE_NAME, AttributeValue.builder().s(comment.getName()).build());
        itemValues.put(Comment.COMMENT_TITLE_ATTRIBUTE_NAME, AttributeValue.builder().s(comment.getTitle()).build());
        itemValues.put(Comment.COMMENT_CREATEDATE_ATTRIBUTE_NAME, AttributeValue.builder().s(comment.getCreateDate()).build());
        itemValues.put(Comment.COMMENT_TEXT_ATTRIBUTE_NAME, AttributeValue.builder().s(comment.getText()).build());


        PutItemRequest request = PutItemRequest.builder()
                .tableName(DYNAMODB_SINGLE_TABLE_NAME)
                .item(itemValues)
                .build();

        try {
            dynamoDbClient.putItem(request);
            System.out.println(DYNAMODB_SINGLE_TABLE_NAME + " was successfully updated, new comment row was added!");

        } catch (ResourceNotFoundException e) {
            System.err.format("Error: The Amazon DynamoDB table \"%s\" can't be found.\n", DYNAMODB_SINGLE_TABLE_NAME);
            System.err.println("Be sure that it exists and that you've typed its name correctly!");
        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
        }
    }

    public List<Comment> FindByPostId(String postId) {

        List<Comment> returnList = new ArrayList<Comment>();
        // Filter by userID = comment to get post data rows only
        String filterExpression = "#788e0 = :788e0 And begins_with(#788e1, :788e1)";

        // make a map of experssion attribute names
        Map<String, String> expressionAttributeNames = new HashMap<String, String>();
        expressionAttributeNames.put("#788e0", "postId");
        expressionAttributeNames.put("#788e1", "userId");

        // make a map of experssion attribute values
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<String, AttributeValue>();
        expressionAttributeValues.put(":788e0", AttributeValue.builder().s(postId)
                .build());

        expressionAttributeValues.put(":788e1", AttributeValue.builder().s(DYNAMODB_COMMENT_ROW_PRIMARYKEY_VALUE)
                .build());
        try{
            ScanRequest scanRequest = ScanRequest.builder()
                    .tableName(DYNAMODB_SINGLE_TABLE_NAME)
                    .filterExpression(filterExpression)
                    .expressionAttributeNames(expressionAttributeNames)
                    .expressionAttributeValues(expressionAttributeValues)
                    .build();

            ScanResponse response = dynamoDbClient.scan(scanRequest);
            List<Map<String, AttributeValue>> mapList = response.items();

            Comment comment;
            for(Map<String,AttributeValue> map : mapList){
                // create post object from each map of attributes
                comment = new Comment();
                comment.setId(map.get(Comment.COMMENT_ID_ATTRIBUTE_NAME).s());
                comment.setTitle(map.get(Comment.COMMENT_TITLE_ATTRIBUTE_NAME).s());
                comment.setText(map.get(Comment.COMMENT_TEXT_ATTRIBUTE_NAME).s());
                comment.setName(map.get(Comment.COMMENT_NAME_ATTRIBUTE_NAME).s());
                comment.setCreateDate(map.get(Comment.COMMENT_CREATEDATE_ATTRIBUTE_NAME).s());
                returnList.add(comment);
            }
            return returnList;
        } catch(DynamoDbException e) {
            System.err.println(e.getMessage());
        }
        return returnList;

    }

}
