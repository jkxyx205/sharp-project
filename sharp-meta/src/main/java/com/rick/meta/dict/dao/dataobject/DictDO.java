package com.rick.meta.dict.dao.dataobject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @author rick
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DictDO {

    private String type;

    private String name;

    private String label;

    private Integer sort;
}
