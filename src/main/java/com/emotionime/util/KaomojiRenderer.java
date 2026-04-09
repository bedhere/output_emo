package com.emotionime.util;

import java.awt.*;

/**
 * 颜文字渲染辅助类
 * 解决特殊Unicode字符显示问题
 */
public class KaomojiRenderer {
    
    /**
     * 获取最佳颜文字字体（专门用于显示颜文字）
     */
    public static Font getBestKaomojiFont() {
        return getBestKaomojiFont(20);
    }
    
    /**
     * 获取最佳颜文字字体（指定大小）
     * 这个字体专门用于显示颜文字，优先级选择支持Unicode符号的字体
     */
    public static Font getBestKaomojiFont(int size) {
        // 字体优先级列表（按Unicode符号支持度排序）
        String[] preferredFonts = {
            "Segoe UI Symbol",     // Windows符号字体，Unicode支持最好
            "Yu Gothic",           // 日本字体，Unicode支持好
            "Meiryo",              // 现代日文字体
            "MS PGothic",          // 日文等宽字体
            "Arial Unicode MS",    // 广泛Unicode支持
            "Segoe UI Emoji",      // Windows 10+ Emoji字体
            "Microsoft YaHei UI",  // 微软雅黑UI版
            "Microsoft YaHei"      // 微软雅黑
        };
        
        return findBestFont(preferredFonts, size, "颜文字");
    }
    
    /**
     * 获取最佳中文字体（用于标题和输入框）
     * 这个字体优先选择支持中文的字体
     */
    public static Font getBestChineseFont(int size) {
        // 字体优先级列表（按中文支持度排序）
        String[] preferredFonts = {
            "Microsoft YaHei UI",  // 微软雅黑UI版，中文支持最好
            "Microsoft YaHei",     // 微软雅黑
            "SimSun",              // 宋体
            "Segoe UI",            // Windows默认UI字体
            "Segoe UI Symbol",     // Windows符号字体
            "Arial Unicode MS"     // Unicode支持
        };
        
        return findBestFont(preferredFonts, size, "中文");
    }
    
    /**
     * 查找最佳字体（通用方法）
     */
    private static Font findBestFont(String[] preferredFonts, int size, String purpose) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] availableFonts = ge.getAvailableFontFamilyNames();
        
        System.out.println("[KaomojiRenderer] 系统可用字体数量: " + availableFonts.length);
        
        // 查找第一个可用的字体
        for (String preferred : preferredFonts) {
            for (String available : availableFonts) {
                if (available.equalsIgnoreCase(preferred)) {
                    System.out.println("[KaomojiRenderer] ✓ 使用字体: " + preferred + " (大小: " + size + ", 用途: " + purpose + ")");
                    return new Font(preferred, Font.PLAIN, size);
                }
            }
        }
        
        // 如果都不存在，使用系统默认字体
        System.out.println("[KaomojiRenderer] ✗ 警告: 未找到合适的" + purpose + "字体，使用默认字体");
        return new Font(Font.SANS_SERIF, Font.PLAIN, size);
    }
    
    /**
     * 检查字符串中是否包含需要特殊处理的Unicode字符
     */
    public static boolean containsSpecialUnicode(String text) {
        if (text == null) return false;
        
        for (char c : text.toCharArray()) {
            // 检查是否为特殊Unicode字符
            if (c >= 0x3000 && c <= 0x30FF) return true;  // 日文
            if (c >= 0x2500 && c <= 0x257F) return true;  // 制表符
            if (c >= 0x2600 && c <= 0x26FF) return true;  // 杂项符号
            if (c >= 0x2700 && c <= 0x27BF) return true;  // 装饰符号
            if (c >= 0xFE00 && c <= 0xFE0F) return true;  // 变体选择符
            if (c >= 0xFF00 && c <= 0xFFEF) return true;  // 半角/全角形式
        }
        return false;
    }
}
