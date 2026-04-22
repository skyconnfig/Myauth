package com.myauth.generator.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.myauth.common.entity.License;
import com.myauth.generator.common.Result;
import com.myauth.generator.entity.LicenseRecord;
import com.myauth.generator.service.LicenseGeneratorService;
import com.myauth.generator.service.LicenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * License管理控制器
 */
@Tag(name = "License管理", description = "授权码生成和管理接口")
@RestController
@RequestMapping("/api/license")
@RequiredArgsConstructor
public class LicenseController {
    
    private final LicenseService licenseService;
    private final LicenseGeneratorService generatorService;
    
    @Operation(summary = "生成密钥对")
    @PostMapping("/generate-keypair")
    public Result<Map<String, String>> generateKeyPair() {
        try {
            generatorService.generateKeyPair();
            Map<String, String> result = new HashMap<>();
            result.put("message", "密钥对生成成功");
            result.put("privateKeyPath", "license/private.key");
            result.put("publicKeyPath", "license/public.key");
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("密钥对生成失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "获取公钥（用于客户端）")
    @GetMapping("/public-key")
    public Result<String> getPublicKey() {
        try {
            String publicKey = generatorService.getPublicKeyForClient();
            return Result.success(publicKey);
        } catch (Exception e) {
            return Result.error("获取公钥失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "生成新的License")
    @PostMapping("/generate")
    public Result<Map<String, Object>> generateLicense(@RequestBody License license) {
        try {
            String licenseKey = licenseService.generateLicense(license);
            
            Map<String, Object> result = new HashMap<>();
            result.put("licenseKey", licenseKey);
            result.put("expireTime", license.getExpireTime());
            result.put("type", license.getType());
            result.put("customerName", license.getCustomerName());
            
            return Result.success("License生成成功", result);
        } catch (Exception e) {
            return Result.error("License生成失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "分页查询License列表")
    @GetMapping("/page")
    public Result<Page<LicenseRecord>> pageLicenses(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String customerName) {
        try {
            Page<LicenseRecord> page = licenseService.pageLicenses(current, size, customerName);
            return Result.success(page);
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "获取所有License列表")
    @GetMapping("/list")
    public Result<List<LicenseRecord>> listAll() {
        try {
            List<LicenseRecord> list = licenseService.listAll();
            return Result.success(list);
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "根据ID查询License")
    @GetMapping("/{id}")
    public Result<LicenseRecord> getById(@PathVariable Long id) {
        try {
            LicenseRecord record = licenseService.getById(id);
            if (record == null) {
                return Result.error("授权记录不存在");
            }
            return Result.success(record);
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "更新License状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        try {
            licenseService.updateStatus(id, status);
            return Result.success("状态更新成功", null);
        } catch (Exception e) {
            return Result.error("状态更新失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "删除License")
    @DeleteMapping("/{id}")
    public Result<Void> deleteById(@PathVariable Long id) {
        try {
            licenseService.deleteById(id);
            return Result.success("删除成功", null);
        } catch (Exception e) {
            return Result.error("删除失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "重新生成License（延期）")
    @PostMapping("/{id}/regenerate")
    public Result<Map<String, Object>> regenerateLicense(
            @PathVariable Long id, 
            @RequestBody License newLicense) {
        try {
            String licenseKey = licenseService.regenerateLicense(id, newLicense);
            
            Map<String, Object> result = new HashMap<>();
            result.put("licenseKey", licenseKey);
            result.put("expireTime", newLicense.getExpireTime());
            
            return Result.success("License重新生成成功", result);
        } catch (Exception e) {
            return Result.error("License重新生成失败: " + e.getMessage());
        }
    }
}
