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
public class FileDTO {
    private long fileId;

    private String fileName;

    private String fileType;

    private Collection<PrivilegeType> userPrivilege;

    private boolean synched;
}
