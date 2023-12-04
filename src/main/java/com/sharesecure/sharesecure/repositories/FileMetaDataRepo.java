package com.sharesecure.sharesecure.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sharesecure.sharesecure.entities.FileMetaData;

@Repository
public interface FileMetaDataRepo extends JpaRepository<FileMetaData, Long> {
    
}
