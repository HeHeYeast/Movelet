# 0.2.0 个人图解版

0.4.0 已为全部 24 个动作提供统一生成示意图，见 [全库审核记录](GENERATED-IMAGE-REVIEW-0.4.md)。本页继续保留第三方原图的来源与使用边界。

本页是 0.2.0 原始图片来源记录。0.3.0 新增 4 幅 AI 示意，参见 [生成审核记录](GENERATED-IMAGE-REVIEW.md)；下文“没有 AI 图”仅描述 0.2.0，第三方原图的构建边界继续保留。

更新日期：2026-10-16。36 张独立原始图片，覆盖 21 个动作条目，其中 20 项有对应动作图，原地踏步仅配坐姿降阶参考。没有 AI 生成人体图，也没有重绘或改变示范姿势。

## 使用体验
- 今日与动作库显示对应动作缩略图；没有对应图的条目保留分类图标。
- 教学页有顺序图集、阶段说明、来源与图片数量；左右滑动或点击箭头切换。
- 点击图片或“放大查看”进入全屏；支持双指缩放、拖动、2 倍放大和还原。
- 专注训练中可查看图解，休息与教学控制优先显示。
- 图片完全离线；仅点击外部来源链接时由浏览器联网。

## 图片核对与边界
每张图片核对了原始页面或 PDF、动作版本、示范阶段，并查看了实际像素内容。此核对不等于教练或物理治疗师的专业签核，也不是个人适用性评估。

重要修正：原先引用 ACE 66 的臀桥资料实际配健身球版本，本轮排除其两张图片，改用 Whittington Health 的地面臀桥原图和来源。

死虫图来自 ACE 屈肘/屈膝版本，旁注解释与中文步骤的差异，不要求触地；穿针引线只参考原图下半部分，不要求向上开臂或头肩触地；W 原始照片已核对 Summit PDF 第 9 页；深蹲末端图片不作为必须达到的深度。

图片原生尺寸从 181×77 到 1534 像素宽不等。肩胛、斜板俯卧撑等原图较小，已在界面注明；没有通过放大或 AI 补画伪装高清。放大查看不能补回原图缺失的细节。

尚无匹配示范图片：扶椅分腿蹲、无跳跃开合步、高台后撤步波比。这三个候选条目继续排除默认计划。靠墙滑臂新增原图，仍保持候选状态，不因有图片就自动纳入计划。

## 逐项来源
完整机器可读记录：`app/src/main/assets/teaching-media.json`，包含每图路径、来源 URL、署名、原图 URL/PDF、提取说明、尺寸、SHA256、版本差异、核对日期和版权状态。

| 动作 ID | 图数 | 角色 | 来源 |
|---|---:|---|---|
| bird_dog | 3 | 对应动作 | [American Council on Exercise · bird-dog](https://www.acefitness.org/resources/everyone/exercise-library/14/bird-dog/) |
| bridge | 1 | 对应动作 | [Whittington Health NHS · Back and Neck Pain](https://www.whittington.nhs.uk/?c=47047) |
| calf | 1 | 对应动作 | [NHS · Strength exercises](https://www.nhs.uk/live-well/exercise/strength-exercises/) |
| cat | 1 | 对应动作 | [Whittington Health NHS · Back and Neck Pain](https://www.whittington.nhs.uk/?c=47047) |
| chest | 1 | 对应动作 | [NHS · Sitting exercises](https://www.nhs.uk/live-well/exercise/sitting-exercises/) |
| chin | 1 | 对应动作 | [Whittington Health NHS · Back and Neck Pain](https://www.whittington.nhs.uk/?c=47047) |
| dead_bug | 4 | 对应动作 | [American Council on Exercise · supine-dead-bug](https://www.acefitness.org/resources/everyone/exercise-library/147/supine-dead-bug/) |
| incline | 2 | 对应动作 | [U.S. Navy Fitness · Improving PRT Scores · p.17](https://www.navyfitness.org/modules/media/?do=download&id=5151bee7-b8c2-4cdd-be76-d1ad6ff416bc#page=17) |
| knee_pushup | 3 | 对应动作 | [American Council on Exercise · bent-knee-push-up](https://www.acefitness.org/resources/everyone/exercise-library/13/bent-knee-push-up/) |
| march | 1 | 降阶参考 | [NHS · Sitting exercises](https://www.nhs.uk/live-well/exercise/sitting-exercises/) |
| neck | 1 | 对应动作 | [NHS · Sitting exercises](https://www.nhs.uk/live-well/exercise/sitting-exercises/) |
| prone_w | 2 | 对应动作 | [Summit Orthopedics · Therapeutic Exercises · p.9](https://www.summitortho.com/wp-content/uploads/2022/02/6558_Therapeutic-Exercise-Handout_9.22_MB.pdf#page=9) |
| pushup | 3 | 对应动作 | [American Council on Exercise · push-up](https://www.acefitness.org/resources/everyone/exercise-library/41/push-up/) |
| scapula | 1 | 对应动作 | [Parkview Health · Exercises · p.4](https://emi.parkview.com/media/file/Exercises.pdf#page=4) |
| side_leg | 1 | 对应动作 | [NHS · Strength exercises](https://www.nhs.uk/live-well/exercise/strength-exercises/) |
| sit_stand | 1 | 对应动作 | [NHS · Strength exercises](https://www.nhs.uk/live-well/exercise/strength-exercises/) |
| squat | 5 | 对应动作及降阶 | [American Council on Exercise · bodyweight-squat](https://www.acefitness.org/resources/everyone/exercise-library/135/bodyweight-squat/)；[NHS · Strength exercises](https://www.nhs.uk/live-well/exercise/strength-exercises/) |
| thread | 1 | 对应动作 | [Whittington Health NHS · Back and Neck Pain](https://www.whittington.nhs.uk/?c=47047) |
| twist | 1 | 对应动作 | [NHS · Sitting exercises](https://www.nhs.uk/live-well/exercise/sitting-exercises/) |
| wall_pushup | 1 | 对应动作 | [NHS · Strength exercises](https://www.nhs.uk/live-well/exercise/strength-exercises/) |
| wall_slide | 1 | 对应动作 | [Wrightington, Wigan and Leigh NHS · Shoulder Level 2 · p.16](https://www.wwl.nhs.uk/leaflets/download/wwl-6214d719c41e56.12101596#page=16) |

## 个人与公开构建
- 原图仅保存在本机 `personal-media/photos/`；个人 APK 和截图仅保存在 `deliverables/personal/`。两者均由 Git 排除。
- 没有将个人使用视为已取得公开再分发许可；保留原始图片和水印，逐图记录来源。
- 默认构建不打包个人图片，且不自动下载图片。GitHub 只保存源码、来源记录及不含这些图片的公开 APK。
- 使用本机个人素材构建：

```powershell
.\scripts\build.ps1 -Tasks @('testDebugUnitTest','lintDebug','assembleDebug','-PpersonalMedia=true')
python scripts/check-media-package.py app/build/outputs/apk/debug/app-debug.apk --personal
```

公开构建省略 `-PpersonalMedia=true`；校验时省略 `--personal`。两版包名和调试签名相同，覆盖安装保留本地记录。请保留已有调试密钥；原始素材和个人 APK 不应提交到公开仓库。

## 尚待完善
- 更高清的肩胛、穿针引线、猫牛式和斜板俯卧撑图；后三个候选动作的匹配教材。
- 真人专业复核及适合个人肩背状态的调整。
- 训练视频、数据导出与恢复尚未实现。
