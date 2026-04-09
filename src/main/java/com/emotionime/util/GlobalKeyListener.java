package com.emotionime.util;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 全局键盘监听器 - 使用JNA实现Windows全局热键注册
 */
public class GlobalKeyListener {
    
    public interface User32 extends StdCallLibrary {
        User32 INSTANCE = Native.load("user32", User32.class, W32APIOptions.DEFAULT_OPTIONS);
        
        boolean RegisterHotKey(Pointer hWnd, int id, int fsModifiers, int vk);
        boolean UnregisterHotKey(Pointer hWnd, int id);
        int GetMessage(MSG lpMsg, Pointer hWnd, int wMsgFilterMin, int wMsgFilterMax);
        boolean TranslateMessage(MSG lpMsg);
        boolean DispatchMessage(MSG lpMsg);
        Pointer FindWindow(String lpClassName, String lpWindowName);
    }
    
    public static class MSG extends Structure {
        public Pointer hwnd;
        public int message;
        public int wParam;
        public int lParam;
        public int time;
        public int pt_x;
        public int pt_y;
        
        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("hwnd", "message", "wParam", "lParam", "time", "pt_x", "pt_y");
        }
    }
    
    private static final int MOD_CONTROL = 0x0002;
    private static final int MOD_SHIFT = 0x0004;
    private static final int MOD_ALT = 0x0001;
    private static final int VK_E = 0x45;
    private static final int WM_HOTKEY = 0x0312;
    
    private GlobalHotKeyListener listener;
    private Thread monitorThread;
    private volatile boolean isRunning = false;
    
    public interface GlobalHotKeyListener {
        void onHotKeyPressed(int keyCode);
    }
    
    public GlobalKeyListener(GlobalHotKeyListener listener) {
        this.listener = listener;
    }
    
    /**
     * 注册全局热键 Ctrl+Shift+E
     */
    public void registerGlobalHotKey() {
        try {
            System.out.println("[DEBUG] 准备注册全局热键 Ctrl+Shift+E");
            
            // 注册热键 - 使用hotkey ID 为 1
            int modifiers = MOD_CONTROL | MOD_SHIFT;
            boolean result = User32.INSTANCE.RegisterHotKey(null, 1, modifiers, VK_E);
            
            if (result) {
                System.out.println("[DEBUG] 全局热键Ctrl+Shift+E注册成功");
                startHotKeyMonitor();
            } else {
                System.err.println("[ERROR] 全局热键注册失败 - 可能被其他程序占用或需要管理员权限");
            }
        } catch (Exception e) {
            System.err.println("[ERROR] 注册全局热键异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 启动热键监听线程
     */
    private void startHotKeyMonitor() {
        if (isRunning) return;
        
        isRunning = true;
        monitorThread = new Thread(() -> {
            System.out.println("[DEBUG] 热键监听线程已启动");
            try {
                MSG msg = new MSG();
                while (isRunning) {
                    int result = User32.INSTANCE.GetMessage(msg, null, 0, 0);
                    
                    if (result > 0) {
                        if (msg.message == WM_HOTKEY) {
                            int hotKeyId = msg.wParam;
                            System.out.println("[DEBUG] 捕获到热键信号: " + hotKeyId);
                            
                            if (hotKeyId == 1 && listener != null) {
                                System.out.println("[DEBUG] 触发Ctrl+Shift+E回调");
                                listener.onHotKeyPressed(hotKeyId);
                            }
                        }
                        User32.INSTANCE.TranslateMessage(msg);
                        User32.INSTANCE.DispatchMessage(msg);
                    } else {
                        Thread.sleep(10);
                    }
                }
            } catch (Exception e) {
                System.err.println("[ERROR] 热键监听异常: " + e.getMessage());
                e.printStackTrace();
            }
            System.out.println("[DEBUG] 热键监听线程已停止");
        }, "GlobalHotKeyListener");
        
        monitorThread.setDaemon(true);
        monitorThread.start();
    }
    
    /**
     * 卸载全局热键
     */
    public void unregisterGlobalHotKey() {
        try {
            isRunning = false;
            
            if (monitorThread != null) {
                monitorThread.interrupt();
                monitorThread.join(1000);
            }
            
            User32.INSTANCE.UnregisterHotKey(null, 1);
            System.out.println("[DEBUG] 全局热键已卸载");
        } catch (Exception e) {
            System.err.println("[ERROR] 卸载全局热键异常: " + e.getMessage());
        }
    }
}
