package com.izzaz.appdrawer.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AppFile {
    private String name;
    private String path;
    private String htmlCode;
    private String displayName;
    private String displayPic;
}
