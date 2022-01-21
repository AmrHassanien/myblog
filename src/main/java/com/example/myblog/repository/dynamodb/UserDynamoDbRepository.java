package com.example.myblog.repository.dynamodb;

import com.example.myblog.model.dynamodb.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.*;

import static com.example.myblog.AppConfig.*;


@Repository
public class UserDynamoDbRepository {

    @Autowired
    private DynamoDbEnhancedClient dynamoDbenhancedClient;

    @Autowired
    private DynamoDbClient dynamoDbClient;

    // Store the user item in the database
    public void save(final User user) {
        HashMap<String, AttributeValue> itemValues = new HashMap<String, AttributeValue>();

        // Add all content to the table
        itemValues.put(DYNAMODB_PRIMARY_KEY_ATTRIBUTE_NAME, AttributeValue.builder().s(user.getId()).build());
        itemValues.put(DYNAMODB_SORT_KEY_ATTRIBUTE_NAME, AttributeValue.builder().s(user.getPostId()).build());
        itemValues.put(User.USER_NAME_ATTRIBUTE_NAME, AttributeValue.builder().s(user.getName()).build());
        itemValues.put(User.USER_EMAIL_ATTRIBUTE_NAME, AttributeValue.builder().s(user.getEmail()).build());
        itemValues.put(User.USER_PASSWORD_ATTRIBUTE_NAME, AttributeValue.builder().s(user.getPassword()).build());
        itemValues.put(User.USER_GENDER_ATTRIBUTE_NAME, AttributeValue.builder().s(user.getGender()).build());
        itemValues.put(User.USER_PROFESSION_ATTRIBUTE_NAME, AttributeValue.builder().s(user.getProfession()).build());
        itemValues.put(User.USER_BIRTHDAY_ATTRIBUTE_NAME, AttributeValue.builder().s(user.getBirthday()).build());

        PutItemRequest request = PutItemRequest.builder()
                .tableName(DYNAMODB_SINGLE_TABLE_NAME)
                .item(itemValues)
                .build();

        try {
            dynamoDbClient.putItem(request);
            System.out.println(DYNAMODB_SINGLE_TABLE_NAME + " was successfully updated");

        } catch (ResourceNotFoundException e) {
            System.err.format("Error: The Amazon DynamoDB table \"%s\" can't be found.\n", DYNAMODB_SINGLE_TABLE_NAME);
            System.err.println("Be sure that it exists and that you've typed its name correctly!");
        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
        }

    }

    // Retrieve a single user item from the database
    public User getUser(final String userID, final String postID) {
        DynamoDbTable<User> userTable = getTable();
        // Construct the key with partition and sort key
        Key key = Key.builder().partitionValue(userID)
                .sortValue(postID)
                .build();

        User user = userTable.getItem(key);
        return user;
    }


    private DynamoDbTable<User> getTable() {
        // Create a tablescheme to scan our bean class User
        DynamoDbTable<User> userTable =
                dynamoDbenhancedClient.table(DYNAMODB_SINGLE_TABLE_NAME,
                        TableSchema.fromBean(User.class));
        return userTable;
    }

    public List<User> findAll() {
        List<User> returnList = new ArrayList<User>();
        // Filter by Post ID = data to get user data rows only
        String filterExpression = "#ab0d0 = :ab0d0";

        // make a map of experssion attribute names
        Map<String, String> expressionAttributeNames = new HashMap<String, String>();
        expressionAttributeNames.put("#ab0d0", "postId");

        // make a map of experssion attribute values
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<String, AttributeValue>();
        expressionAttributeValues.put(":ab0d0", AttributeValue.builder().s("data")
                                                                        .build());
        try{
            ScanRequest scanRequest = ScanRequest.builder()
                    .tableName(DYNAMODB_SINGLE_TABLE_NAME)
                    .indexName(DYNAMODB_GSI_USER_INDEX_NAME)
                    .filterExpression(filterExpression)
                     .expressionAttributeNames(expressionAttributeNames)
                    .expressionAttributeValues(expressionAttributeValues)
                    .build();

            ScanResponse response = dynamoDbClient.scan(scanRequest);
            List<Map<String, AttributeValue>> mapList = response.items();

            User user;
            returnList = new ArrayList<User>();
            for(Map<String,AttributeValue> map : mapList){
                // create user object from each map of attributes
                user = new User();
                user.setId(map.get(DYNAMODB_PRIMARY_KEY_ATTRIBUTE_NAME).s());
                user.setName(map.get(User.USER_NAME_ATTRIBUTE_NAME).s());
                user.setEmail(map.get(User.USER_EMAIL_ATTRIBUTE_NAME).s());
                user.setGender(map.get(User.USER_GENDER_ATTRIBUTE_NAME).s());
                user.setProfession(map.get(User.USER_PROFESSION_ATTRIBUTE_NAME).s());
                user.setBirthday(map.get(User.USER_BIRTHDAY_ATTRIBUTE_NAME).s());
                returnList.add(user);
            }
            return returnList;
        } catch(DynamoDbException e) {
            System.err.println(e.getMessage());
        }
        return returnList;
    }

    public List<User> findById(String userId) {

        List<User> returnList = new ArrayList<User>();
        User user = new User();
        // Prepare search key
        HashMap<String,AttributeValue> keyToGet = new HashMap<String,AttributeValue>();

        keyToGet.put(DYNAMODB_PRIMARY_KEY_ATTRIBUTE_NAME, AttributeValue.builder()
                .s(userId).build());
        keyToGet.put(DYNAMODB_SORT_KEY_ATTRIBUTE_NAME, AttributeValue.builder()
                .s(DYNAMODB_DATA_ROW_SORTKEY_VALUE).build());
        // make new get item request
        GetItemRequest request = GetItemRequest.builder()
                .key(keyToGet)
                .tableName(DYNAMODB_SINGLE_TABLE_NAME)
                .attributesToGet(DYNAMODB_PRIMARY_KEY_ATTRIBUTE_NAME,
                        User.USER_NAME_ATTRIBUTE_NAME,User.USER_EMAIL_ATTRIBUTE_NAME,
                        User.USER_GENDER_ATTRIBUTE_NAME,User.USER_BIRTHDAY_ATTRIBUTE_NAME,
                        User.USER_PROFESSION_ATTRIBUTE_NAME)
                .build();

        try {
            // execute the request
            Map<String,AttributeValue> returnedItem = dynamoDbClient.getItem(request).item();

            if (returnedItem != null) {
                user.setId(returnedItem.get(DYNAMODB_PRIMARY_KEY_ATTRIBUTE_NAME).s());
                user.setName(returnedItem.get(User.USER_NAME_ATTRIBUTE_NAME).s());
                user.setEmail(returnedItem.get(User.USER_EMAIL_ATTRIBUTE_NAME).s());
                user.setGender(returnedItem.get(User.USER_GENDER_ATTRIBUTE_NAME).s());
                user.setProfession(returnedItem.get(User.USER_PROFESSION_ATTRIBUTE_NAME).s());
                user.setBirthday(returnedItem.get(User.USER_BIRTHDAY_ATTRIBUTE_NAME).s());
                returnList.add(user);
            } else {
                System.out.format("No item found with the key %s!\n", userId);
            }
            return returnList;
        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        return returnList;
    }
}
