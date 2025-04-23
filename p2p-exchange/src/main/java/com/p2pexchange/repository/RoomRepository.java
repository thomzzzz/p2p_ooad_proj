package com.p2pexchange.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.p2pexchange.model.Room;

/**
 * Repository for room data access operations.
 */
public interface RoomRepository extends MongoRepository<Room, String> {
    
    /**
     * Find rooms by owner ID.
     * 
     * @param ownerId The owner's ID
     * @return List of rooms owned by the user
     */
    List<Room> findByOwnerId(String ownerId);
    
    /**
     * Find rooms where a user is a member.
     * 
     * @param userId The user's ID
     * @return List of rooms where the user is a member
     */
    List<Room> findByMembersContaining(String userId);
    
    /**
     * Find rooms where a user is a creator.
     * 
     * @param userId The user's ID
     * @return List of rooms where the user is a creator
     */
    List<Room> findByCreatorsContaining(String userId);
    
    /**
     * Find rooms by name containing a query string.
     * 
     * @param name Name query string (case-insensitive)
     * @return List of rooms matching the criteria
     */
    @Query("{ 'name': { $regex: ?0, $options: 'i' } }")
    List<Room> findByNameContaining(String name);
    
    /**
     * Find rooms by access level.
     * 
     * @param accessLevel The access level to search for
     * @return List of rooms with matching access level
     */
    List<Room> findByAccessLevel(String accessLevel);
    
    /**
     * Find rooms containing a specific file.
     * 
     * @param fileId The file's ID
     * @return List of rooms containing the file
     */
    List<Room> findBySharedFilesContaining(String fileId);
    
    /**
     * Find public rooms.
     * 
     * @return List of public rooms
     */
    @Query("{ 'accessLevel': 'PUBLIC' }")
    List<Room> findPublicRooms();
    
    /**
     * Count rooms by owner ID.
     * 
     * @param ownerId The owner's ID
     * @return Count of rooms owned by the user
     */
    long countByOwnerId(String ownerId);
    
    /**
     * Count rooms where a user is a member.
     * 
     * @param userId The user's ID
     * @return Count of rooms where the user is a member
     */
    long countByMembersContaining(String userId);
    
    /**
     * Find rooms with a minimum number of members.
     * 
     * @param count The minimum number of members
     * @return List of rooms with at least the specified number of members
     */
    @Query(value = "{ }", fields = "{ 'id': 1, 'name': 1, 'members': 1 }")
    List<Room> findRoomsWithMemberInfo();
    
    /**
     * Find rooms with a specific number of files.
     * 
     * @param count The number of files
     * @return List of rooms with the specified number of files
     */
    @Query("{ 'sharedFiles': { $size: ?0 } }")
    List<Room> findByFileCount(int count);
    
    /**
     * Find empty rooms (no members except owner).
     * 
     * @return List of empty rooms
     */
    @Query("{ 'members': { $size: 1 } }")
    List<Room> findEmptyRooms();
}