package com.sharesecure.sharesecure.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sharesecure.sharesecure.entities.folder.Folder;

@Repository
public interface FolderRepo extends JpaRepository<Folder, Long> {
    
}
