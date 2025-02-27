package com.socialsports.repository;

import com.socialsports.model.User;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.util.Optional;

@Repository
public class UserRepository {

    private final DynamoDbEnhancedClient dynamoDbEnhancedClient;
    private final DynamoDbTable<User> userTable;

    public UserRepository(DynamoDbEnhancedClient dynamoDbEnhancedClient) {
        this.dynamoDbEnhancedClient = dynamoDbEnhancedClient;
        this.userTable = dynamoDbEnhancedClient.table("User", TableSchema.fromBean(User.class));
    }

    public User save(User user) {
        userTable.putItem(user);
        return user;
    }

    public Optional<User> findByPhoneNumber(String phoneNumber) {
        Key key = Key.builder().partitionValue(phoneNumber).build();
        return Optional.ofNullable(userTable.getItem(key));
    }

    public void delete(User user) {
        userTable.deleteItem(user);
    }
}