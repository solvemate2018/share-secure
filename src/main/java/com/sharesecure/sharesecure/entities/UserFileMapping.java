package com.sharesecure.sharesecure.entities;

import com.sharesecure.sharesecure.entities.enums.UserPrivileges;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class UserFileMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long mappingID;

    @ManyToOne
    @JoinColumn(name = "UserID")
    private User user;

    @ManyToOne
    @JoinColumn(name = "FileDataID")
    private FileMetaData fileMetaData;

    @Column
    @Enumerated(EnumType.STRING)
    private UserPrivileges userPrivileges;

    @Column
    private boolean isOwner;
}
