package cn.movelet.app

enum class Category(val label: String) {
    MOBILITY("肩背活动"), UPPER("胸肩上背"), LOWER("腿臀力量"), CORE("核心稳定"), CARDIO("轻量有氧")
}
enum class DoseUnit(val label: String) { REPS("次"), EACH_SIDE("次 / 侧"), SECONDS("秒") }
data class Source(val title: String, val url: String, val scope: String = "动作要点参考", val checkedOn: String = "2026-10-15")
data class Media(val localPath: String? = null, val license: String = "未取得图片/视频再分发授权；未收录", val status: String = "待授权与专业复核")
data class Exercise(
    val id: String, val name: String, val english: String, val category: Category,
    val muscles: String, val difficulty: String, val sets: Int, val dose: Int, val unit: DoseUnit,
    val setup: String, val steps: List<String>, val breathing: String, val mistakes: List<String>,
    val safety: String, val easier: String, val harder: String, val sources: List<Source>,
    val reviewed: Boolean = true, val media: Media = Media()
)

object Catalog {
    private val sitting = Source("NHS · Sitting exercises", "https://www.nhs.uk/live-well/exercise/sitting-exercises/")
    private val strength = Source("NHS · Strength exercises", "https://www.nhs.uk/live-well/exercise/strength-exercises/")
    private val back = Source("Whittington Health · Back and Neck Pain", "https://www.whittington.nhs.uk/?c=47047", "仅参考动作机制；原页面向儿童，未采用其处方剂量")
    private val w = Source("Summit Orthopedics · Therapeutic Exercise · p.9", "https://www.summitortho.com/wp-content/uploads/2022/02/6558_Therapeutic-Exercise-Handout_9.22_MB.pdf", "Prone W 姿势要点；全文 PDF 需进一步人工复核")
    private val shoulder = Source("AAOS · Shoulder Conditioning", "https://orthoinfo.aaos.org/en/recovery/rotator-cuff-and-shoulder-conditioning-program/", "肩胛控制的一般原则，非个体康复处方")
    private fun ace(id: String, slug: String) = Source("ACE · Exercise Library", "https://www.acefitness.org/resources/everyone/exercise-library/$id/$slug/")
    private fun e(id: String, name: String, en: String, cat: Category, muscle: String, dose: Int, setup: String,
        steps: String, breath: String, mistakes: String, safe: String, easier: String, harder: String, source: Source,
        unit: DoseUnit = DoseUnit.REPS, sets: Int = 2, level: String = "基础", reviewed: Boolean = true) =
        Exercise(id, name, en, cat, muscle, level, sets, dose, unit, setup, steps.split("|"), breath, mistakes.split("|"), safe, easier, harder, listOf(source), reviewed)
    val all = listOf(
        e("neck", "颈部轻转", "Neck rotation", Category.MOBILITY, "颈部活动", 3,
            "坐稳，双脚着地，肩膀自然放松，目视前方。", "缓慢转头看向左侧，在舒适处停约 5 秒。|回到中间，再向右；左右各一次分别计数。",
            "自然呼吸，不屏气。", "用手强扳头部|快速绕颈或耸肩", "头晕、麻木或疼痛即停，不追求转到尽头。", "缩小转动角度", "只增加控制，不追求更大幅度", sitting, DoseUnit.EACH_SIDE, 1),
        e("twist", "坐姿胸椎转动", "Seated trunk rotation", Category.MOBILITY, "胸椎与躯干旋转肌", 5,
            "坐在稳固椅子上，脚踩地，双手轻放胸前。", "骨盆朝前，胸口缓慢转向左侧，停约 3 秒。|回正后换右侧，不用手拉椅背。",
            "转动呼气，回正吸气。", "用腰甩动|骨盆跟着转", "只在无痛范围转动；背部症状加重则停止。", "减小幅度", "舒适位置停留略久", sitting, DoseUnit.EACH_SIDE, 1),
        e("chest", "坐姿轻展胸", "Seated chest stretch", Category.MOBILITY, "胸大肌与肩前侧", 15,
            "坐稳，手臂向身体两侧低位打开。", "肩膀放松，胸口轻轻打开。|保持温和牵拉后放松，不把手臂强拉向后。",
            "缓慢均匀呼吸。", "挺腰代替展胸|肩膀向上提", "肩前侧夹痛就缩小幅度或跳过。", "手臂保持更低", "先保持舒适，不加外力", sitting, DoseUnit.SECONDS, 3),
        e("scapula", "肩胛轻收", "Gentle scapular setting", Category.MOBILITY, "菱形肌、斜方肌中下部", 8,
            "坐或站立，双臂自然垂放。", "肩胛轻轻向后靠近，保持 3 秒。|完全放松，再重复；不用力夹死。",
            "轻收时呼气。", "耸肩|腰部后仰|夹得过猛", "应是轻活动；肩背痛加重即停。", "更轻、更小幅度", "无痛后再学俯卧 W", shoulder, sets = 1),
        e("cat", "小幅猫牛式", "Cat–camel", Category.MOBILITY, "脊柱活动", 6,
            "四点跪撑，手在肩下，膝在髋下。", "慢慢把背拱起，头自然跟随。|再轻轻回到中立并略伸展，不塌到极限。",
            "拱背呼气，回落吸气。", "仰头过多|快速塌腰", "手腕或膝痛可改坐姿胸椎转动。", "坐姿胸椎转动", "保持慢速，不增加终末幅度", back, sets = 1),
        e("thread", "穿针引线", "Thread the needle", Category.MOBILITY, "胸椎旋转、肩胛周围", 4,
            "四点跪撑：双手在双肩下，膝在髋下。先确认支撑肩与手腕无痛。",
            "保留左手支撑，右手掌心朝上，从左臂与双膝之间的空隙向左滑入。|胸口随右手向左转，右肩缓慢靠近垫子；不强求肩或头触地。|停一口呼吸，用左手轻推地回到四点跪撑。|做完右侧换左侧。这里采用小幅穿入版本，不要求向天花板开臂。",
            "穿入时缓慢呼气，回位吸气。", "臀部大幅偏到一侧|用头或颈部承重|硬把肩压到地面", "肩、颈、手腕不适时改坐姿胸椎转动；不是越深越好。",
            "坐姿胸椎转动；或手只滑入一小段", "先提高控制，再小幅增加穿入距离", back, DoseUnit.EACH_SIDE, 1),
        e("wall_slide", "靠墙滑臂", "Wall slide", Category.MOBILITY, "肩胛向上旋转相关肌群", 6,
            "面对墙站立，前臂轻贴墙，肘低于肩。", "前臂缓慢向上滑至舒适高度。|再缓慢下滑，肋骨保持自然，不后仰。",
            "上滑呼气，下滑吸气。", "拱腰抬高双手|肩前夹痛仍继续", "本条待逐项专业复核，暂不加入默认计划。过头痛时跳过。", "肩胛轻收", "在无痛范围慢做", shoulder, sets = 1, reviewed = false),
        e("chin", "仰卧轻点头", "Supine chin nod", Category.MOBILITY, "颈深屈肌", 5,
            "仰卧屈膝，头下垫薄毛巾。", "像轻轻说“是”一样小幅点头，下巴略向内。|头始终留在毛巾上，保持 3 秒后放松。",
            "保持自然呼吸。", "把头抬离毛巾|用力压下巴", "颈痛或头晕即停；避免大幅压颈。", "只放松呼吸", "先保持轻柔，不增加压力", back, sets = 1),
        e("prone_w", "俯卧 W 抬臂", "Prone W raise", Category.UPPER, "菱形肌、斜方肌、肩外旋肌", 6,
            "俯卧，额头放在折叠毛巾上，腿放松；屈肘，使双臂从上方看呈 W 形。",
            "下巴自然内收，胸腹留在垫上。|肩胛温和向后下方移动，双前臂只抬离地面几厘米，保留 W 形。|停 1–2 秒，再慢慢放下。抬臂不等于抬胸，也不要求抬得高。",
            "抬起时呼气，放下吸气，不屏气。", "抬头看前方|挺腰抬胸|耸肩、肘部强推向后", "肩前夹痛或颈腰不适时停止；改坐姿肩胛轻收。无负重版本也可能不适合当前肩痛。",
            "肩胛轻收；或手臂不离地练习轻收", "无痛且动作稳定后延长停留至 3 秒，不默认加哑铃", w),
        e("pushup", "标准俯卧撑", "Push-up", Category.UPPER, "胸大肌、肱三头肌、前三角肌", 5,
            "双手略宽于肩，脚向后伸，头到脚跟成自然直线。", "屈肘缓慢下降，肘约向身体斜后方，不张成一字。|在可控幅度内推地返回，身体整体移动。",
            "下降吸气，推起呼气。", "塌腰或翘臀|脖子向前探|耸肩", "保留 2–4 次余力。肩腕痛改墙壁版本或跳过，不强撑。", "斜板俯卧撑", "慢速下降，不先追求力竭", ace("41", "push-up"), level = "进阶"),
        e("incline", "斜板俯卧撑", "Incline push-up", Category.UPPER, "胸大肌、肱三头肌", 6,
            "双手放在固定、不滑动的高台上；不用活动桌椅。双脚后退。", "从头到脚踝成直线，屈肘让胸口接近台沿。|推离高台回位，选择能保留余力的高度。",
            "下降吸气，推起呼气。", "台面不稳|腰向前塌", "为标准俯卧撑的降阶改编，变体仍待专业复核；肩腕不适改墙壁版本或跳过。", "墙壁俯卧撑", "逐步降低支撑高度", ace("41", "push-up")),
        e("wall_pushup", "墙壁俯卧撑", "Wall push-up", Category.UPPER, "胸部、手臂", 8,
            "面对墙站，双手放墙上略低于肩，脚离墙一小步。", "身体保持直线，缓慢屈肘靠近墙。|推墙回到起始位。",
            "靠近吸气，推开呼气。", "伸脖子碰墙|身体折在腰部", "确认地面防滑；即使降阶仍痛就停止。", "脚更靠近墙", "脚稍向后或稳定高台俯卧撑", strength),
        e("knee_pushup", "跪姿俯卧撑", "Bent-knee push-up", Category.UPPER, "胸部、肱三头肌", 6,
            "双膝垫软垫，双手撑地，头到膝保持直线。", "屈肘下降，胸与髋一起移动。|推起回位，臀部不后坐。",
            "下降吸气，推起呼气。", "只动头部|臀部后坐", "膝、腕、肩不适改墙壁俯卧撑。", "墙壁俯卧撑", "标准俯卧撑", ace("13", "bent-knee-push-up")),
        e("squat", "徒手深蹲", "Bodyweight squat", Category.LOWER, "股四头肌、臀肌", 8,
            "双脚约肩宽，脚尖自然微外，脚跟踩稳。", "髋膝一起弯曲，像坐向身后的椅子。|下到可控深度后踩地站起，膝盖跟随脚尖方向。",
            "下蹲吸气，站起呼气。", "膝明显内扣|脚跟离地|为蹲深而弓腰", "膝或腰痛改浅蹲/坐站；不强求大腿平行地面。", "椅子坐站", "慢下蹲或底部短停", ace("135", "bodyweight-squat")),
        e("sit_stand", "椅子坐站", "Sit to stand", Category.LOWER, "股四头肌、臀肌", 8,
            "椅子靠墙防滑，坐在前部，双脚踩稳。", "身体略前倾，脚踩地站起。|把臀部向后送，缓慢坐回。",
            "站起呼气，坐下吸气。", "重重跌坐|椅子滑动", "必要时用手辅助；膝痛缩小幅度。", "使用较高座面或手辅助", "徒手深蹲", strength),
        e("split", "扶椅分腿蹲", "Supported split squat", Category.LOWER, "股四头肌、臀肌", 6,
            "扶稳固椅背，一脚前一脚后，双脚左右留出宽度。", "保持身体稳定，两膝弯曲小幅下降。|前脚踩地站起；完成一侧后换腿。",
            "下降吸气，起身呼气。", "双脚踩成一条线|膝向内倒", "降阶改编条目，尚待逐项复核；膝痛换坐站或跳过。", "椅子坐站", "减少扶持或增加舒适深度", strength, DoseUnit.EACH_SIDE, reviewed = false),
        e("bridge", "臀桥", "Glute bridge", Category.LOWER, "臀大肌、腘绳肌", 10,
            "仰卧屈膝，脚掌踩地与髋同宽，手臂放身体两侧。", "脚跟稳定踩地，收紧臀部抬起骨盆。|到肩、髋、膝接近一线即停，慢慢放下。",
            "抬起呼气，放下吸气。", "用腰过度顶高|膝向两侧散开", "腰痛则降低高度；不要靠颈部顶地。", "减小抬髋幅度", "顶端停 2–3 秒", ace("66", "glute-bridge")),
        e("calf", "扶椅提踵", "Supported calf raise", Category.LOWER, "小腿腓肠肌、比目鱼肌", 10,
            "双脚与髋同宽，轻扶稳固椅背。", "双脚跟缓慢抬起，重心均匀分布。|停一下，再缓慢落地。",
            "抬起呼气，落下吸气。", "踝关节向外翻|弹跳借力", "踝或足痛即停，始终轻扶防跌倒。", "减小高度", "延长缓慢下落时间", strength),
        e("side_leg", "站姿侧抬腿", "Standing hip abduction", Category.LOWER, "臀中肌", 8,
            "扶稳固椅背，身体直立。", "一腿向侧面小幅抬起，脚尖保持朝前。|控制回落，完成一侧后换腿。",
            "抬起呼气。", "身体侧倒|脚尖外转代偿", "髋部痛即停，不追求高度。", "更小幅度", "顶端短暂停留", strength, DoseUnit.EACH_SIDE),
        e("dead_bug", "死虫式", "Dead bug", Category.CORE, "腹部深层稳定肌群", 5,
            "仰卧，双臂朝上；髋膝约 90°，膝在髋上方。腹部轻收，腰背保持稳定。",
            "先只练右脚跟缓慢向地面下降，熟悉腰部不拱起的范围。|稳定后，右腿向前下方伸，同时左臂向头顶方向伸；它们是对侧，不是同侧。|不用触地，也不必伸直腿。腰部将要拱起前停止，回到起始位。|换左腿与右臂。左右各 5 次表示一组总共 10 次，慢慢做。",
            "伸出时缓慢呼气，返回时吸气；能持续呼吸比固定节奏更重要。", "同手同脚移动|腰明显拱起|憋气或甩腿", "腰痛时缩小幅度或单脚跟点地；肩过头不适时双臂留在身体旁。",
            "双手放身侧，仅交替一脚跟点地", "腰稳定后逐渐伸直对侧腿，不同时增加次数", ace("147", "supine-dead-bug"), DoseUnit.EACH_SIDE),
        e("bird_dog", "鸟狗式", "Bird dog", Category.CORE, "核心稳定肌、臀肌、背部", 5,
            "四点跪撑，手在肩下、膝在髋下，眼睛看垫子，背部保持自然。",
            "腹部轻收，先将右腿沿地面向后滑，确认骨盆不扭。|稳定后右腿向后伸、左臂向前伸，最多到与躯干大致同高。|肩与骨盆都朝地面，停 2 秒；缓慢收回。|换左腿和右臂。每侧各完成目标次数，不用抬得更高。",
            "伸展时呼气，回位吸气，不屏气。", "同侧手脚一起抬|抬腿过高塌腰|骨盆翻向侧面", "手腕或肩痛可用死虫降阶版本；膝下加垫。背痛则先只滑腿或停止。",
            "只伸一条腿，脚尖留地；或死虫单脚点地", "保持稳定，停留延长到 3–5 秒", ace("14", "bird-dog"), DoseUnit.EACH_SIDE, level = "进阶"),
        e("march", "原地轻踏步", "Gentle march", Category.CARDIO, "下肢循环活动", 30,
            "站在防滑地面，附近有稳定支撑。", "交替轻抬脚，缓慢落下，不跳跃。|保持能自然说完整句子的速度。",
            "自然呼吸。", "抬膝过高|落地很重", "不适、头晕或异常气短即停。动作是日常步行的轻量改编。", "坐姿交替抬脚", "稍增加时间，先不提速", Source("NHS · Sitting exercises", sitting.url, "参考 hip marching；站姿为日常动作改编"), DoseUnit.SECONDS, 2),
        e("step_jack", "无跳跃开合步", "Step jack", Category.CARDIO, "全身低冲击活动", 30,
            "双脚站稳，手放身侧。", "右脚侧点地后收回，再换左脚。|手臂仅在肩部舒适范围摆动，可始终低于肩。",
            "持续自然呼吸。", "脚步过快失去平衡|强行举臂过头", "待逐项专业复核，不加入默认计划；肩不适只动脚。", "原地踏步", "小幅开合跳需另行评估耐受", Source("NHS · Get fit at home", "https://www.myhealthlondon.nhs.uk/be-healthier/move-more/get-fit-at-home/", "低冲击原则参考，非此动作的逐步骤证据"), DoseUnit.SECONDS, reviewed = false),
        e("burpee", "高台后撤步波比", "Elevated step-back burpee", Category.CARDIO, "全身与核心", 4,
            "面对牢固高台，保证地面防滑；无跳跃、无俯卧撑。", "双手撑台，一脚一脚向后撤到斜板支撑。|一脚一脚收回，起身站稳后再重复。",
            "持续呼吸，起身呼气。", "双脚跳出|塌腰|起身太快", "待逐项专业复核，不进默认计划；肩腕腰痛、头晕时不做。", "原地踏步", "先提高控制，不急于加入跳跃", Source("ACE · Exercise Library", "https://www.acefitness.org/resources/everyone/exercise-library/", "变体待查证，非已核实的完整教学来源"), level = "进阶", reviewed = false)
    )
    fun get(id: String) = all.first { it.id == id }
}
