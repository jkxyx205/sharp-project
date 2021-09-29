package com.rick.fileupload.core.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Rick
 * @createdAt 2021-09-29 13:50:00
 */
@Getter
@Setter
public class FileMeta {

    private String name;

    private String extension;

    private String contentType;

    private Long size;

    private String groupName;

    private String path;

}
