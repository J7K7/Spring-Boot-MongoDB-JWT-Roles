package com.nosql.mongo.repositories;

import com.nosql.mongo.entities.PassportEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PassportRepository extends MongoRepository<PassportEntity, String> {


    Optional<PassportEntity> findByCertificateNumber(String s);
    boolean existsByCertificateNumber(String certificateID);
    List<PassportEntity> findBySender(String sender);
    List<PassportEntity> findByRecipient(String recipient);

}
