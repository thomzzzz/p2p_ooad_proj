package com.p2pexchange.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.p2pexchange.model.FileDB;

/**
 * Repository for file data access operations.
 */
public interface FileRepository extends MongoRepository<FileDB, String> {
    
    /**
     * Find files by owner ID.
     * 
     * @param ownerId The owner's ID
     * @return List of files owned by the user
     */
    List<FileDB> findByOwnerId(String ownerId);
    
    /**
     * Find files by name containing a query string and owner ID.
     * 
     * @param name Name query string (case-insensitive)
     * @param ownerId The owner's ID
     * @return List of files matching the criteria
     */
    @Query("{ 'filename': { $regex: ?0, $options: 'i' }, 'ownerId': ?1 }")
    List<FileDB> findByNameContainingAndOwnerId(String name, String ownerId);
    
    /**
     * Find files by content type.
     * 
     * @param contentType The content type to search for
     * @return List of files with matching content type
     */
    List<FileDB> findByContentTypeContaining(String contentType);
    
    /**
     * Find files uploaded after a specific timestamp.
     * 
     * @param timestamp The timestamp to filter by
     * @return List of files uploaded after the timestamp
     */
    @Query("{ 'uploadDate': { $gt: ?0 } }")
    List<FileDB> findByUploadDateAfter(long timestamp);
    
    /**
     * Find files by size greater than a specified value.
     * 
     * @param size The minimum file size
     * @return List of files larger than the specified size
     */
    List<FileDB> findBySizeGreaterThan(long size);
    
    /**
     * Find files by encryption type.
     * 
     * @param encryptionType The encryption type to search for
     * @return List of files with matching encryption type
     */
    List<FileDB> findByEncryptionType(String encryptionType);
    
    /**
     * Find files by owner ID and metadata key-value pair.
     * 
     * @param ownerId The owner's ID
     * @param key Metadata key
     * @param value Metadata value
     * @return List of files matching the criteria
     */
    @Query("{ 'ownerId': ?0, 'metadata.?1': ?2 }")
    List<FileDB> findByOwnerIdAndMetadata(String ownerId, String key, String value);
    
    /**
     * Count files by owner ID.
     * 
     * @param ownerId The owner's ID
     * @return Count of files owned by the user
     */
    long countByOwnerId(String ownerId);
    
    /**
     * Get total storage used by an owner.
     * 
     * @param ownerId The owner's ID
     * @return Total size of all files owned by the user
     */
    @Query(value = "{ 'ownerId': ?0 }", fields = "{ 'size': 1 }")
    List<FileDB> findSizesByOwnerId(String ownerId);
}