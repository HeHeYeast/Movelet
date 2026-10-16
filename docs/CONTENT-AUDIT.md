# 首轮动作资料与授权台账（历史记录）

2026-10-16 更新：以下保留 0.1.0 的历史核查结果。当前 0.2.0 图片覆盖、来源修正和个人/公开构建规则以 [IMAGES.md](IMAGES.md) 与 `teaching-media.json` 为准，不再以逐项书面授权作为个人使用图片的统一前置条件。

查阅日期：2026-10-15。以下是第一轮资料核查，**不是持证教练/物理治疗师的专业签核**。完整结构化条目在 `app/src/main/java/cn/movelet/app/Catalog.kt`，每条包含 ID、名称、类别、肌群、难度、剂量与单位、起始姿势、步骤、呼吸、错误、风险、降阶/进阶、来源 URL、查阅日期和素材状态。

## 通用素材规则
24 个条目的本地图片/视频路径均为 `null`，授权状态均为“未取得图片/视频再分发授权；未收录”。没有下载、打包或热链专业网站的图片/视频，也没有把用户截图或 AI 生成人体姿势图当作正式教材。App 中的抽象图标仅是分类图标。打开原始资料会交给手机浏览器，可能需要联网，部分境外网站在手机网络上可能不可达。

中文文字独立编写；每组剂量是第一阶段的保守设计值，不声称来自原网页的个体处方。所有条目仍待真人专业复核。文中“要点对照”只代表阅读到相关动作说明，不代表医学适用性得到验证。

