/**
 * 输入框/回复/艾特/机器人 相关
 * @returns 输入框/回复/艾特/机器人 相关
 */
export function createComposeInputModule() {
  /** 消息表单 */
  const msgForm = ref<ChatMessageDTO>({ roomId: -1, msgType: MessageType.TEXT, content: undefined, body: {} });
  /** 回复消息 */
  const replyMsg = ref<Partial<ChatMessageVO>>();
  /** 艾特用户列表 */
  const atUserList = ref<Partial<AtChatMemberOption>[]>([]);

  /**
   * 设置回复消息
   * @param item 消息
   */
  function setReplyMsg(item: Partial<ChatMessageVO>) {
    replyMsg.value = item;
  }

  /**
   * 设置艾特用户
   * @param userId 用户id
   */
  function setAtUid(userId: string) {
    if (!userId || atUserList.value.find(p => p.userId === userId))
      return;
    mitter.emit(MittEventType.CHAT_AT_USER, { type: "add", payload: userId });
  }

  /**
   * 移除艾特用户
   * @param username 用户名
   */
  function removeAtByUsername(username?: string) {
    if (!username)
      return;
    atUserList.value = atUserList.value.filter(p => p.username !== username);
  }

  function resetCompose() {
    msgForm.value = { roomId: -1, msgType: MessageType.TEXT, content: undefined, body: {} };
    replyMsg.value = undefined;
    atUserList.value = [];
  }

  return {
    msgForm,
    replyMsg,
    atUserList,
    setReplyMsg,
    setAtUid,
    removeAtByUsername,
    resetCompose,
  };
}


