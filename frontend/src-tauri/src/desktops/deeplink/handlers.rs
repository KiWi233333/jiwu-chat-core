use tauri::AppHandle;
use super::oauth::is_oauth_callback;

/// 处理应用启动时的深度链接
pub fn handle_startup_urls(_app: &AppHandle, urls: Vec<url::Url>) {
    for url in urls {
        let url_str = url.as_str();
        // OAuth 已移除：仅识别 URL 避免未处理协议报错，不再向后端发送事件
        if is_oauth_callback(url_str) {
            // no-op
        }
    }
}

/// 处理运行时接收到的深度链接
pub fn handle_runtime_url(_app: &AppHandle, url: &str) {
    // OAuth 已移除：仅识别 URL 避免未处理协议报错，不再向后端发送事件
    if is_oauth_callback(url) {
        // no-op
    }
}