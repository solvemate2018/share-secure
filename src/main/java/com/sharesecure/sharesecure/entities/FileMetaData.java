package com.sharesecure.sharesecure.entities;

import java.util.ArrayList;
import java.util.Collection;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class FileMetaData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column
    private String fileName;

    @Column
    private String fileType;

    @OneToMany(mappedBy = "fileMetaData", cascade = CascadeType.ALL, orphanRemoval = true)
    private Collection<UserFileMapping> usersWithAccess = new ArrayList<UserFileMapping>();

    public void addUserWithAccess(UserFileMapping userFile){
        usersWithAccess.add(userFile);
    }
}
