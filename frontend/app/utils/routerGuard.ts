import { type } from "@tauri-apps/plugin-os";

type OSType = "windows" | "macos" | "linux" | string;

export const DESKTOP_OS_TYPES: OSType[] = ["windows", "macos", "linux"];

/**
 * 获取阻止导航的提示消息
 */
export function getBlockNavigationMessage(_path: string): string {
  return "";
}

/**
 * 检查是否为桌面端
 */
export async function detectIsDesktop(
  setting: { isDesktop?: boolean },
): Promise<boolean> {
  try {
    if (setting?.isDesktop) {
      return true;
    }
    const osTypeName = type() as OSType;
    return DESKTOP_OS_TYPES.includes(osTypeName);
  }
  catch {
    return false;
  }
}

