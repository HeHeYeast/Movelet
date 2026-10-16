# 轻练 Movelet

荣耀 Magic8 个人居家训练助手，第一轮原生 Android 实现。Kotlin + Jetpack Compose Material 3 + Room；无登录、无服务器、无云同步。

## 功能范围
- 今日计划与真实日期的一周切换；肩背活动、力量/恢复、可选有氧分段。
- 24 个结构化动作条目，中文步骤、呼吸、错误、安全提醒、降阶/进阶、资料入口及逐条素材状态。
- 当日每项组数、次数/秒数与同类替换；可选项默认关闭。历史日期可浏览及调整，但只能在当天开始训练。
- 专注训练：逐组完成、实际剂量、休息倒计时与加时、跳过动作、教学入口、暂停、简化、提前结束及反馈。
- Room 保存训练计划、每组实际结果、历史快照；切到后台自动暂停，进程重建后仍以暂停状态恢复。
- 动作库搜索/分类/基础难度筛选；记录按日/全部历史查看。满足保守条件时给出手动加量建议，不自动改计划。

## 重要状态
当前版本 0.3.0。死虫式、鸟狗式、穿针引线、俯卧 W 新增统一人物与场景的 AI 分步示意，明确标识生成来源，支持全屏缩放。共生成/修订 7 次，剔除 3 个错误稿后纳入 4 幅图；见 [生成与逐图审核记录](docs/GENERATED-IMAGE-REVIEW.md)。其余 20 个动作尚未统一生成。个人版另保留 36 张原图、覆盖 21 项（含 1 项仅降阶参考），新图与“原图对照”分开展示。图片与文字未获真人专业签核，4 个候选动作仍排除默认计划。公开版含 4 幅生成图，不含个人第三方原图。见 [本版验证](docs/VALIDATION-0.3.0.md) 与 [原图来源](docs/IMAGES.md)。

完整说明见 [产品与架构方案](docs/DESIGN.md)、[资料与授权台账](docs/CONTENT-AUDIT.md)、[交付与验证报告](docs/DELIVERY.md)。此前旧周计划未提供，当前是依据用户明确原则设计的新保守初始计划。

## 安装
1. 将 `deliverables/Movelet-0.3.0-public-debug.apk` 传到手机，用文件管理器打开；本机个人版位于 `deliverables/personal/Movelet-0.3.0-personal-debug.apk`。
2. 仅为打开此 APK 的文件管理器允许“安装未知应用”（MagicOS 文案可能不同），安装后可关闭此权限。
3. 启动“轻练 Movelet”，阅读首次训练说明。第一轮先用轻量内容验证交互与个人耐受。
4. 本地记录保存在 App 私有目录。卸载、清除应用数据会删除记录。无云备份；第一轮未实现用户导出/恢复，长期正式使用前应补齐。

调试 APK 带开发签名，不需应用商店。后续覆盖升级需使用相同签名；请保留本项目本地构建工具目录下的 debug.keystore，不能把密钥提交到公开仓库。

## 构建
固定版本：JDK 17、Gradle 8.11.1、Android Gradle Plugin 8.9.1、Kotlin 2.1.10、Compose BOM 2025.02.00、Room 2.6.1；compileSdk/targetSdk 35，minSdk 26。

标准 Android 环境：配置 `JAVA_HOME`、`ANDROID_HOME`，执行：

```powershell
.\gradlew.bat testDebugUnitTest lintDebug assembleDebug
```

当前工作区工具位于 `.toolchain/`，已安装完整 Temurin JDK 17（现有 IntelliJ Java 缺少 jlink，未用于最终构建）；可执行：

```powershell
.\scripts\build.ps1
```

`scripts/setup.ps1` 从官方渠道安装项目内 SDK 命令行工具和 Gradle 并核对校验值。SDK 平台/Build Tools 首次安装需通过 sdkmanager 接受许可。`setup-emulator.ps1` 是本轮验证用的固定镜像下载脚本，不是运行 App 的依赖。

## 项目结构
```text
app/src/main/java/cn/movelet/app/
  MainActivity.kt   主题、主导航、今日计划与调整
  Screens.kt        动作库、详情、专注训练、记录
  Catalog.kt        独立结构化动作库与素材/来源字段
  Training.kt       周计划、剂量、训练状态与渐进建议
  Storage.kt        Room、仓库、ViewModel
app/src/test/       训练引擎及 Room 持久化测试
app/src/androidTest/ Compose 端到端交互测试
app/schemas/        Room v1 导出 schema
docs/               方案、内容台账、交付与限制
deliverables/       APK、校验值、实际运行截图
scripts/            可复现的本地构建/环境准备脚本
```

已在用户的荣耀 Magic8（Android 16 / MagicOS 10.0.0）通过无线调试完成安装与启动检查；完整训练流程、耗电与长期稳定性仍需真机验证。详情以交付报告为准。

个人版 APK 位于本机 `deliverables/personal/Movelet-0.3.0-personal-debug.apk`，不上传 GitHub；该目录中的运行截图也不上传。`Movelet-source.zip` 为 0.1.0 首轮历史快照，最新源码以 Git 仓库为准。
