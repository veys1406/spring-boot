package com.example.spboot;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GradeRepository extends JpaRepository<Grade,Long> {// CRUD methodlari otomatik geliyor
    List<Grade> findByOwnerUsername(String ownerUsername);// SELECT * FROM grade WHERE owner_username = ?
}
