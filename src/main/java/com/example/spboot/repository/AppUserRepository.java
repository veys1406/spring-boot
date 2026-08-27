package com.example.spboot.repository;

import com.example.spboot.entity.AppUser;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;


public interface AppUserRepository extends MongoRepository<AppUser,String> {
    Optional<AppUser> findByUsername(String username);// MongoRepo kullanmak guven saglamiyor bu method password alip
                                                        // bide Object olarak alsaydik LoginRequestde yine acik vericektik
    //Mongo Template yani sorguyu elle kurmak da tamamen guvenli degil fieldlar Object oldugu surece
}
