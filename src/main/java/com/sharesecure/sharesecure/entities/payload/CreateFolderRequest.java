package com.sharesecure.sharesecure.entities.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateFolderRequest {
    @NotBlank
    @Size(min = 3, max = 16)
    private String folderName;
}
