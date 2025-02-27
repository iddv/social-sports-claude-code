package com.socialsports.repository;

import com.socialsports.model.Community;
import com.socialsports.model.SportType;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class CommunityRepository {

    private final DynamoDbEnhancedClient dynamoDbEnhancedClient;
    private final DynamoDbTable<Community> communityTable;

    public CommunityRepository(DynamoDbEnhancedClient dynamoDbEnhancedClient) {
        this.dynamoDbEnhancedClient = dynamoDbEnhancedClient;
        this.communityTable = dynamoDbEnhancedClient.table("Community", TableSchema.fromBean(Community.class));
    }

    public Community save(Community community) {
        communityTable.putItem(community);
        return community;
    }

    public Optional<Community> findById(String id) {
        Key key = Key.builder().partitionValue(id).build();
        return Optional.ofNullable(communityTable.getItem(key));
    }
    
    public List<Community> findBySportType(SportType sportType) {
        // Note: In a real implementation, this would require a secondary index on supportedSports
        // For demo purposes, this simplified approach scans all items
        return communityTable.scan().items()
                .stream()
                .filter(community -> community.getSupportedSports().contains(sportType))
                .collect(Collectors.toList());
    }
    
    public List<Community> findByMember(String phoneNumber) {
        // Note: In a real implementation, this would require a secondary index on memberPhoneNumbers
        // For demo purposes, this simplified approach scans all items
        return communityTable.scan().items()
                .stream()
                .filter(community -> community.getMemberPhoneNumbers().contains(phoneNumber))
                .collect(Collectors.toList());
    }

    public void delete(Community community) {
        communityTable.deleteItem(community);
    }
}