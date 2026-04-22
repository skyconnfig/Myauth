package com.myauth.common.util;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;

/**
 * 机器码获取工具类
 */
public class MachineUtil {
    
    /**
     * 获取机器码（基于MAC地址）
     */
    public static String getMachineId() {
        try {
            StringBuilder sb = new StringBuilder();
            Enumeration<NetworkInterface> networkInterfaces = 
                NetworkInterface.getNetworkInterfaces();
            
            for (NetworkInterface ni : Collections.list(networkInterfaces)) {
                byte[] mac = ni.getHardwareAddress();
                if (mac != null && mac.length > 0) {
                    for (int i = 0; i < mac.length; i++) {
                        sb.append(String.format("%02X", mac[i]));
                    }
                    // 只取第一个有效的MAC地址
                    break;
                }
            }
            
            String machineId = sb.toString();
            return machineId.isEmpty() ? "UNKNOWN" : machineId;
        } catch (SocketException e) {
            return "UNKNOWN";
        }
    }
    
    /**
     * 获取简化的机器码（前16位）
     */
    public static String getShortMachineId() {
        String machineId = getMachineId();
        if (machineId.length() > 16) {
            return machineId.substring(0, 16);
        }
        return machineId;
    }
}
