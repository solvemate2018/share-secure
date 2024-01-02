package com.sharesecure.sharesecure.dtos;

import java.util.Collection;
import java.util.List;

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
public class FolderDTO {
    private long id;

    private String folderName;

    private List<FileDTO> nestedFiles;

    private List<FolderReference> nestedFolders;

    private UserDTO folderOwner;

    private FolderReference parentFolder;

    private Collection<PrivilegeType> userPrivilege;

    private boolean synched;
}
