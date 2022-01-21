package com.example.myblog.repository.dynamodb;

import com.example.myblog.AppConfig;
import com.example.myblog.model.dynamodb.Post;
import com.example.myblog.model.dynamodb.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.myblog.AppConfig.*;
import static com.example.myblog.AppConfig.DYNAMODB_SINGLE_TABLE_NAME;

@Repository
public class PostDynamoDbRepository {

    @Autowired
    private DynamoDbClient dynamoDbClient;

    @Autowired
    private DynamoDbEnhancedClient dynamoDbenhancedClient ;

    // Store the post item in the database
    public void save(final Post post) {
        HashMap<String, AttributeValue> itemValues = new HashMap<String,AttributeValue>();

        // Add all content to the table
        itemValues.put(DYNAMODB_PRIMARY_KEY_ATTRIBUTE_NAME, AttributeValue.builder().s(post.getUser().getId()+SPERATOR+post.getUser().getName()).build());
        itemValues.put(DYNAMODB_SORT_KEY_ATTRIBUTE_NAME, AttributeValue.builder().s(post.getId()).build());
        itemValues.put(Post.POST_TITLE_ATTRIBUTE_NAME, AttributeValue.builder().s(post.getTitle()).build());
        itemValues.put(Post.POST_TEXT_ATTRIBUTE_NAME, AttributeValue.builder().s(post.getText()).build());

        itemValues.put(Post.POST_IMAGE_ATTRIBUTE_NAME, AttributeValue.builder().b(SdkBytes.fromByteArray(post.getImage())).build());
        itemValues.put(Post.POST_DATE_ATTRIBUTE_NAME, AttributeValue.builder().s(post.getCreateDate()).build());

        PutItemRequest request = PutItemRequest.builder()
                .tableName(DYNAMODB_SINGLE_TABLE_NAME)
                .item(itemValues)
                .build();

        try {
            dynamoDbClient.putItem(request);
            System.out.println(DYNAMODB_SINGLE_TABLE_NAME +" was successfully updated, new post was added!");

        } catch (ResourceNotFoundException e) {
            System.err.format("Error: The Amazon DynamoDB table \"%s\" can't be found.\n", DYNAMODB_SINGLE_TABLE_NAME);
            System.err.println("Be sure that it exists and that you've typed its name correctly!");
        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
        }

    }


    public List<Post> findByUserId(String id) {
        List<Post> returnList = new ArrayList<Post>();
        // Filter by Post ID != data to get post data rows only
        String filterExpression = "begins_with(#788e0, :788e0) And #788e1 <> :788e1";

        // make a map of experssion attribute names
        Map<String, String> expressionAttributeNames = new HashMap<String, String>();
        expressionAttributeNames.put("#788e0", "userId");
        expressionAttributeNames.put("#788e1", "postId");

        // make a map of experssion attribute values
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<String, AttributeValue>();
        expressionAttributeValues.put(":788e0", AttributeValue.builder().s(id)
                .build());

        expressionAttributeValues.put(":788e1", AttributeValue.builder().s("data")
                .build());
        try{
            ScanRequest scanRequest = ScanRequest.builder()
                    .tableName(DYNAMODB_SINGLE_TABLE_NAME)
                  //  .indexName(DYNAMODB_GSI_POST_INDEX_NAME)
                    .filterExpression(filterExpression)
                    .expressionAttributeNames(expressionAttributeNames)
                    .expressionAttributeValues(expressionAttributeValues)
                    .build();

            ScanResponse response = dynamoDbClient.scan(scanRequest);
            List<Map<String, AttributeValue>> mapList = response.items();

            Post post;
            for(Map<String,AttributeValue> map : mapList){
                // create post object from each map of attributes
                post = new Post();
                post.setId(map.get(DYNAMODB_SORT_KEY_ATTRIBUTE_NAME).s());
                post.setTitle(map.get(Post.POST_TITLE_ATTRIBUTE_NAME).s());
                post.setImage(map.get(Post.POST_IMAGE_ATTRIBUTE_NAME).b().asByteArray());
                post.setText(map.get(Post.POST_TEXT_ATTRIBUTE_NAME).s());
                User user = new User(id, extractUserName(map.get(DYNAMODB_PRIMARY_KEY_ATTRIBUTE_NAME).s()));
                post.setUser(user);
                returnList.add(post);
            }
            return returnList;
        } catch(DynamoDbException e) {
            System.err.println(e.getMessage());
        }
        return returnList;
    }
    private String extractUserId(String s) {

        int i = s.indexOf(SPERATOR);

        return s.substring(0,i-1);
    }
   private String extractUserName(String s) {

       int i = s.indexOf(SPERATOR);

       return s.substring(i+1);
   }

