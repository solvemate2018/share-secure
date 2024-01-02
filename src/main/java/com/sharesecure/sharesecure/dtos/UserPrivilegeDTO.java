package com.sharesecure.sharesecure.dtos;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.sharesecure.sharesecure.entities.enums.PrivilegeType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(content = Include.NON_NULL)
public class UserPrivilegeDTO {
    private FileDTO file;

    private FolderReference folder;

    private UserDTO user;

    private Collection<PrivilegeType> userPrivileges;
}
