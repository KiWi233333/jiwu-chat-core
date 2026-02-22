<script lang="ts" setup>
import dayjs from "dayjs";

export interface UserSafeUpdateCardsProps {
  isAnim?: boolean
}

const {
  isAnim = true,
} = defineProps<UserSafeUpdateCardsProps>();

const user = useUserStore();
const setting = useSettingStore();

const getCreateTime = computed(() => dayjs(user.userInfo.createTime).format("YYYY-MM-DD") || "未知");

// 表单显示状态
const form = ref({
  showUpdatePwd: false,
  showUpdatePhone: false,
  showUpdateEmail: false,
});

// 重新加载用户信息
const isLoading = ref<boolean>(false);
async function reloadUserInfo() {
  isLoading.value = true;
  user.loadUserInfo(user.token).finally(() => isLoading.value = false);
}

watch(
  form,
  ({ showUpdatePwd, showUpdatePhone, showUpdateEmail }) => {
    if (showUpdatePwd || showUpdatePhone || showUpdateEmail) {
      reloadUserInfo();
    }
  },
  { immediate: true, deep: true },
);

</script>

<template>
  <div
    class="flex flex-col gap-3"
    style="--anima: blur-in;--anima-duration: 200ms;"
    :data-fade="isAnim"
  >
    <!-- 用户信息卡片 -->
    <div class="flex items-center gap-3 border-default-2 rounded-xl card-bg-color p-4">
      <CommonAvatar
        class="h-12 w-12 shrink-0 rounded-full"
        :src="BaseUrlImg + user.userInfo.avatar"
      />
      <div class="flex flex-1 flex-col gap-1">
        <div class="flex items-center gap-2">
          <span class="text-base text-color font-500">{{ user.userInfo.username }}</span>
          <i v-if="user.userInfo.isEmailVerified" i-solar:verified-check-bold text-sm text-theme-primary />
        </div>
        <span class="text-small text-xs">
          注册于 {{ getCreateTime }}
        </span>
      </div>
      <NuxtLink to="/user" prefetch :prefetch-on="{ visibility: true }">
        <el-button
          text
          size="small"
          class="shrink-0"
        >
          编辑资料
        </el-button>
      </NuxtLink>
    </div>

    <!-- 两列布局 -->
    <div class="grid grid-cols-1 gap-3">
      <!-- 左列：登录与安全 -->
      <div class="flex flex-col gap-4 border-default-2 rounded-xl card-bg-color p-4">
        <div class="flex flex-col gap-1">
          <h4 class="text-sm text-color font-500">
            登录与安全
          </h4>
          <p class="text-small text-xs">
            管理您的密码和验证方式
          </p>
        </div>

        <!-- 密码 -->
        <div class="flex items-center justify-between gap-3">
          <div class="flex flex-col gap-1">
            <span class="text-xs text-color">密码</span>
            <span class="text-small text-xs">············</span>
          </div>
          <el-button
            text
            size="small"
            type="primary"
            @click="form.showUpdatePwd = true"
          >
            修改
          </el-button>
        </div>

        <!-- 手机号 -->
        <div class="flex items-center justify-between gap-3">
          <div class="flex flex-col gap-1">
            <span class="text-xs text-color">手机号</span>
            <span
              class="text-small text-xs"
              :class="{ 'text-theme-danger': !user.userInfo.phone }"
            >
              {{ user.markPhone || "还未绑定" }}
            </span>
          </div>
          <el-button
            text
            size="small"
            type="primary"
            @click="form.showUpdatePhone = true"
          >
            {{ user.userInfo.phone ? "换绑" : "绑定" }}
          </el-button>
        </div>

        <!-- 邮箱 -->
        <div class="flex items-center justify-between gap-3">
          <div class="flex flex-col gap-1">
            <span class="text-xs text-color">邮箱</span>
            <span
              class="text-small text-xs"
              :class="{ 'text-theme-danger': !user.userInfo.email }"
            >
              {{ user.userInfo.email || "还未绑定" }}
            </span>
          </div>
          <el-button
            text
            size="small"
            type="primary"
            @click="form.showUpdateEmail = true"
          >
            {{ user.userInfo.email ? "修改" : "绑定" }}
          </el-button>
        </div>
      </div>
    </div>

    <!-- 退出登录 -->
    <div class="flex justify-end pt-2">
      <el-button
        type="danger"
        class="shadow"
        @click="user.exitLogin"
      >
        <i i-solar:exit-bold-duotone mr-1 text-sm />
        退出登录
      </el-button>
    </div>

    <!-- 对话框 -->
    <Teleport to="body">
      <UserSafeDialog v-model="form" />
    </Teleport>
  </div>
</template>

<style scoped lang="scss">
</style>
