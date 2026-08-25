package com.example.spboot.repository;

import com.example.spboot.entity.Notes;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NotesRepository extends JpaRepository<Notes,Long> {// CRUD methodlari otomatik geliyor
    List<Notes> findByOwnerUsername(String ownerUsername);// Derived Query

    @Query("SELECT n FROM Notes n WHERE n.ownerUsername = :username AND n.icerik LIKE %:keyword%") // username ve keyword named parameter
    List<Notes> searchByKeyword(@Param("username") String username, @Param("keyword") String keyword );

}
