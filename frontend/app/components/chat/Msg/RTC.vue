<script lang="ts" setup>
import { MSG_CTX_NAMES } from "~/constants/msgContext";
import { CallTypeEnum } from "~/types/chat/rtc";

/**
 * RTC 消息（仅历史只读展示，不再发起通话）
 */
defineProps<{
  data: ChatMessageVO<RtcBodyMsgVO>
  prevMsg?: ChatMessageVO
  index: number
}>();
</script>


<template>
  <ChatMsgTemplate
    :prev-msg="prevMsg"
    :index="index"
    :data="data"
    v-bind="$attrs"
  >
    <template #body>
      <div :ctx-name="MSG_CTX_NAMES.RTC" class="msg-box msg-wrap min-w-6em flex items-center leading-1em">
        <i :ctx-name="MSG_CTX_NAMES.RTC" class="icon p-2.4" :class="data.message.body?.type === CallTypeEnum.AUDIO ? 'i-solar:end-call-outline' : 'i-solar:videocamera-record-outline'" />
        {{ data.message.content }}
      </div>
    </template>
  </ChatMsgTemplate>
</template>

<style lang="scss" scoped>
@use "./msg.scss";

.icon {
  margin: 0 0.2em 0.1em 0;
}
.self {
  .msg-box {
    .icon {
      order: 1;
      margin: 0 0 0.1em 0.4em;
    }
  }
}
</style>
