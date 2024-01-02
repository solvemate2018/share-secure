package com.sharesecure.sharesecure.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sharesecure.sharesecure.entities.UserPrivilege;

@Repository
public interface UserPrivilegeRepo extends JpaRepository<UserPrivilege, Long> {
    
}
