# 第一轮交付与验证报告（0.1.0 历史记录）

0.2.0 图解版说明见 [IMAGES.md](IMAGES.md)，新版验证见 [IMAGE-VALIDATION.md](IMAGE-VALIDATION.md)。

交付日期：2026-10-15（Asia/Shanghai）。版本：0.1.0 / versionCode 1。定位：**可安装的原生核心功能验证版**。整个产品尚未全部完成，尤其是授权高清教学素材与完整真机训练验证。

## 下载与安装
- APK：`deliverables/Movelet-0.1.0-debug.apk`，约 17.1 MB，包名 `cn.movelet.app`，应用名“轻练 Movelet”。
- 校验：`deliverables/SHA256SUMS.txt`。SHA256：`eb592c0931b29fa29e498fcbe108e55192d907b2f1e729c10bcfb010928f1503`。
- 这是带开发签名的 universal APK，含 arm64-v8a，可传到手机文件管理器中打开安装。系统询问时仅授权该文件管理器安装未知应用。
- 首次打开会展示训练与素材说明。新安装无预置训练记录，截图中的数秒训练是模拟器操作测试产生的数据，没有打包到 APK。
- 数据只在本机；卸载或清除应用数据会丢失记录。第一轮尚无导出/恢复功能。

## 阶段结果
| 阶段 | 本轮结果 | 验证/边界 |
|---|---|---|
| 1. 环境 | 空目录起步，SDK、Gradle 与完整 JDK 已补齐 | 现有 IntelliJ Java 17 缺少 jlink，最终改用项目内完整 Temurin JDK 17.0.20.1+1 |
| 2. 方案 | 产品范围、原生架构、独立动作模型、周计划、五页交互设计 | 见 DESIGN.md；未提供旧周表，所以本轮是新初始计划，不冒称已逐项审查旧表 |
| 3. 第一轮 App | 今日/七日切换、详情、动作库、专注训练、记录；Room 本地存储 | 运行在真实 Android 模拟器进程中，非网页或静态效果图 |
| 4. 教学内容 | 24 条结构化文字与来源记录；重点解释穿针、死虫、鸟狗、W | 4 条候选动作待逐项查证并排除默认计划；所有文字仍需真人专业复核；0 个授权离线图片/视频 |
| 5. 验证 | 18 项本地测试、1 条设备端到端用例、安装启动、恢复和布局检查 | 完整交互检查在 Android 35 x86_64 模拟器；荣耀 Magic8 已完成安装与启动检查 |
| 6. 交付 | APK、源码、安装说明、资料台账、测试证据与截图 | 本地可继续构建；不包含云端服务 |

## 实现细节
默认周一/三/五力量日，其他天恢复或轻活动，肩背活动每天保留；可选有氧默认关闭。周三以椅子坐站替代尚待核查的扶椅分腿蹲。上背徒手负荷有限，俯卧 W 不能当作足够阻力的拉类训练。

每个日期的计划可调组数、次数/秒数、同类动作及可选项。开始后保存独立快照。正在训练时可记录实际剂量、跳过、休息加时/跳过、暂停、查看教学、简化、提前结束。跳过不会伪装成完成；可选有氧被跳过不会否定已完成的必选训练。

训练计时精度为秒，进入后台自动暂停，重启后要求手动继续。Room 每次更新持久化，进程被突然终止时最后一个尚未写入的计时片段可能有约 1 秒偏差；这不是后台长期运行计时器。

结束时记录主观难度和疼痛反馈，满足保守条件时仅给出手动加量建议，不自动加组、不自动升阶。计划不诊断或治疗肩背疼痛。

## 可复现验证证据

最终构建：`testDebugUnitTest lintDebug assembleDebug assembleDebugAndroidTest` → **BUILD SUCCESSFUL**。

