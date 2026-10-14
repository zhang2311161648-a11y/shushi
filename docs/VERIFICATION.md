# 开源版本验证记录

版本：1.4.29。日期：2026-10-14。执行：Codex。

从现有工作目录按源码目录建立独立副本，包含源码、数据库 schema、构建脚本和公开文档；不带入本机 Git 历史、账本、签名密钥、原始手机截图、调试日志或缓存。原工作目录的源码与记录保持不变。

## 无私人签名文件的调试构建

副本内没有 keys、credentials.properties、google-services.json 或 local.properties。仅通过构建进程的环境变量提供 Android SDK 路径，JDK 使用 Android Studio 自带的 21。

```text
gradlew :app:assembleDebug :app:testDebugUnitTest :core:data:testDebugUnitTest :core:domain:testDebugUnitTest :core:model:testDebugUnitTest :feature:analysis:testDebugUnitTest --offline --no-configuration-cache --console=plain
```

结果：BUILD SUCCESSFUL，耗时 3 分 39 秒。首次构建使用本机已有的依赖缓存；这不能替代全新网络环境的依赖下载验证。

| 模块 | 测试数 | 失败 / 错误 / 跳过 |
| --- | ---: | --- |
| app | 8 | 0 / 0 / 0 |
| core:data | 107 | 0 / 0 / 0 |
| core:domain | 87 | 0 / 0 / 0 |
| core:model | 1 | 0 / 0 / 0 |
| feature:analysis | 4 | 0 / 0 / 0 |
| 合计 | 207 | 0 / 0 / 0 |

测试数来自本次构建生成的 JUnit XML。编译存在既有的弃用 API、Room 索引和 nullable collection 等警告，不影响本次构建通过。

## 图片和资料

两张演示截图来自源码中的虚构物品测试，逐张目视检查，文件来源写在 images/README.md。公开资料按当前代码核对 Android 最低版本、AI 功能移除状态、物品删除范围与完整备份/CSV 的区别。

本次未连接实体手机或模拟器，没有对 1.4.29 进行设备安装、升级或交互验收。既有截图只能说明对应页面的演示外观，不能代替本次设备测试。

Antigravity 的只读资料审查任务已准备，模型界面为 Gemini 3.8 Flash Medium；电脑界面操作被用户停止后不再继续，未取得可验收回传。最终文件整理、编译、检查和发布由 Codex 完成。

## 正式构建与 APK 检查

```text
gradlew :app:assembleRelease -PshushiSigningDirectory=<本机私人签名目录> --offline --no-configuration-cache --console=plain
```

结果：BUILD SUCCESSFUL，耗时 4 分钟。签名目录位于源码副本之外，只供本机构建读取，不进入公开源码或压缩包。

APK 通过 apksigner 核验；aapt 核对应用名「书史记账」、包名 `com.naveenapps.expensemanager`、版本 `1.4.29 / 1004029`、最低 SDK 26、目标 SDK 36。APK 权限列表无 INTERNET。发布 lint 没有新增问题，已有 baseline 过滤了 9 项；R8 报告 Kotlin metadata 兼容警告，正式构建成功，但本次未做设备运行验收。

APK SHA-256：`648497A0DEF201EF69D91AE709F239756802573EAE1426EEA93BFC8124C8CDC5`。

源码文件检查未发现私人签名文件、访问令牌、账本数据库或个人设备标识；Markdown 相对链接和 XML 解析检查通过。检查覆盖发布快照，不能保证发现所有潜在问题。

GitHub Actions 使用公开源码构建调试包与运行单元测试；其线上结果以仓库 Actions 页面为准，不与本机验证混写。
