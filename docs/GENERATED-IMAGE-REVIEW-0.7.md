# 0.7.0 新增八项动作图解

2026-10-18。按用户“动作素材需要完善”的反馈，补齐参考计划新增的 8 项图解。使用内置 imagegen，以旧版死虫式为人物、服装和场景参考；没有把参考姿势直接套给不同动作。

## 交付内容

新增 8 幅原始 PNG、19 个阶段画面：6 幅双格、1 幅三格、1 幅四格。全库现在 32 幅统一生成图、69 个阶段画面。文件位于 `app/src/main/assets/generated/`，未用代码补画或改变人体姿势。完整提示词、初稿和返工输出路径见 [生成记录](generated-image-prompts-0.7.json)。

素材台账 `teaching-media.json` 现在 68 条：32 生成图和个人版专有的 36 张第三方原图。新增条目记录尺寸、SHA256、阅读顺序、图注、来源与核对范围。来源机构只用于核对动作机制，没有授权背书含义；生成图也未获真人专业签核。

## 逐张检查与处理

审核者为 Codex，检查实际生成结果中的可见肢体、支撑点、动作版本、阶段差异及图文对应。静态图不能证明实际肌肉发力、动态控制或个人适用性。

| 动作 | 处理与可见要点 | 资料核对 |
|---|---|---|
| 下巴微收 | 侧面近景，目视前方、头部轻微后移；箭头仅提示方向，不低头压胸。初稿纳入 | [Leeds NHS](https://leedscommunityhealthcare.nhs.uk/our-services-a-z/musculoskeletal-msk/neck-problems/new-and-undiagnosed-neck-problems/) 的直立头部后移机制 |
| 背靠墙滑手 | 背靠墙、低位起始、屈肘小幅上滑，未画成面对墙。初稿纳入 | [Royal Papworth NHS 第7页](https://royalpapworth.nhs.uk/application/files/7517/0774/1302/PI-155-Physiotherapy-information-after-pectus-surgery-vs2.pdf#page=7)；本 App 缩小幅度，不采用术后适应证或强制贴墙、完全过头范围 |
| 门框胸肌拉伸 | 初稿仅手掌接触，肘部悬空，不符合本 App 前臂支撑步骤，剔除。返工后肘部与前臂接触门框，胸肩小幅转离；配文提醒不能只转头、退回后再换侧 | [Leicester NHS 第4页](https://yourhealth.leicestershospitals.nhs.uk/library/csi/therapies/physiotherapy/4419-exercises-to-help-manage-your-thoracic-outlet-syndrome-tos/file)；仅核对单侧门框、屈肘与转离机制，不复制原页 AI 图或疾病处方 |
| 后撤箭步蹲 | 三格展示站稳、退步、屈膝，前脚跟落地，后膝下沉未触地；图注补齐站起与收脚。初稿纳入 | [NASM Reverse lunges](https://www.nasm.org/resource-center/blog/squat-alternatives)；图示深度不是必达目标 |
| 平板支撑 | 跪撑准备与伸腿保持对照，双前臂着垫，肘在肩下附近，脚尖支撑。初稿纳入 | [ACE Front plank](https://www.acefitness.org/resources/everyone/exercise-library/32/front-plank/)；准备与结束采用先落膝版本 |
| 开合跳 | 初稿膝部偏直，返工调整；最终仅作为并脚、分脚与手臂位置图，静态画面仍不能验证落地缓冲。图注明确不代表完整腾空过程，另以文字提示膝踝缓冲、舒适抬臂范围 | [NASM Jumping jacks](https://www.nasm.org/resource-center/exercise-library/jumping-jacks)；不套用爆发速度与时长 |
| 地面改良波比 | 四格展示撑地、后撤一脚、手掌斜板支撑、收回一脚；图注明确继续收另一脚并站起才算一次，无跳跃或俯卧撑。初稿纳入 | [ASFA Basic/Floor step-back burpee](https://www.americansportandfitness.com/blogs/fitness-blog/what-is-a-burpee) |
| 轻松走动 | 初稿两个阶段重复同侧迈步，剔除。返工改为站稳与迈一步，不声称展示左右交替；配文说明随后交替继续 | [NHS Walking](https://www.nhs.uk/live-well/exercise/walking-for-health/)；仅作为轻松短距离走动，不采用快走处方 |

## 原有素材与边界

本轮重新查看了穿针引线、死虫式、鸟狗式、俯卧 W 的已有图解，保留原文件和解释。其他旧图沿用 [0.4.0 核对记录](GENERATED-IMAGE-REVIEW-0.4.md)，没有声称本轮全部重做或获专业审核。

七天训练、组数、次数、可选有氧默认关闭、数据结构和自定义安排优先级均不变。原四个候选动作限制不变。新增图在两版 APK 离线可用，个人原图仍只进入个人构建。验证结果见 [0.7.0 验证报告](VALIDATION-0.7.0.md)。
