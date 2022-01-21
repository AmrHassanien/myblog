package com.example.myblog;

import com.example.myblog.model.dynamodb.Comment;
import com.example.myblog.model.dynamodb.Post;
import com.example.myblog.model.dynamodb.User;
import com.example.myblog.repository.dynamodb.CommentDynamoDbRepository;
import com.example.myblog.repository.dynamodb.PostDynamoDbRepository;
import com.example.myblog.repository.dynamodb.UserDynamoDbRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.thymeleaf.util.DateUtils;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.io.File;
import java.io.IOException;

import static com.example.myblog.AppConfig.*;
import static java.nio.file.Files.readAllBytes;

@Component
public class DynamoDbSetup implements CommandLineRunner {

    @Autowired
    private DynamoDbEnhancedClient DynamoDbEnhancedClient;

    @Autowired
    private DynamoDbClient dynamoDbClient;

    @Autowired
    private UserDynamoDbRepository userDynamoDbRepository;

    @Autowired
    private PostDynamoDbRepository postDynamoDbRepository;

    @Autowired
    private CommentDynamoDbRepository commentDynamoDbRepository;

    @Value("${dynamodb.table.create-on-start}")
    boolean createTables;
    //@Value("")

    @Override
    public void run(String... args) throws Exception {

        if(createTables) {
            // check if blog table exists
            if (tableExists()) {
                // if exists delete Blog table
                deleteTable();
            }
            // Create table with userId as primary key and postId as a sort key
            createTable();
            // add User to create attributes and use sort key value = data
            createAttributes();
            // Create GSI with UserId and postId and project (title and image) from Post attributes
            createGSIndex();
            // Create GSI with postId and commentID project all comment ID
            //
        }
    }

    private void createGSIndex() {

        // Create User Index
        createUserIndex();
        waitIndexCreation();


        // Create Post Index
        createPostIndex();
        waitIndexCreation();


        // Create Image Index
        createImageIndex();

    }

