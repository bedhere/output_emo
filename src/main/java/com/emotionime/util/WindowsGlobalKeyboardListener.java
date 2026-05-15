package com.emotionime.util;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HMODULE;
import com.sun.jna.platform.win32.WinDef.LPARAM;
import com.sun.jna.platform.win32.WinDef.WPARAM;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.platform.win32.WinUser.HHOOK;
import com.sun.jna.platform.win32.WinUser.MSG;

public class WindowsGlobalKeyboardListener {
    private static final int VK_BACK = 0x08;
    private static final int VK_RETURN = 0x0D;
    private static final int VK_CONTROL = 0x11;
    private static final int VK_LCONTROL = 0xA2;
    private static final int VK_RCONTROL = 0xA3;
    private static final int VK_E = 0x45;

    public interface Listener {
        void onCharacter(char c);
        void onBackspace();
        void onEnter();
        void onClearRequest();
    }

    private volatile boolean ctrlPressed = false;

    private volatile boolean windowFocused = false;
    private volatile boolean hookEnabled = true;

    public void setWindowFocused(boolean focused) {
        this.windowFocused = focused;
    }

    public void setHookEnabled(boolean enabled) {
        this.hookEnabled = enabled;
    }

    private final Listener listener;
    private volatile boolean running;
    private volatile int hookThreadId;

    private Thread hookThread;
    private HHOOK hook;
    private WinUser.LowLevelKeyboardProc keyboardProc;

    public WindowsGlobalKeyboardListener(Listener listener) {
        this.listener = listener;
    }

    public void start() {
        if (running) {
            return;
        }
        running = true;
        hookThread = new Thread(this::runHookLoop, "WindowsGlobalKeyboardHook");
        hookThread.setDaemon(true);
        hookThread.start();
    }

    public void stop() {
        running = false;
        if (hookThreadId != 0) {
            User32.INSTANCE.PostThreadMessage(hookThreadId, WinUser.WM_QUIT, new WPARAM(0), new LPARAM(0));
        }
        if (hookThread != null) {
            try {
                hookThread.join(1500);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void runHookLoop() {
        User32 user32 = User32.INSTANCE;
        hookThreadId = Kernel32.INSTANCE.GetCurrentThreadId();

        keyboardProc = (nCode, wParam, info) -> {
            if (nCode >= 0) {
                int msg = wParam.intValue();
                int vk = info.vkCode;
                if (vk == VK_CONTROL || vk == VK_LCONTROL || vk == VK_RCONTROL) {
                    ctrlPressed = (msg == WinUser.WM_KEYDOWN || msg == WinUser.WM_SYSKEYDOWN);
                }
                if (msg == WinUser.WM_KEYDOWN || msg == WinUser.WM_SYSKEYDOWN) {
                    processKey(vk);
                }
            }
            return user32.CallNextHookEx(hook, nCode, wParam, new LPARAM(Pointer.nativeValue(info.getPointer())));
        };

        HMODULE hMod = Kernel32.INSTANCE.GetModuleHandle(null);
        hook = user32.SetWindowsHookEx(WinUser.WH_KEYBOARD_LL, keyboardProc, hMod, 0);
        if (hook == null) {
            running = false;
            return;
        }

        MSG msg = new MSG();
        while (running) {
            int result = user32.GetMessage(msg, null, 0, 0);
            if (result == -1 || result == 0) {
                break;
            }
            user32.TranslateMessage(msg);
            user32.DispatchMessage(msg);
        }

        user32.UnhookWindowsHookEx(hook);
        hook = null;
        hookThreadId = 0;
        running = false;
    }

    private void processKey(int vkCode) {
        if (listener == null) return;

        // Ctrl+E 全局快捷键：无论hook开关、无论窗口焦点，始终生效
        if (ctrlPressed && vkCode == VK_E) {
            listener.onClearRequest();
            return;
        }

        if (!hookEnabled || windowFocused) return;

        if (vkCode == VK_BACK) {
            listener.onBackspace();
            return;
        }
        if (vkCode == VK_RETURN) {
            listener.onEnter();
            return;
        }

        byte[] keyboardState = new byte[256];
        User32 user32 = User32.INSTANCE;
        if (!user32.GetKeyboardState(keyboardState)) {
            return;
        }

        char[] out = new char[4];
        int scanCode = user32.MapVirtualKeyEx(vkCode, 0, user32.GetKeyboardLayout(0));
        int rc = user32.ToUnicodeEx(vkCode, scanCode, keyboardState, out, out.length, 0, user32.GetKeyboardLayout(0));
        if (rc > 0) {
            char c = out[0];
            if (!Character.isISOControl(c)) {
                listener.onCharacter(c);
            }
        }
    }
}
