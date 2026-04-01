package com.luckycolor.admin.modules.platform.codegen.web.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CodegenImportRequest {

    @NotEmpty
    private List<String> tableNames = new ArrayList<>();
}
