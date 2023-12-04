package com.sharesecure.sharesecure.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sharesecure.sharesecure.entities.UserFileMapping;

public interface UserFile extends JpaRepository<UserFileMapping, Long> {
    
}
