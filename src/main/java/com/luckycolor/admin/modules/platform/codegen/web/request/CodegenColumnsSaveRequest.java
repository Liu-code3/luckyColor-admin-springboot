package com.luckycolor.admin.modules.platform.codegen.web.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CodegenColumnsSaveRequest {

    @Valid
    @NotEmpty
    private List<CodegenColumnSaveItem> columns = new ArrayList<>();
}
