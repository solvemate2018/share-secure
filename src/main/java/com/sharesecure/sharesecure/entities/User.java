package com.sharesecure.sharesecure.entities;

import java.util.ArrayList;
import java.util.Collection;

import org.hibernate.validator.constraints.UniqueElements;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Collection<UserFileMapping> accessibleFiles = new ArrayList<UserFileMapping>();

    public boolean hasAccessToFile(String fileName){
        for (UserFileMapping userFileMapping : accessibleFiles) {
            if(userFileMapping.getFileMetaData().getFileName() == fileName){
                return true;
            }
        }
        return false;
    }
}
