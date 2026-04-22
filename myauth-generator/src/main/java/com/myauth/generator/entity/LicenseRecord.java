package com.myauth.generator.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 授权记录实体
 */
@Data
@TableName("license_record")
public class LicenseRecord implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 授权码
     */
    private String licenseKey;
    
    /**
     * 过期时间
     */
    private String expireTime;
    
    /**
     * 机器码
     */
    private String machineId;
    
    /**
     * 授权类型
     */
    private String type;
    
    /**
     * 最大用户数
     */
    private Integer maxUsers;
    
    /**
     * 功能模块
     */
    private String modules;
    
    /**
     * 客户名称
     */
    private String customerName;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 状态: 0-禁用, 1-启用
     */
    private Integer status;
    
    /**
     * 逻辑删除
     */
    @TableLogic
    private Integer deleted;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
