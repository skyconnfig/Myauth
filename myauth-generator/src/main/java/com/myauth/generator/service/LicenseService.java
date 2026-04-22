package com.myauth.generator.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.myauth.common.entity.License;
import com.myauth.generator.entity.LicenseRecord;
import com.myauth.generator.mapper.LicenseRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * License业务服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LicenseService {
    
    private final LicenseRecordMapper licenseRecordMapper;
    private final LicenseGeneratorService generatorService;
    
    /**
     * 生成新的License
     */
    @Transactional(rollbackFor = Exception.class)
    public String generateLicense(License license) throws Exception {
        // 1. 生成License Key
        String licenseKey = generatorService.generateLicenseKey(license);
        
        // 2. 保存到数据库
        LicenseRecord record = new LicenseRecord();
        record.setLicenseKey(licenseKey);
        record.setExpireTime(license.getExpireTime());
        record.setMachineId(license.getMachineId());
        record.setType(license.getType());
        record.setMaxUsers(license.getMaxUsers());
        record.setModules(license.getModules());
        record.setCustomerName(license.getCustomerName());
        record.setRemark(license.getRemark());
        record.setStatus(1); // 启用状态
        
        licenseRecordMapper.insert(record);
        
        log.info("License已保存到数据库，ID: {}", record.getId());
        
        return licenseKey;
    }
    
    /**
     * 分页查询License记录
     */
    public Page<LicenseRecord> pageLicenses(int current, int size, String customerName) {
        Page<LicenseRecord> page = new Page<>(current, size);
        LambdaQueryWrapper<LicenseRecord> wrapper = new LambdaQueryWrapper<>();
        
        if (customerName != null && !customerName.isEmpty()) {
            wrapper.like(LicenseRecord::getCustomerName, customerName);
        }
        
        wrapper.orderByDesc(LicenseRecord::getCreateTime);
        
        return licenseRecordMapper.selectPage(page, wrapper);
    }
    
    /**
     * 根据ID查询License
     */
    public LicenseRecord getById(Long id) {
        return licenseRecordMapper.selectById(id);
    }
    
    /**
     * 更新License状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        LicenseRecord record = new LicenseRecord();
        record.setId(id);
        record.setStatus(status);
        licenseRecordMapper.updateById(record);
    }
    
    /**
     * 删除License
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        licenseRecordMapper.deleteById(id);
    }
    
    /**
     * 重新生成License（延期或修改）
     */
    @Transactional(rollbackFor = Exception.class)
    public String regenerateLicense(Long id, License newLicense) throws Exception {
        // 1. 查询原记录
        LicenseRecord oldRecord = licenseRecordMapper.selectById(id);
        if (oldRecord == null) {
            throw new RuntimeException("授权记录不存在");
        }
        
        // 2. 禁用旧License
        oldRecord.setStatus(0);
        licenseRecordMapper.updateById(oldRecord);
        
        // 3. 生成新License
        return generateLicense(newLicense);
    }
    
    /**
     * 获取所有License列表
     */
    public List<LicenseRecord> listAll() {
        LambdaQueryWrapper<LicenseRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(LicenseRecord::getCreateTime);
        return licenseRecordMapper.selectList(wrapper);
    }
}