| 检查 | 结果 | 证据 |
|---|---|---|
| 训练引擎/动作数据 | 16/16 通过 | `deliverables/validation/TEST-cn.movelet.app.TrainingTest.xml` |
| Room 持久化 | 2/2 通过；关闭重开、反馈、仅一场活动训练、快照独立 | `deliverables/validation/TEST-cn.movelet.app.StorageTest.xml` |
| 静态检查 | 0 errors，11 warnings；均为固定依赖有更新版本的提示 | `deliverables/validation/lint.txt` / `lint-results-debug.html` |
| 模拟器端到端 | **OK (1 test)** | `deliverables/instrumentation-result.txt` |
| APK 签名 | v2 验签成功，1 个签名者 | `deliverables/validation/signature.txt` |
| ZIP 对齐 | `zipalign -c -P 16 4` 退出码 0 | 构建终端验证；不等于 MagicOS 16KB 内核真机验证 |
| 清单检查 | minSdk 26 / targetSdk 35；没有 INTERNET 权限 | aapt 检查；仅合并了应用自身的 signature 级 receiver 权限 |

设备端到端用例：动作库搜索死虫 → 教学页 → 回到今日 → 开始 → 完成一组 → 跳过休息 → 教学暂停 → 结束并填写反馈 → 记录中确认每侧 3 次的实际组数。

人工操作核对：七日切换，非当天不允许新建训练；英文搜索；浅/深色主题；130% 字体；强制结束进程后重开，已完成 1 组、活动时间、剩余休息保持一致且暂停。强制关闭前后 UI 证据在 `deliverables/resume-before.txt` 与 `deliverables/resume-after.txt`。

测试环境：官方 Android Emulator 37.2.12，Android 15/API 35 AOSP x86_64，Pixel 7 布局 1080×2400，WHPX 硬件加速。SDK/镜像使用 Google 官方包并校验 SHA1；Gradle、Temurin 使用官方发布并校验 SHA256。工具放在项目 `.toolchain/`，没有改动系统 Java 安装。

真机安装补充（2026-10-15）：通过无线调试连接用户的荣耀 Magic8，系统返回 HONOR / BKQ-AN00、Android 16（API 36）、MagicOS_10.0.0。覆盖安装返回 `Success`；启动返回 `Status: ok`，前台 Activity 为 `cn.movelet.app/.MainActivity`，应用进程存在，安装版本 0.1.0 / versionCode 1。未清除用户数据，未向手机运行会生成训练记录的自动化测试。

同步前再次执行 `testDebugUnitTest lintDebug assembleDebug`，结果 `BUILD SUCCESSFUL`（缓存任务复用）。

未测试：MagicOS 真机完整训练交互与后台恢复、实际肩背动作耐受、完整 25 分钟训练的耗电和帧率、长期多月数据、系统大版本升级迁移。不能由安装启动或模拟器测试推断这些项目已通过。

## 真实运行截图
- [今日](../deliverables/screenshots/01-today.png)
- [力量日浏览](../deliverables/screenshots/02-strength-plan.png)
- [动作库](../deliverables/screenshots/03-library.png)
- [死虫式教学](../deliverables/screenshots/04-dead-bug.png)
- [专注训练与休息](../deliverables/screenshots/05-focus.png)
- [记录](../deliverables/screenshots/06-history.png)
- [130% 字体](../deliverables/screenshots/07-large-text.png)
- [深色模式](../deliverables/screenshots/08-dark-mode.png)

## 下一阶段按优先级处理
1. 高清教学素材授权与真人专业复核。所有条目的图片/视频仍为空；不允许用未核实 AI 姿势图补齐。逐项来源、原始链接、变体适用范围和待核问题见 CONTENT-AUDIT.md。
2. 在 Magic8 上继续核对导航栏与字体、旋转/锁屏/后台恢复、完整训练流程、实际电量与流畅度；当前真机结果限于系统版本、安装和启动。
3. 加入用户可控的本地导出/恢复，避免卸载或换机丢失长期记录；保留无账号、无云同步边界。
4. 获得真实几次训练反馈后再调训练量，并在取得旧周表后做逐项对照；不以此应用替代疼痛评估。

## 开发维护
项目结构与标准构建命令见 README.md。固定依赖版本用于可复现的第一轮构建，更新提示未静默禁用。没有使用 destructive migration。

用于覆盖升级的调试密钥位于 `.toolchain/android-user/debug.keystore`，已被 `.gitignore` 排除。源码归档不包含密钥、SDK、Gradle 缓存和模拟器数据；换机器构建会生成不同开发签名，不能直接覆盖当前安装，需保留原密钥或先做数据迁移。
