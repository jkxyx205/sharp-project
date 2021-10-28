package com.rick.demo.db;

import com.rick.db.plugin.SQLUtils;
import lombok.experimental.UtilityClass;

import java.util.Arrays;

/**
 * @author Rick
 * @createdAt 2021-10-28 09:38:00
 */
@UtilityClass
public class DataInit {

    public static void init() {
        SQLUtils.deleteNotIn("t_project", "id", Arrays.asList(479723134929764352L, 479723663504343040L, 479723663504343041L, 479723663504343042L, 479723663504343043L));
        SQLUtils.update("t_project", "is_deleted",new Object[] { false, 479723663504343042L, 479723663504343043L }, "id in (?,?)");
    }
}
