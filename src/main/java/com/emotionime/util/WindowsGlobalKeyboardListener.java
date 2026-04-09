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

    public interface Listener {
        void onCharacter(char c);
        void onBackspace();
        void onEnter();
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
                if (msg == WinUser.WM_KEYDOWN || msg == WinUser.WM_SYSKEYDOWN) {
                    processKey(info.vkCode);
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
        if (listener == null) {
            return;
        }
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
