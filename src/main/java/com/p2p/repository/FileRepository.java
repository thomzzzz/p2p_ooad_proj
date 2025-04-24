package com.p2p.repository;

import com.p2p.model.File;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface FileRepository extends MongoRepository<File, String> {
    List<File> findByOwnerId(String ownerId);
}