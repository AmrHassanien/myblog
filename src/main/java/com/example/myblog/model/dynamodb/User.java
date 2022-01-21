package com.example.myblog.model.dynamodb;

import com.example.myblog.AppConfig;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

import java.util.UUID;

import static com.example.myblog.AppConfig.DYNAMODB_DATA_ROW_SORTKEY_VALUE;

@DynamoDbBean
public class User {

    public static final String USER_NAME_ATTRIBUTE_NAME = "userName";

    public static final String USER_EMAIL_ATTRIBUTE_NAME = "userEmail";

    public static final String USER_PASSWORD_ATTRIBUTE_NAME = "userPassword";

    public static final String USER_GENDER_ATTRIBUTE_NAME = "userGender";

    public static final String USER_PROFESSION_ATTRIBUTE_NAME = "userProfession";

    public static final String USER_BIRTHDAY_ATTRIBUTE_NAME = "userBirthday";

    private String id;

    private String postId;

    private String name;

    private String email;

    private String password;

    private String gender;

    private String profession;
    private String birthday;

  //  @OneToMany(mappedBy="user")
   // private List<Post> posts;

    public User() {
        this.postId= DYNAMODB_DATA_ROW_SORTKEY_VALUE;
        this.id = UUID.randomUUID().toString();
    }

    public User(String id){
        this();
        this.id = id;
    }
    public User(String id, String name, String email, String gender) {
        this();
        this.id = id;
        this.name = name;
        this.email = email;
        this.gender = gender;
    }

    public User(String id, String name) {
        this();
        this.id = id;
        this.name = name;
    }

    @DynamoDbPartitionKey
    @DynamoDbAttribute(AppConfig.DYNAMODB_PRIMARY_KEY_ATTRIBUTE_NAME)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @DynamoDbSortKey
    @DynamoDbAttribute(AppConfig.DYNAMODB_SORT_KEY_ATTRIBUTE_NAME)
    public String getPostId() {
        return DYNAMODB_DATA_ROW_SORTKEY_VALUE;
    }

    @DynamoDbAttribute(USER_NAME_ATTRIBUTE_NAME)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @DynamoDbAttribute(USER_EMAIL_ATTRIBUTE_NAME)
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @DynamoDbAttribute(USER_GENDER_ATTRIBUTE_NAME)
    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

   /* public List<Post> getPosts() {
        return posts;
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
    } */

    @DynamoDbAttribute(USER_PASSWORD_ATTRIBUTE_NAME)
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @DynamoDbAttribute(USER_PROFESSION_ATTRIBUTE_NAME)
    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    @DynamoDbAttribute(USER_BIRTHDAY_ATTRIBUTE_NAME)
    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;

        User user = (User) o;

        if (!getId().equals(user.getId())) return false;
        if (!getPostId().equals(user.getPostId())) return false;
        if (getName() != null ? !getName().equals(user.getName()) : user.getName() != null) return false;
        if (getEmail() != null ? !getEmail().equals(user.getEmail()) : user.getEmail() != null) return false;
        if (getPassword() != null ? !getPassword().equals(user.getPassword()) : user.getPassword() != null)
            return false;
        if (getGender() != null ? !getGender().equals(user.getGender()) : user.getGender() != null) return false;
        if (getProfession() != null ? !getProfession().equals(user.getProfession()) : user.getProfession() != null)
            return false;
        return getBirthday() != null ? getBirthday().equals(user.getBirthday()) : user.getBirthday() == null;
    }

    @Override
    public int hashCode() {
        int result = getId().hashCode();
        result = 31 * result + getPostId().hashCode();
        result = 31 * result + (getName() != null ? getName().hashCode() : 0);
        result = 31 * result + (getEmail() != null ? getEmail().hashCode() : 0);
        result = 31 * result + (getPassword() != null ? getPassword().hashCode() : 0);
        result = 31 * result + (getGender() != null ? getGender().hashCode() : 0);
        result = 31 * result + (getProfession() != null ? getProfession().hashCode() : 0);
        result = 31 * result + (getBirthday() != null ? getBirthday().hashCode() : 0);
        return result;
    }
}
