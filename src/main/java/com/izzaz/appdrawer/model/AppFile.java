package com.izzaz.appdrawer.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AppFile {
    private String id;
    private String displayName;
    private String pathToApp;
    private String pathToImage;
    private String htmlCode;
}
