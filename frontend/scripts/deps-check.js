#!/usr/bin/env node

/**
 * 依赖检查和管理脚本
 * 参考: vue-next, vite, element-plus 等项目
 */

const { execSync } = require("node:child_process");
const fs = require("node:fs");
const path = require("node:path");

const colors = {
  reset: "\x1B[0m",
  bright: "\x1B[1m",
  red: "\x1B[31m",
  green: "\x1B[32m",
  yellow: "\x1B[33m",
  blue: "\x1B[34m",
  magenta: "\x1B[35m",
  cyan: "\x1B[36m",
};

const log = {
  info: msg => console.log(`${colors.cyan}🔍 ${msg}${colors.reset}`),
  success: msg => console.log(`${colors.green}✅ ${msg}${colors.reset}`),
  error: msg => console.log(`${colors.red}❌ ${msg}${colors.reset}`),
  warning: msg => console.log(`${colors.yellow}⚠️  ${msg}${colors.reset}`),
  title: msg => console.log(`${colors.bright}${colors.magenta}🚀 ${msg}${colors.reset}`),
};

class DependencyChecker {
  constructor() {
    this.projectRoot = path.resolve(__dirname, "..");
    this.packageJson = JSON.parse(fs.readFileSync(path.join(this.projectRoot, "package.json"), "utf8"));
  }

  /**
   * 检查过时的依赖
   */
  async checkOutdated() {
    log.title("检查过时的依赖包...");
    try {
      const output = execSync("pnpm outdated --format=json", {
        encoding: "utf8",
        cwd: this.projectRoot,
        stdio: "pipe",
      });

      if (output.trim()) {
        const outdated = JSON.parse(output);
        if (Object.keys(outdated).length > 0) {
          log.warning("发现过时的依赖包:");
          Object.entries(outdated).forEach(([name, info]) => {
            console.log(`  ${name}: ${info.current} → ${info.latest}`);
          });
        }
        else {
          log.success("所有依赖包都是最新的");
        }
      }
      else {
        log.success("所有依赖包都是最新的");
      }
    }
    catch (error) {
      log.info("没有过时的依赖包或无法检查");
    }
  }

  /**
   * 检查安全漏洞
   */
  async checkSecurity() {
    log.title("检查安全漏洞...");
    try {
      execSync("pnpm audit --audit-level moderate", {
        encoding: "utf8",
        cwd: this.projectRoot,
        stdio: "pipe",
      });
      log.success("未发现安全漏洞");
    }
    catch (error) {
      log.error("发现安全漏洞，请运行 `pnpm audit --fix` 修复");
      console.log(error.stdout);
    }
  }

  /**
   * 检查重复依赖
   */
  async checkDuplicates() {
    log.title("检查重复依赖...");
    try {
      const output = execSync("pnpm ls --depth=0 --long", {
        encoding: "utf8",
        cwd: this.projectRoot,
        stdio: "pipe",
      });

      const packages = new Map();
      const lines = output.split("\n");

      lines.forEach((line) => {
        const match = line.match(/(\S+?)@(\S+)/);
        if (match) {
          const [, name, version] = match;
          if (packages.has(name)) {
            packages.get(name).push(version);
          }
          else {
            packages.set(name, [version]);
          }
        }
      });

      const duplicates = Array.from(packages.entries()).filter(([, versions]) => versions.length > 1);

      if (duplicates.length > 0) {
        log.warning("发现重复依赖:");
        duplicates.forEach(([name, versions]) => {
          console.log(`  ${name}: ${versions.join(", ")}`);
        });
      }
      else {
        log.success("未发现重复依赖");
      }
    }
    catch (error) {
      log.error("检查重复依赖失败");
    }
  }

  /**
   * 分析包大小
   */
  async analyzeSize() {
    log.title("分析包大小...");
    try {
      // 检查是否安装了 bundle-analyzer
      const hasAnalyzer = this.packageJson.devDependencies?.["webpack-bundle-analyzer"]
        || this.packageJson.devDependencies?.["rollup-plugin-analyzer"];

      if (!hasAnalyzer) {
        log.warning("建议安装 @nuxt/devtools 或相关分析工具来分析包大小");
        return;
      }

      log.info("请运行 `pnpm build:analyze` 来分析包大小");
    }
    catch (error) {
      log.error("分析包大小失败");
    }
  }

  /**
   * 清理依赖
   */
  async cleanup() {
    log.title("清理依赖...");
    try {
      // 清理 node_modules
      if (fs.existsSync(path.join(this.projectRoot, "node_modules"))) {
        execSync("rm -rf node_modules", { cwd: this.projectRoot });
        log.success("已清理 node_modules");
      }

      // 清理 pnpm lock
      if (fs.existsSync(path.join(this.projectRoot, "pnpm-lock.yaml"))) {
        fs.unlinkSync(path.join(this.projectRoot, "pnpm-lock.yaml"));
        log.success("已清理 pnpm-lock.yaml");
      }

      // 重新安装
      log.info("重新安装依赖...");
      execSync("pnpm install", { cwd: this.projectRoot, stdio: "inherit" });
      log.success("依赖重新安装完成");
    }
    catch (error) {
      log.error(`清理失败: ${error.message}`);
    }
  }

  /**
   * 运行所有检查
   */
  async runAll() {
    await this.checkOutdated();
    await this.checkSecurity();
    await this.checkDuplicates();
    await this.analyzeSize();
  }
}

// 命令行参数处理
const command = process.argv[2];
const checker = new DependencyChecker();

switch (command) {
  case "outdated":
    checker.checkOutdated();
    break;
  case "security":
    checker.checkSecurity();
    break;
  case "duplicates":
    checker.checkDuplicates();
    break;
  case "size":
    checker.analyzeSize();
    break;
  case "cleanup":
    checker.cleanup();
    break;
  default:
    checker.runAll();
}
