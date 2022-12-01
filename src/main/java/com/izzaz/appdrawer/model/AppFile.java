package com.izzaz.appdrawer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppFile {
    private String id;
    private String displayName;
    private String pathToApp;
    private String pathToImage;
    private String htmlCode;
}
