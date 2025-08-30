package com.rick.admin.core;

import com.rick.admin.sys.user.dao.UserDAO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Rick.Xu
 * @date 2025/8/30 14:48
 */
@Service
public class UserUpdateTest {

    @Autowired
    private UserDAO userDAO;

    @Test
    public void testUpdate() {
        userDAO.update("name", "id =  ?", "BS", 1L);
    }
}
