package com.sharesecure.sharesecure.entities.payload;

import com.sharesecure.sharesecure.entities.enums.PrivilegeType;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GiveAccessRequest {
    @Email
    @Size(min = 6, max = 20)
    String userEmail;
    
    PrivilegeType[] privileges;
}
