package com.shieldai.agent.detections;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.platform.win32.Kernel32;

import java.util.ArrayList;
import java.util.List;

public class Win32WindowUtil {

    public static class WindowInfo {
        public String processName;
        public String title;
        public String url; // placeholder for future URL grabbing

        public WindowInfo(String processName, String title, String url) {
            this.processName = processName;
            this.title = title;
            this.url = url;
        }
    }

    public static List<WindowInfo> getAllWindowInfo() {
        final List<WindowInfo> windows = new ArrayList<>();
        User32.INSTANCE.EnumWindows((hWnd, data) -> {
            if (User32.INSTANCE.IsWindowVisible(hWnd)) {
                char[] buffer = new char[512];
                User32.INSTANCE.GetWindowText(hWnd, buffer, 512);
                String title = Native.toString(buffer).trim();
                if (!title.isEmpty()) {
                    String processName = getProcessName(hWnd);
                    windows.add(new WindowInfo(processName, title, ""));
                }
            }
            return true;
        }, null);
        return windows;
    }

    private static String getProcessName(HWND hWnd) {
        IntByReference pid = new IntByReference();
        User32.INSTANCE.GetWindowThreadProcessId(hWnd, pid);
        Kernel32 kernel = Kernel32.INSTANCE;

        com.sun.jna.platform.win32.WinNT.HANDLE process =
                kernel.OpenProcess(Kernel32.PROCESS_QUERY_INFORMATION | Kernel32.PROCESS_VM_READ, false, pid.getValue());

        if (process != null) {
            char[] exePath = new char[512];
            Psapi.INSTANCE.GetModuleBaseNameW(process, null, exePath, 512);
            kernel.CloseHandle(process);
            return Native.toString(exePath).toLowerCase();
        }
        return "";
    }

    public interface Psapi extends com.sun.jna.Library {
        Psapi INSTANCE = Native.load("psapi", Psapi.class);
        int GetModuleBaseNameW(com.sun.jna.platform.win32.WinNT.HANDLE hProcess,
                               com.sun.jna.platform.win32.WinNT.HANDLE hModule,
                               char[] lpBaseName, int nSize);
    }
}
