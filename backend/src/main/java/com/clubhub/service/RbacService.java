package com.clubhub.service;

import java.util.List;

public interface RbacService {

    List<String> getRoleCodeListByUserId(Long userId);

    List<String> getPermissionCodeListByUserId(Long userId);
}
