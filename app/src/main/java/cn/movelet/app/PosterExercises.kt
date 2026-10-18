package cn.movelet.app

/** Separate identities keep older teaching images and history tied to their original variants. */
object PosterExercises {
    private fun source(title: String, url: String) = Source(title, url,
        "仅核对动作机制；次数和时长按用户参考图，不采用资料中的疾病适应证或训练处方。文字独立编写，未获真人专业签核。", "2026-10-18")
    private fun exercise(id: String, name: String, english: String, category: Category, muscles: String,
        sets: Int, dose: Int, unit: DoseUnit, setup: String, steps: String, breathing: String,
        mistakes: String, safety: String, easier: String, harder: String, source: Source, difficulty: String = "基础") =
        Exercise(id, name, english, category, muscles, difficulty, sets, dose, unit, setup, steps.split("|"), breathing,
            mistakes.split("|"), safety, easier, harder, listOf(source))

    val all = listOf(
        exercise("chin_tuck", "下巴微收", "Upright chin tuck", Category.MOBILITY, "颈部姿势控制", 1, 8, DoseUnit.REPS,
            "坐稳或站直，目视前方，肩膀放松。",
            "头部轻轻向后平移，像做一个很小的双下巴；眼睛仍看前方。|停约 3 秒后放松，再重复。不是低头看胸口，也不用手往后推。",
            "自然呼吸，不憋气。", "低头代替后移|咬紧牙或强压下巴|肩膀耸起",
            "出现颈痛、头晕、麻木即停；活动幅度可以很小。", "只做更小幅度，或放松肩颈自然呼吸", "先提高控制，不强求更大后移幅度",
            source("Leeds Community Healthcare NHS · Chin tucks", "https://leedscommunityhealthcare.nhs.uk/our-services-a-z/musculoskeletal-msk/neck-problems/new-and-undiagnosed-neck-problems/")),
        exercise("wall_angel", "靠墙滑手（背靠墙）", "Back-to-wall arm slide", Category.MOBILITY, "肩胛控制、肩部活动", 1, 8, DoseUnit.REPS,
            "背靠墙站稳，双脚略向前，屈肘，手朝上，手臂从舒服的低位开始。",
            "背部保持自然，不挺腰顶肋骨；双臂沿墙缓慢向上滑到无痛高度。|缓慢回落算一次；不要求手臂伸直过头，也不要强压手腕贴墙。",
            "上滑缓慢呼气，下滑吸气。", "拱腰追求高度|强扭手腕贴墙|肩前夹痛仍上滑",
            "本动作与面对墙滑臂不同。肩部过头痛时改肩胛轻收，不必完成 8 次。", "肩胛轻收，或只在低位小幅滑动", "先保持舒适控制，再小幅增加范围",
            source("Royal Papworth NHS · Wall angels · p.7", "https://royalpapworth.nhs.uk/application/files/7517/0774/1302/PI-155-Physiotherapy-information-after-pectus-surgery-vs2.pdf#page=7")),
        exercise("doorway", "门框胸肌拉伸", "Single-arm doorway chest stretch", Category.MOBILITY, "胸大肌、肩前侧", 1, 20, DoseUnit.SECONDS_EACH_SIDE,
            "站在稳固门框旁，一侧前臂轻靠门框，肘弯曲，肘部放在肩高附近或更低的舒服位置。",
            "双脚站稳，身体轻轻向前并略转离支撑手臂，到胸前出现温和牵拉即停。|自然呼吸，单侧保持目标秒数后先退回放松，再换另一侧；左右都做完才确认这一组。",
            "持续缓慢呼吸。", "用身体猛压肩关节|手臂举得过高|憋气硬拉",
            "刺痛、肩前夹痛或手指麻木时立即退出。可降低手臂或改坐姿轻展胸。", "坐姿轻展胸", "保持温和牵拉，不用外力增加幅度",
            source("University Hospitals of Leicester NHS · Chest stretch · p.4", "https://yourhealth.leicestershospitals.nhs.uk/library/csi/therapies/physiotherapy/4419-exercises-to-help-manage-your-thoracic-outlet-syndrome-tos/file")),
        exercise("reverse_lunge", "后撤箭步蹲", "Bodyweight reverse lunge", Category.LOWER, "股四头肌、臀肌", 2, 8, DoseUnit.EACH_SIDE,
            "双脚与髋同宽站立，前方留出空间，需要时靠近稳固支撑物。",
            "一脚向后迈一步，左右仍保留宽度；前脚全脚掌踩稳。|两膝缓慢弯曲到可控深度，躯干稳定，不要求后膝触地。|前脚踩地站起，把后脚收回站稳；重复或换侧，左右各完成目标次数。",
            "下降吸气，站起呼气。", "双脚踩成一条线|前膝明显内扣|后退太远失去平衡",
            "膝髋疼痛或站不稳时缩小幅度、扶稳支撑，或改椅子坐站；不为凑次数硬撑。", "椅子坐站；或扶稳支撑小幅后撤", "控制稳定后再增加幅度，不先增加负重",
            source("NASM · Squat alternatives · Reverse lunges", "https://www.nasm.org/resource-center/blog/squat-alternatives"), "进阶"),
        exercise("plank", "平板支撑", "Forearm plank", Category.CORE, "腹部稳定肌群、臀肌、肩胛稳定肌", 2, 20, DoseUnit.SECONDS,
            "前臂放垫上，双肘在肩下，先跪撑准备，眼睛看向垫面。",
            "脚尖撑地，伸腿抬起身体，让头、躯干和腿接近一条直线。|轻收腹和臀，持续呼吸；腰要塌或肩痛前就结束，不追求撑满时间。|双膝先落地再放松，记录实际保持的秒数。",
            "保持能自然呼吸，不屏气。", "塌腰或翘臀|耸肩和伸脖子|憋气撑到失控",
            "肩、腰不适立即结束。可膝盖留地减轻负担，或用死虫式的脚跟点地版本。", "跪姿前臂支撑；或死虫单脚点地", "先把 20 秒做稳，再少量增加保持时间",
            source("ACE · Front plank", "https://www.acefitness.org/resources/everyone/exercise-library/32/front-plank/")),
        exercise("jumping_jack", "开合跳", "Jumping jack", Category.CARDIO, "下肢、全身协调", 2, 30, DoseUnit.SECONDS,
            "穿适合运动的鞋，在防滑且不受障碍物影响的位置站稳，双臂放身侧。",
            "轻轻跳开双脚，同时双臂向两侧上举到肩部舒适高度。|再轻跳收脚、落下双臂，膝踝自然缓冲，保持可控节奏。|按实际完成的时间记录，不必连续做满目标时间。",
            "持续自然呼吸，不憋气。", "重重落地|膝盖向内夹|强行把手臂举过头",
            "这是含跳跃的可选有氧。膝踝或肩不适、头晕时停，改原地轻踏步；默认不加入当天训练。", "原地轻踏步，或无跳跃侧步", "先保持轻落地，不急于加速",
            source("NASM · Jumping jacks", "https://www.nasm.org/resource-center/exercise-library/jumping-jacks"), "进阶"),
        exercise("floor_burpee", "改良波比（逐脚后撤）", "Floor step-back burpee", Category.CARDIO, "全身、核心稳定", 2, 5, DoseUnit.REPS,
            "站稳，面前有足够防滑空间。本版本不做跳跃，也不做俯卧撑。",
            "屈髋屈膝下蹲，双手落在脚前方地面。|一只脚、再另一只脚向后撤到手掌支撑的斜板位，腹部保持稳定。|逐脚向前收回，站稳算一次，整个过程缓慢可控。",
            "持续呼吸，站起时呼气。", "双脚跳出或收回|塌腰|起身太急头晕",
            "仅作可选内容。肩腕腰或膝痛时不做；够不到地面时改原地踏步，不勉强弯腰下压。", "原地轻踏步；稳定高台版本需先学习其独立说明", "先把转换做稳，不默认加入跳跃或俯卧撑",
            source("ASFA · Basic step-back burpee", "https://www.americansportandfitness.com/blogs/fitness-blog/what-is-a-burpee"), "进阶"),
        exercise("walk", "轻松走动", "Easy walking", Category.CARDIO, "下肢轻活动", 1, 120, DoseUnit.SECONDS,
            "选择平整、防滑、无杂物的短路线，步幅和速度以舒适为准。",
            "自然迈步和摆臂，轻松走动，不需要追求心率或步数。|空间不足可原地小步走；到时间后缓慢停下，记录实际秒数。",
            "自然呼吸，保持能轻松说话的速度。", "走得太急|在杂物或湿滑地面绕行",
            "不适或头晕即停，必要时扶稳。这里只是短时间放松走动，不是疾病康复处方。", "坐姿交替轻抬脚", "先保持习惯，不为这段放松提高强度",
            source("NHS · Walking for health", "https://www.nhs.uk/live-well/exercise/walking-for-health/"))
    )
}
