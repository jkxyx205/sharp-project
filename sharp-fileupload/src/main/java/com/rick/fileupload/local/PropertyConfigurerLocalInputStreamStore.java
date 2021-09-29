package com.rick.fileupload.local;

import com.rick.fileupload.core.property.LocalProperties;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author Rick
 * @createdAt 2021-09-29 14:43:00
 */
@RequiredArgsConstructor
public class PropertyConfigurerLocalInputStreamStore extends LocalInputStreamStore {

    private final LocalProperties localProperties;

    @Override
    public String getGroupName() {
        return localProperties.getGroupName();
    }

    @Override
    public String getPath() {
        return "rick";
    }

    @Override
    public String getRoot() {
        return localProperties.getRootPath();
    }

    @Override
    public String getServerUrl() {
        return localProperties.getServerUrl();
    }
}
