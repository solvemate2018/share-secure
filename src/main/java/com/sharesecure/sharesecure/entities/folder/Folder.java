package com.sharesecure.sharesecure.entities.folder;

import java.util.ArrayList;
import java.util.Collection;

import com.sharesecure.sharesecure.entities.FileMetaData;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "folder")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "folder_type", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
public abstract class Folder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column
    private String folderNormalizedName;
    
    @Transient
    private boolean synched = true;

    @OneToMany(mappedBy = "folder", cascade = CascadeType.ALL, orphanRemoval = true)
    private Collection<FileMetaData> subFiles = new ArrayList<>();

    @OneToMany(mappedBy = "parentFolder", cascade = CascadeType.ALL, orphanRemoval = true)
    private Collection<SubFolder> subFolders = new ArrayList<>();

    public void addFile(FileMetaData fileMetaData) {
        subFiles.add(fileMetaData);
    }

    public void removeFile(FileMetaData fileMetaData) {
        fileMetaData.setFolder(null);
        subFiles.remove(fileMetaData);
    }

    public void addFolder(SubFolder folder) {
        subFolders.add(folder);
    }

    public void removeFolder(SubFolder folder) {
        folder.setParentFolder(null); 
        subFolders.remove(folder);
    }

    public boolean getSynched(){
        return synched;
    }
}
