/**
 * RTC 相关类型（供历史消息占位展示，不再发起通话）
 */

export enum SignalTypeEnum {
  JOIN = "join",
  OFFER = "offer",
  ANSWER = "answer",
  CANDIDATE = "candidate",
  LEAVE = "leave",
}

export enum CallStatusEnum {
  CALLING = 1,
  ACCEPT = 2,
  END = 3,
  REJECT = 4,
  ERROR = 5,
  BUSY = 6,
  CANCEL = 7,
}

export enum CallTypeEnum {
  AUDIO = 1,
  VIDEO = 2,
}

export interface WSRtcCallMsg {
  roomId: number;
  callId: number;
  signalType: SignalTypeEnum;
  data: any;
  receiverIds: string[];
  senderId?: string;
  status: CallStatusEnum;
  type: CallTypeEnum;
  startTime: number;
  endTime?: number;
}
