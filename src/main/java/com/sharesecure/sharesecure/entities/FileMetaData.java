package com.sharesecure.sharesecure.entities;

import java.util.ArrayList;
import java.util.Collection;

import com.sharesecure.sharesecure.entities.enums.PrivilegeType;
import com.sharesecure.sharesecure.entities.folder.Folder;

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
    private String normalizedFileName;

    @Column
    private String fileType;

    @Transient
    private boolean synched = true;

    @ManyToOne
    @JoinColumn(name = "folder_id")
    private Folder folder;

    @OneToMany(mappedBy = "file", cascade = CascadeType.ALL, orphanRemoval = true)
    private Collection<UserPrivilege> userPrivileges = new ArrayList<>();

    public void addUserPrivilege(UserPrivilege userPrivilege) {
        userPrivileges.add(userPrivilege);
    }

    public boolean getSynched(){
        return synched;
    }

    public User getFileOwner(){
        for (UserPrivilege privilege : userPrivileges) {
            if(privilege.isOwner())
            return privilege.getUser();
        }
        return null;
    }

    public boolean userHasAnyAccess(User user){
        for(UserPrivilege privilege : userPrivileges){
            if(privilege.getUser().equals(user)){
                return true;
            }
        }
        return false;
    }

    public boolean userHasDownloadAccess(User user){
        for(UserPrivilege privilege : userPrivileges){
            if(privilege.getUser().equals(user) && (privilege.getUserPrivileges().contains(PrivilegeType.DOWNLOAD) || privilege.getUserPrivileges().contains(PrivilegeType.DOWNLOAD_DELETE) ||privilege.getUserPrivileges().contains(PrivilegeType.DOWNLOAD_DELETE_SHARE))){
                return true;
            }
        }
        return false;
    }

    public boolean userHasDeleteAccess(User user){
        for(UserPrivilege privilege : userPrivileges){
            if(privilege.getUser().equals(user) && (privilege.getUserPrivileges().contains(PrivilegeType.DOWNLOAD_DELETE) || privilege.getUserPrivileges().contains(PrivilegeType.DOWNLOAD_DELETE_SHARE))){
                return true;
            }
        }
        return false;
    }

    public boolean userHasSharingAccess(User user){
        for(UserPrivilege privilege : userPrivileges){
            if(privilege.getUser().equals(user) && privilege.getUserPrivileges().contains(PrivilegeType.DOWNLOAD_DELETE_SHARE)){
                return true;
            }
        }
        return false;
    }

    public UserPrivilege getUserPrivilegeForUser(User user) {
        for(UserPrivilege privilege : userPrivileges){
            if(privilege.getUser().equals(user)){
                return privilege;
            }
        }
        return null;
    }

    
    public void removeUserPrivilege(UserPrivilege userPrivilege) {
        userPrivileges.remove(userPrivilege);
    }
}
