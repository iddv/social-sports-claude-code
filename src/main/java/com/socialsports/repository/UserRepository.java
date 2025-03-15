package com.socialsports.repository;

import com.socialsports.model.User;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public Optional<User> findById(String userId) {
        Key key = Key.builder().partitionValue(userId).build();
        return Optional.ofNullable(userTable.getItem(key));
    }

    public Optional<User> findByPhoneNumber(String phoneNumber) {
        QueryConditional queryConditional = QueryConditional
                .keyEqualTo(Key.builder().partitionValue(phoneNumber).build());
        
        QueryEnhancedRequest request = QueryEnhancedRequest.builder()
                .queryConditional(queryConditional)
                .build();
                
        // Query the GSI
        return userTable.index("phoneNumber-index")
                .query(request)
                .stream()
                .flatMap(page -> page.items().stream())
                .findFirst();
    }
    
    public Optional<User> findByEmail(String email) {
        if (email == null || email.isEmpty()) {
            return Optional.empty();
        }
        
        QueryConditional queryConditional = QueryConditional
                .keyEqualTo(Key.builder().partitionValue(email).build());
        
        QueryEnhancedRequest request = QueryEnhancedRequest.builder()
                .queryConditional(queryConditional)
                .build();
                
        // Query the GSI
        return userTable.index("email-index")
                .query(request)
                .stream()
                .flatMap(page -> page.items().stream())
                .findFirst();
    }

    public void delete(User user) {
        userTable.deleteItem(user);
    }
    
    /**
     * Find all users in the database.
     * 
     * @return A list of all users
     */
    public List<User> findAll() {
        ScanEnhancedRequest scanRequest = ScanEnhancedRequest.builder().build();
        return userTable.scan(scanRequest)
                .items()
                .stream()
                .collect(Collectors.toList());
    }
}