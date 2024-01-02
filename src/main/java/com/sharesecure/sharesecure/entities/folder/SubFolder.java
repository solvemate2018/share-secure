package com.sharesecure.sharesecure.entities.folder;

import java.util.ArrayList;
import java.util.Collection;

import com.sharesecure.sharesecure.entities.User;
import com.sharesecure.sharesecure.entities.UserPrivilege;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@DiscriminatorValue("SUB")
public class SubFolder extends Folder {
    @Column
    private String folderName;

    @OneToMany(mappedBy = "folder", cascade = CascadeType.ALL, orphanRemoval = true)
    private Collection<UserPrivilege> userPrivileges = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "parent_folder_id")
    private Folder parentFolder;

    public void addUserPrivilege(UserPrivilege privilege){
        userPrivileges.add(privilege);
    }

    public User getFolderOwner(){
        for (UserPrivilege privilege : userPrivileges) {
            if(privilege.isOwner())
            return privilege.getUser();
        }
        return null;
    }

    public boolean userHasAccess(User user){
        for (UserPrivilege privilege : userPrivileges) {
            if(privilege.getUser().equals(user))
            return true;
        }
        return false;
    }

    public UserPrivilege getUserPrivilegeForUser(User user) {
        for (UserPrivilege privilege : userPrivileges) {
            if(privilege.getUser().equals(user))
            return privilege;
        }
        return null;
    }

    public void removeUserPrivilege(UserPrivilege userPrivilege) {
        userPrivileges.remove(userPrivilege);
    }
}
