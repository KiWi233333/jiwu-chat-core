<script lang="ts" setup>
import { appBlogHost, giteeReportUrl, githubReportUrl } from "~/constants";

const setting = useSettingStore();
const downloadUrl = ref(appBlogHost);

watch([() => setting.isWeb, () => setting.isMobileSize], ([isWeb]) => {
  if (!isWeb)
    return;
  downloadUrl.value = appBlogHost;
}, { immediate: true });

const GITHUB = "github";
const GITEE = "gitee";
function handleRepo(type: typeof GITHUB | typeof GITEE, _isStar: boolean = true) {
  window.open(type === GITHUB ? `${githubReportUrl}` : `${giteeReportUrl}`, "_blank");
}
</script>

<template>
  <template v-if="setting.isWeb">
    <el-tooltip content="JiwuChat 官网" placement="bottom">
      <a
        :href="appBlogHost" target="_blank"
        title="JiwuChat 官网"
        class="h-2rem w-2rem flex-row-c-c cursor-pointer border-default rounded-full card-default shadow-sm sm:(h-1.8rem w-1.8rem)"
        v-bind="$attrs"
      >
        <i i-solar:planet-2-bold-duotone p-2.5 text-theme-primary />
      </a>
    </el-tooltip>
    <el-popconfirm
      title="如果你觉得这个项目不错，并且您愿意的话，欢迎给项目点个Star！"
      confirm-button-text="前往"
      cancel-button-text="下次一定"
      :icon="ElIconStar"
      :width="300"
      @confirm="handleRepo(GITHUB)"
    >
      <template #reference>
        <div
          title="Github开源仓库 - JiwuChat"
          class="h-2rem w-2rem flex-row-c-c cursor-pointer border-default rounded-full card-default shadow-sm sm:(h-1.8rem w-1.8rem)"
          v-bind="$attrs"
          @click="handleRepo(GITHUB, false)"
        >
          <CommonElImage error-root-class="hidden" src="/images/brand/github.svg" alt="GitHub" class="h-5/6 w-5/6 object-contain dark:invert" />
        </div>
      </template>
    </el-popconfirm>
    <el-popconfirm
      title="如果你觉得这个项目不错，并且您愿意的话，欢迎给项目点个Star！"
      confirm-button-text="前往"
      :icon="ElIconStar"
      cancel-button-text="下次一定"
      :width="300"
      @confirm="handleRepo(GITEE)"
    >
      <template #reference>
        <div
          href="https://gitee.com/KiWi233333/JiwuChat"
          target="_blank"
          title="Gitee开源仓库 - JiwuChat"
          class="h-2rem w-2rem flex-row-c-c cursor-pointer border-default rounded-full card-default shadow-sm sm:(h-1.8rem w-1.8rem)"
          v-bind="$attrs"
          @click="handleRepo(GITEE, false)"
        >
          <CommonElImage src="/images/brand/gitee.svg" alt="Gitee" class="h-5/6 w-5/6 object-contain" />
        </div>
      </template>
    </el-popconfirm>
    <!-- APP下载 -->
    <el-tooltip content="下载 APP" placement="bottom">
      <a
        :href="downloadUrl"
        target="_blank"
        download
        rel="noopener noreferrer"
        v-bind="$attrs"
        class="h-1.8rem flex-row-c-c btn-info-bg border-default rounded-4rem card-default pl-4 pr-6 text-xs"
      >
        <i class="i-solar-download-minimalistic-broken mr-2 p-2" />
        APP
      </a>
    </el-tooltip>
  </template>
</template>

<style scoped lang="scss"></style>
