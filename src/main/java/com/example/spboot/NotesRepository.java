package com.example.spboot;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NotesRepository extends MongoRepository<Notes,String> {// CRUD methodlari otomatik geliyor
    List<Notes> findByOwnerUsername(String ownerUsername);// MongoDB sorgusu uretir SQL degil
}
