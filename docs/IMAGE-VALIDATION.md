# 0.2.0 图解版验证记录

日期：2026-10-16（Asia/Shanghai）。包名仍为 `cn.movelet.app`，versionCode 2；个人版 `0.2.0-personal`，公开版 `0.2.0`。Room schema 未变。

| 检查 | 结果 |
|---|---|
| 个人版构建 | `testDebugUnitTest lintDebug assembleDebug assembleDebugAndroidTest -PpersonalMedia=true`：BUILD SUCCESSFUL |
| 公开版构建 | `testDebugUnitTest lintDebug assembleDebug`：BUILD SUCCESSFUL |
| 本地测试 | 两种构建各 20 项通过：训练/动作 16、Room 2、图片来源及资源隔离 2 |
| 静态检查 | 0 errors，11 warnings，均为已有依赖更新提示 |
| 模拟器交互 | Android 15/API 35；OK (2 tests)：完整训练记录流程、离线图片翻页/放大/还原/返回及降阶标签 |
| 图片校验 | 个人 APK 恰好包含来源清单中的 36 张图片，逐张 SHA256 一致；公开 APK 图片数 0 |
| 签名 | 两个 APK 均通过 v2 验签，1 个签名者 |
| ZIP 对齐 | 两个 APK 的 `zipalign -c -P 16 4` 均退出码 0 |
| 真机升级 | 荣耀 BKQ-AN00（用户 Magic8）、Android 16/MagicOS 10.0.0；`adb install -r` 返回 Success；启动 Status: ok；已安装版本 0.2.0-personal / versionCode 2 |
| 真机页面读取 | 成功读取新版今日动作缩略图描述与动作库元素；用户正在使用手机，后续截图检查转到模拟器。没有宣称完整真机图集测试通过 |

模拟器检查了动作库缩略图、死虫式分解图、全屏查看和俯卧 W，并核对标准字体与 130% 字体下全屏按钮避让三键导航栏。个人版截图放在本机 `deliverables/personal/screenshots/`，不上传 GitHub。没有向真机运行会生成训练记录的端到端用例，没有清除真机应用数据。导航栏修复后再次覆盖安装成功；为避免打断用户，最后这次安装未强行打开 App。

构建日志、公开版 JUnit 报告、lint 与模拟器结果在 `deliverables/validation-0.2.0/`。个人版 JUnit 报告在本机 `deliverables/personal/validation/`。

## 安装包
- 个人版（手机已升级）：`deliverables/personal/Movelet-0.2.0-personal-debug.apk`，大小 19030104 字节，SHA256 `c8d2d7672867bdafd9584c650de7116661ddf87e34bad5cafad17acc1c907a38`。
- 公开版（无个人图片）：`deliverables/Movelet-0.2.0-public-debug.apk`，大小 16712237 字节，SHA256 `9a7912e8915adf251258b133006f185095ffcf06d6294cbe3b84a732f9cda171`。
- 原 `Movelet-source.zip` 保留为 0.1.0 历史快照；最新源码在 Git 仓库。

## 测试边界
未验证完整 MagicOS 真机图集流程、完整 25 分钟耗电/帧率、长期数据积累和系统升级迁移。图片准确性检查限于来源、原始像素和文字对应；未获得真人专业签核。低分辨率原图及 3 个候选动作图片缺口详见 IMAGES.md。
