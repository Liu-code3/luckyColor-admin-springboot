package com.luckycolor.admin.modules.system.user.web.request;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SystemUserAssignRolesRequest {

    private List<String> roleCodes = new ArrayList<>();
}