    /*
       This method, when called, will keep polling the indexes status untill they are all ACTIVE status
     */
    private void waitIndexCreation(){

        int attempts = 0;
        while (attempts < 5) {
            try {
                DescribeTableResponse describeTableResponse = dynamoDbClient.describeTable(b -> b.tableName(DYNAMODB_SINGLE_TABLE_NAME));
                boolean active = true;
                for(GlobalSecondaryIndexDescription index : describeTableResponse.table().globalSecondaryIndexes()){
                    IndexStatus status = index.indexStatus();
                    if (!status.equals(IndexStatus.ACTIVE)) {
                        active = false;
                    }
                }
                if (active) {
                    break;
                }
                Thread.sleep(1500);
                attempts++;
            } catch (ResourceNotFoundException e) {
                // continue to poll
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    private Post saveDummyPostObject(User user) {
        // create a Post
        Post post = new Post();
        post.setTitle("My First Post");
        post.setText("However, when this generated lambda is invoked, it crashes with the mentioned NoClassDefFoundError. This is related to the fact that the Lambda (somehow) tries to resolve the bean class with the classloader of the SDK. But since the bean class is loaded in a different classloader, the classloader of the SDK cannot find the bean class.\n" +
                "More specifically, the problem arises when the SDK classloader is either a parent of or entirely unrelated to the classloader of the bean.\n" +
                "\n" +
                "This issue is most likely related to issue #2198 since the error messages are identical. However that issue is specifically about usage with the play framework (which I do not know anything about). Furthermore, a usage of Class.forName is mentioned but I did not come across usage of that. Therefore I decided to create a more focused issue for the core problem I (somewhat) have a solution for.");

        byte[] image = new byte[0];
        try {
            File resource = new ClassPathResource("/static/images/blog.png").getFile();
            image = readAllBytes(resource.toPath());
            // AppConfig.getFileFromResourceAsStream("images/blog.png").read(image);
            post.setImage(image);

        } catch (IOException e) {
            e.printStackTrace();
        }
        post.setCreateDate(DateUtils.createNow().getTime().toString());
        post.setUser(user);
        postDynamoDbRepository.save(post);
        return post;
    }

    private void createImageIndex() {
        // Create POST index for table Blog
        UpdateTableRequest updateTableRequest = UpdateTableRequest.builder()
                .tableName(DYNAMODB_SINGLE_TABLE_NAME)
                .attributeDefinitions( // Define Primary key and sort key for the index
                        AttributeDefinition.builder()
                                .attributeName(DYNAMODB_SORT_KEY_ATTRIBUTE_NAME)
                                .attributeType(ScalarAttributeType.S)
                              /*  .build(),
                        AttributeDefinition.builder()
                                .attributeName(Post.POST_IMAGE_ATTRIBUTE_NAME)
                                .attributeType(ScalarAttributeType.B)*/
                                .build())
                .globalSecondaryIndexUpdates(GlobalSecondaryIndexUpdate.builder()
                        .create(CreateGlobalSecondaryIndexAction.builder()
                                .indexName(DYNAMODB_GSI_IMAGE_INDEX_NAME) // create Index named IMAGE
                                .keySchema(KeySchemaElement.builder()
                                                .attributeName(DYNAMODB_SORT_KEY_ATTRIBUTE_NAME)
                                                .keyType(KeyType.HASH)
                                               /* .build(),
                                            KeySchemaElement.builder()// post image attribute as sort key
                                                .attributeName(Post.POST_IMAGE_ATTRIBUTE_NAME)
                                                .keyType(KeyType.RANGE)*/
                                                .build())
                                .provisionedThroughput(
                                        ProvisionedThroughput.builder()
                                                .readCapacityUnits(new Long(10))
                                                .writeCapacityUnits(new Long(10))
                                                .build())
                                .projection(Projection.builder() // add attributes to POST INDEX
                                        .projectionType(ProjectionType.INCLUDE)
                                        .nonKeyAttributes(Post.POST_IMAGE_ATTRIBUTE_NAME)
                                        .build())
                                .build())
                        .build())
                .build();


        String indexStatus = "";

        try {
            // invoke client request
            UpdateTableResponse result = dynamoDbClient.updateTable(updateTableRequest);
            dynamoDbClient.waiter();
            indexStatus = result.tableDescription().tableStatusAsString();
            if (indexStatus != ""){
                System.out.println("Index Image created successfully! \n Index status : " + indexStatus);
            }
        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
        }
    }

    private void createPostIndex() {

        // Create POST index for table Blog
        UpdateTableRequest updateTableRequest = UpdateTableRequest.builder()
                .tableName(DYNAMODB_SINGLE_TABLE_NAME)
                .attributeDefinitions( // Define Primary key and sort key for the index
                        AttributeDefinition.builder()
                                .attributeName(DYNAMODB_SORT_KEY_ATTRIBUTE_NAME)
                                .attributeType(ScalarAttributeType.S)
                                .build())
                .globalSecondaryIndexUpdates(GlobalSecondaryIndexUpdate.builder()
                        .create(CreateGlobalSecondaryIndexAction.builder()
                                .indexName(DYNAMODB_GSI_POST_INDEX_NAME) // create Index named POST
                                .keySchema(
                                        KeySchemaElement.builder()
                                                .attributeName(DYNAMODB_SORT_KEY_ATTRIBUTE_NAME)
                                                .keyType(KeyType.HASH)
                                                .build())
                                .provisionedThroughput(
                                        ProvisionedThroughput.builder()
                                                .readCapacityUnits(new Long(10))
                                                .writeCapacityUnits(new Long(10))
                                                .build())
                                .projection(Projection.builder() // add attributes to POST INDEX
                                        .projectionType(ProjectionType.INCLUDE)
                                        .nonKeyAttributes(DYNAMODB_PRIMARY_KEY_ATTRIBUTE_NAME, Post.POST_DATE_ATTRIBUTE_NAME,Post.POST_TITLE_ATTRIBUTE_NAME,Post.POST_TEXT_ATTRIBUTE_NAME)
                                        .build())
                                .build())
                        .build())
                .build();


        String indexStatus = "";

        try {
            // invoke client request
            UpdateTableResponse result = dynamoDbClient.updateTable(updateTableRequest);
            indexStatus = result.tableDescription().tableStatusAsString();
            if (indexStatus != ""){
                System.out.println("Index Post created successfully! \n Index status : " + indexStatus);
            }
        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
        }
    }

    private void createUserIndex() {
        UpdateTableRequest updateTableRequest = UpdateTableRequest.builder()
                .tableName(DYNAMODB_SINGLE_TABLE_NAME)
                .attributeDefinitions(
                        AttributeDefinition.builder()
                                .attributeName(DYNAMODB_PRIMARY_KEY_ATTRIBUTE_NAME)
                                .attributeType(ScalarAttributeType.S)
                                .build())
                .globalSecondaryIndexUpdates(GlobalSecondaryIndexUpdate.builder()
                        .create(CreateGlobalSecondaryIndexAction.builder()
                                .indexName(DYNAMODB_GSI_USER_INDEX_NAME)
                                .keySchema(KeySchemaElement.builder()
                                                .attributeName(DYNAMODB_PRIMARY_KEY_ATTRIBUTE_NAME)
                                                .keyType(KeyType.HASH)
                                                .build())
                                .provisionedThroughput(
                                        ProvisionedThroughput.builder()
                                                .readCapacityUnits(new Long(10))
                                                .writeCapacityUnits(new Long(10))
                                                .build())
                                .projection(Projection.builder()
                                        .projectionType(ProjectionType.INCLUDE)
                                        .nonKeyAttributes(User.USER_NAME_ATTRIBUTE_NAME, User.USER_EMAIL_ATTRIBUTE_NAME,User.USER_GENDER_ATTRIBUTE_NAME, User.USER_PROFESSION_ATTRIBUTE_NAME, User.USER_BIRTHDAY_ATTRIBUTE_NAME)
                                        .build())
                                .build())
                        .build())
                .build();


        String indexStatus = "";

        try {
            UpdateTableResponse result = dynamoDbClient.updateTable(updateTableRequest);
            indexStatus = result.tableDescription().tableStatusAsString();
            if (indexStatus != ""){
                System.out.println("Index USER created successfully! \n Index status : " + indexStatus);
            }
        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
        }
    }

    /*
     Add dummy rows to create attributes
     */
    private void createAttributes() {
        // Create a user
        User user = new User();
        user.setName("Amr");
        user.setEmail("amr@c3s.co");
        user.setGender("male");
        user.setPassword("groupit");
        user.setProfession("Developer");
        user.setBirthday("1984/7/1");
        user.setId("U-001");
        userDynamoDbRepository.save(user);
        Post post = saveDummyPostObject(user);
        //create a comment
        Comment comment = new Comment();
        comment.setPost(post);
        comment.setTitle("Hello");
        comment.setText("the first comment");
        comment.setName("Must live");
        comment.setCreateDate(DateUtils.createNow().getTime().toString());
        commentDynamoDbRepository.save(comment);

        // Create a user
        user = new User();
        user.setName("Zambrotta");
        user.setEmail("Zam@rotta.co");
        user.setGender("male");
        user.setPassword("changeit");
        user.setProfession("Developer");
        user.setBirthday("1989/2/1");
        user.setId("U-002");
        userDynamoDbRepository.save(user);

        // Create a user
        user = new User();
        user.setName("Zanetti");
        user.setEmail("keoks@rotta.co");
        user.setGender("female");
        user.setPassword("changeit");
        user.setProfession("Developer");
        user.setBirthday("1999/2/1");
        user.setId("U-003");
        userDynamoDbRepository.save(user);


    }
    /*
     Creates the table and primary key without attributes
     */
    private void createTable() {

        System.out.println("Creating Table Blog...");
        CreateTableRequest request = CreateTableRequest.builder()
                .attributeDefinitions(
                        AttributeDefinition.builder()
                                .attributeName(DYNAMODB_PRIMARY_KEY_ATTRIBUTE_NAME)
                                .attributeType(ScalarAttributeType.S)
                                .build(),
                        AttributeDefinition.builder()
                                .attributeName(DYNAMODB_SORT_KEY_ATTRIBUTE_NAME)
                                .attributeType(ScalarAttributeType.S)
                                .build())
                .keySchema(
                        KeySchemaElement.builder()
                                .attributeName(DYNAMODB_PRIMARY_KEY_ATTRIBUTE_NAME)
                                .keyType(KeyType.HASH)
                                .build(),
                        KeySchemaElement.builder()
                                .attributeName(DYNAMODB_SORT_KEY_ATTRIBUTE_NAME)
                                .keyType(KeyType.RANGE)
                                .build())
                .provisionedThroughput(
                        ProvisionedThroughput.builder()
                                .readCapacityUnits(new Long(10))
                                .writeCapacityUnits(new Long(10)).build())
                .tableName(DYNAMODB_SINGLE_TABLE_NAME)
                .build();

        TableStatus tableId = null;

        try {
            CreateTableResponse result = dynamoDbClient.createTable(request);
            tableId = result.tableDescription().tableStatus();
            if (tableId!=null){
                System.out.println("Table Blog create successfully! \nTable Status : " + result.tableDescription().tableStatusAsString());
            }
        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
        }
    }

    /*
        Delete the application DynamoDb Table named Blog
     */
    private void deleteTable() {

        System.out.println("Deleting Table Blog..");
        DeleteTableRequest request = DeleteTableRequest.builder()
                .tableName(DYNAMODB_SINGLE_TABLE_NAME)
                .build();

        try {
            dynamoDbClient.deleteTable(request);

        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
        }
        System.out.println(DYNAMODB_SINGLE_TABLE_NAME +" was successfully deleted!");
    }

    /*
     Use describeTable method in DynamoDbClient to check whether the app table exists
     */
    private boolean tableExists() {

        System.out.println("Check if table Blog exists..");
        DescribeTableRequest request = DescribeTableRequest.builder()
                .tableName(DYNAMODB_SINGLE_TABLE_NAME)
                .build();
        try {
            TableDescription tableInfo =
                    dynamoDbClient.describeTable(request).table();

            if (tableInfo != null) {
                System.out.println("Table Blog already exists");
                return true;
            }

        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
        }
        return false;
    }
}
