package com.nosql.mongo.repositories;

import com.nosql.mongo.entities.UserEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


public interface UserRepository extends MongoRepository<UserEntity, String> {
    Optional<UserEntity> findByEmail(String s);
    Optional<UserEntity> findByUsername(String s);
}
