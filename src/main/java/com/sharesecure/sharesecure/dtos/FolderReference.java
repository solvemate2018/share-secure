package com.sharesecure.sharesecure.dtos;

import java.util.Collection;

import com.sharesecure.sharesecure.entities.enums.PrivilegeType;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(content = Include.NON_NULL)
public class FolderReference {
    private long id;

    private String folderName;

    private Collection<PrivilegeType> userPrivileges;

    private UserDTO folderOwner;

    private boolean synched;
}
