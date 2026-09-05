package com.example.spboot.repository;

import com.example.spboot.entity.Notes;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NotesRepository extends MongoRepository<Notes,String> {// CRUD methodlari otomatik geliyor
    List<Notes> findByOwnerUsername(String ownerUsername);// Derived Query

    @Query("{ 'ownerUsername': ?0, 'icerik': { $regex: ?1, $options: 'i' } }")
    List<Notes> searchByKeyword(String username, String keyword);

}
