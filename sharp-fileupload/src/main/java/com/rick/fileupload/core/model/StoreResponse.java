package com.rick.fileupload.core.model;

import lombok.Value;

/**
 * @author Rick
 * @createdAt 2021-09-30 12:03:00
 */
@Value
public class StoreResponse {

    private String groupName;

    private String path;

    private String fullPath;

    private String url;
}
