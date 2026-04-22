package com.myauth.common.entity;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * License授权实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class License implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 授权码
     */
    private String licenseKey;
    
    /**
     * 过期时间 (格式: yyyy-MM-dd)
     */
    private String expireTime;
    
    /**
     * 机器码（可选，用于绑定设备）
     */
    private String machineId;
    
    /**
     * 授权类型: trial(试用), standard(标准), professional(专业), enterprise(企业)
     */
    private String type;
    
    /**
     * 最大用户数
     */
    private Integer maxUsers;
    
    /**
     * 功能模块（JSON数组字符串，如: ["module1", "module2"]）
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
}
