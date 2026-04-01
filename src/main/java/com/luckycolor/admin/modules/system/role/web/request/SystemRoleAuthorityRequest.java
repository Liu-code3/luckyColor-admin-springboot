package com.luckycolor.admin.modules.system.role.web.request;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SystemRoleAuthorityRequest {

    private List<Long> menuIds = new ArrayList<>();

    private List<String> permissionCodes = new ArrayList<>();

    private String dataScope;

    private Long departmentId;

    private List<Long> departmentIds = new ArrayList<>();
}
