# 开发入门与源码导读

这份教程面向想运行项目、学习 Compose 或参与维护的人。建议先编译调试包，再沿着「输入一笔支出 → 保存 → 更新列表」读代码。

## 1. 准备环境

安装支持项目 Android Gradle Plugin 8.13.0 的 Android Studio，在 SDK Manager 安装 Android SDK Platform 36 和对应 Build Tools。项目使用 Gradle Wrapper 8.13、Kotlin 2.2.20，Java/Kotlin 字节码目标是 17。可使用 JDK 17+；本次本地验证用 JDK 21。

克隆仓库后由 Android Studio 打开根目录，等待 Gradle 同步。需要访问 Google Maven、Maven Central、JitPack 与 Gradle 分发服务。

Android Studio 一般会生成本机的 `local.properties`。命令行构建也可设置 `ANDROID_HOME`，或在本机创建 `local.properties`，例如：

```properties
sdk.dir=C:/Users/your-name/AppData/Local/Android/Sdk
```

这个文件不提交到 Git。

## 2. 编译和安装

Windows PowerShell：

```powershell
# 如果已有 JAVA_HOME 无效，只为当前终端设置有效路径。
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:installDebug
```

macOS / Linux：

```bash
chmod +x gradlew
./gradlew :app:assembleDebug
./gradlew :app:installDebug
```

APK 位于 `app/build/outputs/apk/debug/app-debug.apk`，调试包名为 `com.naveenapps.expensemanager.debug`，最低 Android 8.0 / API 26。调试构建无需 Firebase 配置、API Key 或正式签名文件。

## 3. 项目怎么分层

| 目录 | 读什么 |
| --- | --- |
| `app/` | Application、Activity、首页导航、书史主题、物品与存钱页面 |
| `feature/transaction/` | 流水列表、新增和编辑 |
| `feature/account/`、`feature/budget/` | 账户与预算 |
| `feature/analysis/` | 日期区间、分类占比和排行 |
| `feature/settings/` | 备份恢复、应用锁和导航设置 |
| `core/model/` | 领域数据、金额、账本、物品与存钱计划 |
| `core/domain/` | UseCase：页面调用的业务操作 |
| `core/repository/`、`core/data/` | 仓库接口和实现 |
| `core/database/` | Room 实体、DAO、迁移与 schema |
| `core/datastore/`、`core/settings/` | 用户设置 |
| `build-logic/`、`gradle/` | 公共构建约定与依赖版本 |

UI 使用 Jetpack Compose / Material 3；状态由 ViewModel、Flow 和协程管理；依赖注入用 Koin；数据持久化使用 Room 与 DataStore。

## 4. 从一条流水读源码

从 `feature/transaction/` 找到创建页面和 ViewModel，跟随保存事件进入 `core/domain/` 的 transaction UseCase，再到 `core/data/` 的 TransactionRepositoryImpl 与 `core/database/` 的 TransactionDao。保存后，Flow 更新列表和余额。

金额存储与计算优先看 `MoneyCents.kt`；修改金额代码时要保留整数分的语义。物品管理从 `app/` 下的 `AnxietyAssetOverview.kt` 和 `JiangJiAnxietyReliefViewModel.kt` 开始，物品删除只影响物品表。

练习可以从改一处文案开始，再给已有页面补一个输入校验。修改数据库时必须增加迁移并验证旧数据，完整备份兼容性也要同步检查。

## 5. 运行测试

```bash
./gradlew :app:testDebugUnitTest :core:data:testDebugUnitTest :core:domain:testDebugUnitTest :core:model:testDebugUnitTest :feature:analysis:testDebugUnitTest
./gradlew :app:assembleDebugAndroidTest
# 需要已连接的模拟器或设备：
./gradlew :app:connectedDebugAndroidTest
```

本次实际执行结果见 [VERIFICATION](VERIFICATION.md)。CI 构建调试包并运行上述单元测试，不持有维护者的签名密钥。

## 6. 正式版签名

维护者的密钥不公开。自行发布请生成自己的 Android keystore，并在仓库外建立签名目录，包含 `android_keystore.jks` 与 `credentials.properties`：

```properties
KEY_ALIAS=your-key-alias
KEY_STORE_PASSWORD=your-own-store-password
KEY_PASSWORD=your-own-key-password
```

```bash
./gradlew :app:assembleRelease -PshushiSigningDirectory=/absolute/path/to/private-signing
```

Windows 同样支持这个参数。缺少签名文件时，正式版构建会明确失败，避免把 debug 签名冒充正式版。不同签名的 APK 不能直接覆盖现有安装；独立分发建议更换 applicationId，并安排数据迁移。

## 7. 提交改动

Fork 后创建分支，保持一个 PR 解决一个问题。说明触发条件、修改后的行为和实际验证结果。界面修改附使用虚构数据的截图；数据逻辑修改附针对性的测试。开始前读 [CONTRIBUTING](../CONTRIBUTING.md)。
