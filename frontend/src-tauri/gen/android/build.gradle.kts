buildscript {
  repositories {
    // 阿里云镜像
    maven { url = uri("https://maven.aliyun.com/repository/public") }
    maven { url = uri("https://maven.aliyun.com/repository/google") }
    maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }

    // 原始仓库
    google()
    mavenCentral()
  }
  dependencies {
    classpath("com.android.tools.build:gradle:8.6.0")
    classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.25")
  }
}

allprojects {
  repositories {
    // 阿里云镜像
    maven { url = uri("https://maven.aliyun.com/repository/public") }
    maven { url = uri("https://maven.aliyun.com/repository/google") }
    maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }

    // 原始仓库
    google()
    mavenCentral()
  }
}

tasks.register("clean").configure {
  delete("build")
}

