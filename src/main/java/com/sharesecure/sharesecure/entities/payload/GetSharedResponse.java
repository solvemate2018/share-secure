package com.sharesecure.sharesecure.entities.payload;

import java.util.Collection;

import com.sharesecure.sharesecure.dtos.UserPrivilegeDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetSharedResponse {
    private Collection<UserPrivilegeDTO> userPrivileges;
}
