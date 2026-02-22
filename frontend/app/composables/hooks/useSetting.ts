import { getVersion } from "@tauri-apps/api/app";

/** 版本列表项（已移除服务端版本管理，列表恒为空，仅保留类型供模板使用） */
export interface VersionListItem {
  version?: string;
  notice?: string;
  createTime?: string;
  noticeSummary?: string;
  isLatest?: number;
}

/**
 * 设置 - 版本公告（已移除服务端版本管理，仅保留桌面端当前版本与检查更新）
 */
export function useSettingNotice({
  watchParamsReload,
}: {
  watchParamsReload?: boolean;
} = {
  watchParamsReload: false,
}) {
  const setting = useSettingStore();
  const showNotice = ref(false);
  const notice = ref<string>("# 暂无内容");
  const runtime = useRuntimeConfig();
  const currentVersion = computed(() => setting.isDesktop ? setting.appUploader.version : runtime.public.version);
  const showUpateNoticeLine = ref(false);

  function showVersionNotice(_version: string) {
    // 已移除服务端版本公告
  }

  async function handleCheckUpadate() {
    if (setting.isWeb)
      return;
    if (setting.isMobile) {
      return;
    }
    setting.checkUpdates(true, {
      handleOnUpload: () => {
        showUpateNoticeLine.value = false;
      },
    });
  }

  const isLoading = ref<boolean>(false);
  const isShowResult = ref<boolean>(false);
  const pageInfo = ref({
    total: 0,
    pages: 0,
    size: 0,
    current: 0,
  });
  const versionList = reactive<VersionListItem[]>([]);
  const isNoMore = computed(() => pageInfo.value.current > 0 && pageInfo.value.current >= pageInfo.value.pages);
  const isEmpty = computed(() => pageInfo.value.current > 0 && versionList.length === 0);
  const searchParams = ref<Record<string, unknown>>({});

  function reloadVersionPage() {
    versionList.length = 0;
    pageInfo.value = { total: 0, pages: 0, size: 0, current: 0 };
  }

  function loadVersionPage(_dto?: Record<string, unknown>) {
    // 已移除服务端版本列表
  }

  onMounted(async () => {
    setting.loadSystemFonts();
    if (setting.isWeb)
      return;
    const version = await getVersion();
    setting.appUploader.version = version ?? "";
    if (!setting.appUploader.isCheckUpdatateLoad) {
      setting.checkUpdates(false);
    }
  });

  return {
    showNotice,
    notice,
    currentVersion,
    showUpateNoticeLine,
    showVersionNotice,
    handleCheckUpadate,
    isNoMore,
    isEmpty,
    isLoading,
    isShowResult,
    pageInfo,
    versionList,
    searchParams,
    loadVersionPage,
    reloadVersionPage,
  };
}

/**
 * 设置 - 主题
 */
export function useSettingTheme() {
  const setting = useSettingStore();
  const themeConfigList = setting.settingPage.modeToggle.list.map(item => ({
    ...item,
    label: item.name,
    value: item.value,
  }));
  const thePostion = ref({
    clientX: 0,
    clientY: 0,
  });
  const theme = computed({
    get: () => setting.settingPage.modeToggle.value,
    set: (val: "light" | "dark" | "system") => useSettingThemeChange(val),
  });

  return {
    theme,
    themeConfigList,
    thePostion,
  };
}
