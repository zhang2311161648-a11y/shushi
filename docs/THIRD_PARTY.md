# 第三方来源

本项目基于 [Expense Manager](https://github.com/nkuppan/expensemanager)，作者 Naveen Kumar Kuppan，原仓库采用 Apache 2.0。本项目保留其 LICENSE、源码版权注释，并在 NOTICE 与 UPSTREAM 中说明书史改动。

依赖的直接版本与坐标由 [gradle/libs.versions.toml](../gradle/libs.versions.toml)、各模块 build.gradle.kts 与 build-logic 定义。主要依赖包括 AndroidX / Jetpack Compose / Room / DataStore / WorkManager、Kotlin 协程与序列化、Koin、Coil、Vico、OpenCSV、Gson、NaveenApps 的组件和 roomdatabasebackup。每个依赖仍受自身许可约束，Apache 2.0 的项目许可不会覆盖或替代第三方许可。

Google OSS Licenses Gradle 插件在构建中生成依赖许可元数据；发布 APK 前需保留这些元数据。公开源包不附带模型权重、外部付费素材或个人照片。

演示图来自项目中的虚构数据测试，图标来自本项目资源。图片来源与用途见 [images/README](images/README.md)。
