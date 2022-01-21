package com.example.myblog;

import com.example.myblog.model.dynamodb.Post;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClientBuilder;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

@Configuration
public class AppConfig {


    // DYNAMODB TABLE
    public static final String DYNAMODB_SINGLE_TABLE_NAME = "Blog";
    public static final String DYNAMODB_PRIMARY_KEY_ATTRIBUTE_NAME = "userId";
    public static final String DYNAMODB_SORT_KEY_ATTRIBUTE_NAME = "postId";
    public static final String DYNAMODB_DATA_ROW_SORTKEY_VALUE = "data";
    public static final String DYNAMODB_COMMENT_ROW_PRIMARYKEY_VALUE = "comment";

    // GSI POST CONSTANTS
    public static final String DYNAMODB_GSI_POST_INDEX_NAME = "POST";

    // GSI USER CONSTANTS
    public static final String SPERATOR = "#";
    public static final String DYNAMODB_GSI_USER_INDEX_NAME = "USER";


    //GSI IMAGE CONSTANTS
    public static final String DYNAMODB_GSI_IMAGE_INDEX_NAME = "IMAGE";


    @Bean
    public DynamoDbClient getDynamoDBClient(){
        DynamoDbClientBuilder builder = DynamoDbClient.builder();
        AwsBasicCredentials awsBasicCredentials = AwsBasicCredentials.create("access_key_id", "secret_access_key");
        builder.credentialsProvider(StaticCredentialsProvider.create(awsBasicCredentials));
        try {
            builder.endpointOverride(new URL("http://localhost:8000").toURI());
        } catch(URISyntaxException e) {
            e.printStackTrace();
        } catch(MalformedURLException e) {
            e.printStackTrace();
        }

        return builder.build();
    }

    @Bean
    public DynamoDbEnhancedClient getDynamoDbEnhancedClient(@Autowired DynamoDbClient dynamoDbClient) {
        return DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
    }

    public static InputStream getFileFromResourceAsStream(String fileName) {
        // The class loader that loaded the class
        ClassLoader classLoader = AppConfig.class.getClassLoader();
        System.out.println("Classloader name = " + classLoader.toString());
        InputStream inputStream = classLoader.getResourceAsStream(fileName);

        // the stream holding the file content
        if (inputStream == null) {
            throw new IllegalArgumentException("file not found! " + fileName);
        } else {
            return inputStream;
        }
    }

}
