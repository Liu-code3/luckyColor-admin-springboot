package com.luckycolor.admin.modules.system.notice.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import com.luckycolor.admin.infrastructure.persistence.dataobject.TenantBaseDO;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("sys_notice")
public class NoticeDO extends TenantBaseDO {

    private String noticeTitle;

    private String noticeType;

    private String noticeContent;

    private Integer publishStatus;

    private LocalDateTime publishTime;

    private Integer sort;

    private String remark;
}
