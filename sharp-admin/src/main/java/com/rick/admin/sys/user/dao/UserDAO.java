package com.rick.admin.sys.user.dao;

import com.rick.admin.sys.user.entity.User;
import com.rick.db.plugin.dao.core.EntityCodeDAOImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * @author Rick.Xu
 * @date 2023/5/28 15:01
 */
@Repository
@RequiredArgsConstructor
public class UserDAO extends EntityCodeDAOImpl<User, Long> {

//    private final PasswordEncoder passwordEncoder;
//
//    @Override
//    public int insertOrUpdate(Map<String, Object> params) {
//        params.put("password", passwordEncoder.encode(AuthConstants.DEFAULT_PASSWORD));
//
//        List<String> roleIdList = (List<String>) params.get("roleIds");
//        List<Role> roleList = Lists.newArrayListWithExpectedSize(roleIdList.size());
//        for (String id : roleIdList) {
//            roleList.add(Role.builder().id(Long.parseLong(id)).build());
//        }
//        params.put("roleList", roleList);
//
//        return super.insertOrUpdate(params);
//    }

    @Override
    public int update(User entity) {
        return super.update(entity);
    }
}