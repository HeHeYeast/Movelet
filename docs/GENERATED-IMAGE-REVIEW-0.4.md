# 全部 24 个动作的统一图解

2026-10-16。0.4.0 补齐其余 20 个动作，沿用 0.3.0 的 4 个动作。全部图片由内置 `image_gen.imagegen` 生成；本批 20 次初稿、4 次修订，24 次调用。统一虚构人物、服装、色调和室内场景，视角按教学需要选择。23 幅双格图、1 幅四格图，共 50 个阶段画面。

## 交付与追溯

- 最终图：`app/src/main/assets/generated/`，共 24 幅原始 PNG；没有使用代码补画人体或改变姿势。
- 本批完整提示词和每次工具输出路径：[generated-image-prompts-0.4.json](generated-image-prompts-0.4.json)。首批见 [0.3 审核记录](GENERATED-IMAGE-REVIEW.md)。
- 台账：`app/src/main/assets/teaching-media.json`，60 条素材记录，其中 24 幅生成图、36 张个人原图。每条记录尺寸、SHA256、来源范围、核对状态及阅读顺序。
- 原图对照仍限个人构建，生成图进入两种构建。默认显示统一生成图，不把不同风格混入同一图集。

## 本批逐项检查

审核者为 Codex。逐张查看实际输出，检查可见的肢体数量、连接、支撑、方向、图文对应。表中“纳入”指可作为标明 AI 来源的辅助示意，不代表真人教练或治疗师签核。衣物遮挡、静态姿势不能证明真实肌肉发力、动态控制或适合个人疼痛状况。

| 动作 | 可见要点与处理 | 核对依据与范围 |
|---|---|---|
| 颈部轻转 | 肩与躯干保持朝前，只转头；初稿纳入 | NHS Sitting：neck rotation |
| 坐姿胸椎转动 | 初稿胸口转动不明显，剔除；修订为胸肩共同转、骨盆朝前 | NHS Sitting：upper-body twist |
| 坐姿轻展胸 | 双臂低位打开，未画成高位强拉；初稿纳入 | NHS Sitting：chest stretch |
| 肩胛轻收 | 初稿全身视角变化难辨，剔除；改上半身近景及轻收方向箭头 | Parkview 教材 p.4；箭头仅提示方向，不表示真实发力或要求强压肩胛 |
| 小幅猫牛式 | 手膝支撑，拱背与回到中立附近；初稿纳入 | Whittington：cat-camel；只核对动作机制，不采用儿科处方 |
| 靠墙滑臂 | 面对墙、双臂沿墙上滑，未变成背靠墙天使；初稿纳入 | WWL Shoulder Level 2 p.16；完整变体仍为候选 |
| 仰卧轻点头 | 头留薄毛巾上，微小点头；初稿纳入，说明变化细微 | Whittington：chin nod；不采用儿科处方 |
| 标准俯卧撑 | 掌与脚尖支撑，肘屈曲，身体整体下降；初稿纳入 | ACE Push-up |
| 斜板俯卧撑 | 双掌在固定高台，身体保持斜线；初稿纳入 | Navy Fitness 教材 p.17；台高为示例 |
| 墙壁俯卧撑 | 掌撑墙、双脚着地，屈肘整体靠近；初稿纳入 | NHS Strength：wall press-up |
| 跪姿俯卧撑 | 双膝留垫，头髋膝形成斜线，不后坐；初稿纳入 | ACE Bent-knee push-up |
| 徒手深蹲 | 双脚跟着地、膝随脚尖、适中深度；初稿纳入 | ACE Bodyweight squat；图示深度不是必须达到的目标 |
| 椅子坐站 | 椅子靠墙，前部坐稳、前倾站起；初稿纳入 | NHS Strength：sit-to-stand |
| 扶椅分腿蹲 | 固定前后站位、前脚跟着地、双膝弯曲；初稿纳入为候选 | NHS My Planned Care p.8 的分腿蹲机制；扶椅、缩小幅度属改编，不采用 ACL 康复适应证及剂量 |
| 臀桥 | 双脚着地，肩背支撑，骨盆抬起；初稿纳入 | Whittington：floor bridge；没有使用球上臀桥 |
| 扶椅提踵 | 双手扶椅、双前脚掌留地、双脚跟抬起；初稿纳入 | NHS Strength：calf raises |
| 站姿侧抬腿 | 初稿脚尖明显外转，剔除；修订降低幅度、脚尖朝前 | NHS Strength：sideways leg lift |
| 原地轻踏步 | 初稿双幅重复同侧且抬膝偏高，剔除；修订为站稳与低幅抬脚，说明落地后换侧 | NHS Warm-up：march on the spot；进一步减小幅度，不采用原文时长 |
| 无跳跃开合步 | 侧点地、低位摆臂，没有腾空；初稿纳入为候选 | NHS Balance 仅支持缓慢侧步原则；点地后收回与手臂动作是本 App 改编，并非原文完整动作 |
| 高台后撤步波比 | 四格依次：撑台、撤一脚、斜板支撑、收一脚；说明还需收回另一脚再站稳；初稿纳入为候选 | ASFA 的 Elevated Step-Back Burpee；稳定支撑、逐脚往返，无跳跃/俯卧撑 |

## 参考资料

- [NHS Sitting exercises](https://www.nhs.uk/live-well/exercise/sitting-exercises/)
- [NHS Strength exercises](https://www.nhs.uk/live-well/exercise/strength-exercises/)
- [NHS Warm-up](https://www.nhs.uk/live-well/exercise/how-to-warm-up-before-exercising/)
- [NHS Balance exercises](https://www.nhs.uk/live-well/exercise/balance-exercises/)
- [Whittington Health](https://www.whittington.nhs.uk/?c=47047)
- [Parkview Exercises p.4](https://emi.parkview.com/media/file/Exercises.pdf#page=4)
- [WWL Shoulder Level 2 p.16](https://www.wwl.nhs.uk/leaflets/download/wwl-6214d719c41e56.12101596#page=16)
- [ACE Push-up](https://www.acefitness.org/resources/everyone/exercise-library/41/push-up/)、[跪姿版本](https://www.acefitness.org/resources/everyone/exercise-library/13/bent-knee-push-up/)、[深蹲](https://www.acefitness.org/resources/everyone/exercise-library/135/bodyweight-squat/)
- [Navy Fitness 教材 p.17](https://www.navyfitness.org/modules/media/?do=download&id=5151bee7-b8c2-4cdd-be76-d1ad6ff416bc#page=17)
- [NHS My Planned Care 分腿蹲 p.8](https://www.myplannedcare.nhs.uk/wp-content/uploads/2024/01/ACLR-Targeted-Support-Offer-Template-v2.pdf#page=8)
- [ASFA Elevated Step-Back Burpee](https://www.americansportandfitness.com/blogs/fitness-blog/what-is-a-burpee)

## 保留的边界

wall_slide、split、step_jack、burpee 仍为候选，未纳入默认计划。新增或补充来源不等于获得个体适用性评估。没有改变组数、频率、渐进规则或数据库结构。所有生成图仍明确标为 AI，不宣称真人拍摄、专业机构认可或疾病治疗效果。