   // Retrieve post items from the database by Id
    public List<Post> findById(String id) {
        List<Post> returnList = new ArrayList<Post>();
        Post post = new Post();
        User user = new User();
        String keyConditionExpression = "#788e0 = :788e0";
        String filterExpression = "NOT (begins_with(#788e1, :788e1))";
        // make a map of experssion attribute names
        Map<String, String> expressionAttributeNames = new HashMap<String, String>();
        expressionAttributeNames.put("#788e0", "postId");
        expressionAttributeNames.put("#788e1", "userId");

        // make a map of experssion attribute values
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<String, AttributeValue>();
        expressionAttributeValues.put(":788e0", AttributeValue.builder().s(id)
                .build());
        expressionAttributeValues.put(":788e1", AttributeValue.builder().s(DYNAMODB_COMMENT_ROW_PRIMARYKEY_VALUE)
                .build());

        QueryRequest queryRequest = QueryRequest.builder()
                .tableName(DYNAMODB_SINGLE_TABLE_NAME)
                .indexName(DYNAMODB_GSI_POST_INDEX_NAME)
                .keyConditionExpression(keyConditionExpression)
                .filterExpression(filterExpression)
                .scanIndexForward(true)
                .expressionAttributeNames(expressionAttributeNames)
                .expressionAttributeValues(expressionAttributeValues)
                .build();

        QueryResponse queryResponse = dynamoDbClient.query(queryRequest);
        Map<String, AttributeValue> map = queryResponse.items().get(0);

        // populate post object from map of attributes
        post.setId(map.get(DYNAMODB_SORT_KEY_ATTRIBUTE_NAME).s());
        post.setTitle(map.get(Post.POST_TITLE_ATTRIBUTE_NAME).s());
        post.setText(map.get(Post.POST_TEXT_ATTRIBUTE_NAME).s());
        post.setCreateDate(map.get(Post.POST_DATE_ATTRIBUTE_NAME).s());
        user = new User(extractUserId(map.get(DYNAMODB_PRIMARY_KEY_ATTRIBUTE_NAME).s()), extractUserName(map.get(DYNAMODB_PRIMARY_KEY_ATTRIBUTE_NAME).s()));
        post.setUser(user);

        returnList.add(post);
        return returnList;
    }

    public Object findAll() {
        return null;
    }

    public byte[] getPostImage(String postId) {

        String keyConditionExpression = "#ab0d0 = :ab0d0";
        // make a map of experssion attribute names
        Map<String, String> expressionAttributeNames = new HashMap<String, String>();
        expressionAttributeNames.put("#ab0d0", "postId");

        // make a map of experssion attribute values
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<String, AttributeValue>();
        expressionAttributeValues.put(":ab0d0", AttributeValue.builder().s(postId)
                .build());

        QueryRequest queryRequest = QueryRequest.builder()
                .tableName(DYNAMODB_SINGLE_TABLE_NAME)
                .indexName(DYNAMODB_GSI_IMAGE_INDEX_NAME)
                .keyConditionExpression(keyConditionExpression)
                .scanIndexForward(true)
                .expressionAttributeNames(expressionAttributeNames)
                .expressionAttributeValues(expressionAttributeValues)
                .build();
        System.out.println("Getting image request...");
        QueryResponse queryResponse = dynamoDbClient.query(queryRequest);
        List<Map<String, AttributeValue>> mapList = queryResponse.items();
        System.out.println("Returned list count = " + mapList.size());

        for(Map<String,AttributeValue> map : mapList){

            // create post object from each map of attributes
            if(map.get(DYNAMODB_PRIMARY_KEY_ATTRIBUTE_NAME).s().contains(SPERATOR)) {
                if(!map.get(DYNAMODB_PRIMARY_KEY_ATTRIBUTE_NAME).s().startsWith(DYNAMODB_COMMENT_ROW_PRIMARYKEY_VALUE))// not a comment row
                    if(map.get(Post.POST_IMAGE_ATTRIBUTE_NAME).b() != null)
                        return map.get(Post.POST_IMAGE_ATTRIBUTE_NAME).b().asByteArray();
            }
        }
        return null;
    }
}
