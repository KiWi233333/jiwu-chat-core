import type { Result } from "~/types/result";
import { StatusCode } from "~/types/result";

/**
 * 按 ID 获取用户信息（占位：社区模块已移除，后端暂无独立接口）
 * 返回空结果，供用户页/聊天内查看他人资料时降级展示
 */
export function getCommUserInfoSe(_userId: string, _token: string): Promise<Result<CommUserVO>> {
  return Promise.resolve({
    code: StatusCode.SELECT_ERR,
    message: "",
    data: null as unknown as CommUserVO,
  });
}

export interface CommUserVO {
  avatar?: string;
  nickname?: string;
  [key: string]: any;
}
