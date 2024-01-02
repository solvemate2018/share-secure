package com.sharesecure.sharesecure.entities.folder;

import com.sharesecure.sharesecure.entities.User;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@DiscriminatorValue("ROOT")
public class RootFolder extends Folder {    
    @OneToOne
    @JoinColumn(name = "folder_owner_id")
    private User folderOwner;

}