## 来源索引
- S1 [NHS Sitting exercises](https://www.nhs.uk/live-well/exercise/sitting-exercises/)：颈部、坐姿转动、展胸等。
- S2 [NHS Strength exercises](https://www.nhs.uk/live-well/exercise/strength-exercises/)：墙壁俯卧撑、坐站、提踵、侧抬腿等。
- S3 [Whittington Health Back and Neck Pain](https://www.whittington.nhs.uk/?c=47047)：猫牛、穿针引线、仰卧轻点头。原页面向儿童/青少年，只参考动作机制，不移植剂量及疗效叙述。
- S4 [Summit Orthopedics Therapeutic Exercise handout](https://www.summitortho.com/wp-content/uploads/2022/02/6558_Therapeutic-Exercise-Handout_9.22_MB.pdf)：搜索索引可读第 9 页 Prone W 要点；全文读取未成功，尚需人工确认图片、版本、版权主体。
- S5 [AAOS Shoulder Conditioning](https://orthoinfo.aaos.org/en/recovery/rotator-cuff-and-shoulder-conditioning-program/)：肩胛控制原则；不是本人的康复处方。
- A1 [ACE Push-up](https://www.acefitness.org/resources/everyone/exercise-library/41/push-up/)。
- A2 [ACE Bent-knee push-up](https://www.acefitness.org/resources/everyone/exercise-library/13/bent-knee-push-up/)。
- A3 [ACE Bodyweight squat](https://www.acefitness.org/resources/everyone/exercise-library/135/bodyweight-squat/)。
- A4 [ACE Glute bridge](https://www.acefitness.org/resources/everyone/exercise-library/66/glute-bridge/)。
- A5 [ACE Supine dead bug](https://www.acefitness.org/resources/everyone/exercise-library/147/supine-dead-bug/)。
- A6 [ACE Bird dog](https://www.acefitness.org/resources/everyone/exercise-library/14/bird-dog/)。
- C1 [NHS Get fit at home](https://www.myhealthlondon.nhs.uk/be-healthier/move-more/get-fit-at-home/)：仅低冲击活动原则，不是开合步的逐步出处。
- C2 [ACE Library](https://www.acefitness.org/resources/everyone/exercise-library/)：仅后续检索入口，不作为已核实的波比教学证据。

| ID | 中文名称 | 资料 | 核查范围/剩余问题 | 默认安排 |
|---|---|---|---|---|
| neck | 颈部轻转 | S1 | 起始姿势及舒适范围对照 | 每日 |
| twist | 坐姿胸椎转动 | S1 | 稳定骨盆、缓慢旋转对照 | 每日 |
| chest | 坐姿轻展胸 | S1 | 低位展胸对照；剂量自定 | 每日 |
| scapula | 肩胛轻收 | S5 | 一般肩胛控制原则，站坐轻收为简化 | 每日 |
| cat | 小幅猫牛式 | S3 | 小幅屈伸改编，非原页完整处方 | 库内 |
| thread | 穿针引线 | S3 | 已核穿入方向；省去向上开臂阶段 | 恢复日可选、默认关 |
| wall_slide | 靠墙滑臂 | S5 | 尚未找到逐步对应证据，待复核 | 排除 |
| chin | 仰卧轻点头 | S3 | 头不离毛巾，小幅点头 | 库内 |
| prone_w | 俯卧 W 抬臂 | S4 | 索引文字已核 W 形、额垫毛巾、小幅抬前臂；全文/真人待核 | 周三 |
| pushup | 标准俯卧撑 | A1 | 动作控制要点对照 | 库内可替换 |
| incline | 斜板俯卧撑 | A1 | 依据推撑模式的降阶改编，变体待专业复核 | 力量日 |
| wall_pushup | 墙壁俯卧撑 | S2 | 支撑与推离动作对照 | 库内可替换 |
| knee_pushup | 跪姿俯卧撑 | A2 | 跪撑版本对照 | 库内可替换 |
| squat | 徒手深蹲 | A3 | 脚跟稳定、髋膝协同对照 | 周一/五 |
| sit_stand | 椅子坐站 | S2 | 稳固无轮椅、控制坐下对照 | 周三/六 |
| split | 扶椅分腿蹲 | S2 | 只有一般力量原则，逐步变体待核 | 排除 |
| bridge | 臀桥 | A4 | 屈膝抬髋、避免过伸对照 | 周一/五 |
| calf | 扶椅提踵 | S2 | 扶持提踵对照 | 周六 |
| side_leg | 站姿侧抬腿 | S2 | 支撑和侧向抬腿对照 | 库内 |
| dead_bug | 死虫式 | A5 | 对侧手脚、躯干稳定；先单脚跟点地；伸出呼气为教学节奏选择 | 周一/五 |
| bird_dog | 鸟狗式 | A6 | 对侧伸展、骨盆不旋转、不超可控高度 | 周三 |
| march | 原地轻踏步 | S1 | 参考坐姿抬腿，站姿为日常步行改编 | 有氧默认关 |
| step_jack | 无跳跃开合步 | C1 | 仅低冲击原则，逐项证据待核 | 排除 |
| burpee | 高台后撤步波比 | C2 | 仅候选教学草稿，具体变体待核 | 排除 |

## 四个重点动作的易错处
穿针引线：穿入的手从另一侧支撑臂与膝之间通过，主要让胸口旋转；不以头承重，不要求肩触地。

死虫：对侧手与脚配对；“每侧 5 次”意味着左右各 5 次；腰背稳定优先于腿伸直或触地。手臂过头不舒服可留在身侧。

鸟狗：对侧配对，肩与骨盆尽量朝地，抬高不是目标；先单腿沿地滑，再尝试同时伸手伸腿。

俯卧 W：额头垫毛巾、保留 W 形、胸腹不抬，小幅抬前臂；无法无痛执行就改肩胛轻收或停止。此动作不能替代有足够阻力的上背拉类训练。

## 后续正式素材验收
1. 取得逐条可留存的授权文件，确认是否允许离线分发、裁剪、中文标注；登记作者、版本、授权文件路径。
2. 每个动作至少拍清起始与结束姿势；对侧动作有左右标记，穿针/W 有适当侧视角。
3. 由具备资质的教练或物理治疗师审核视频、中文步骤、禁忌与降阶；填写审核人、日期和修订版。
4. 素材在真机上检查清晰度、横竖屏/缩放、离线可用性后才解除“待授权”标签。

## 安全依据
[NHS Shoulder pain](https://www.nhs.uk/symptoms/shoulder-pain/) 支持避免加重症状的活动，并对持续、加重、明显活动受限或突发严重症状寻求评估。App 不诊断疼痛来源，不承诺治疗效果。
