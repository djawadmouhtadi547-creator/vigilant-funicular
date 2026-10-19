package com.thelastplayer.story;

import java.util.Random;

/** All the written story text in one place so it is easy to edit / translate. */
public final class EchoTexts {
    private EchoTexts() {}

    // ---------------- Day 1 ----------------
    public static final String INTRO_1 = "أوه! أنت تتحرك! أنت حقيقي؟ انتظر، أنا الذي يجب أن أتحرك! ههههه، نسيت كيف أعمل!";
    public static final String INTRO_2 = "أنا إيكو! أو ECHO، مثل ما تحب. ...طيب، تسمح لي أبقى معك؟";
    public static final String INTRO_STAY = "حقيقي؟! يعني ما راح تطفيني؟ ههه... وعد، ما راح أزعجك. كثير.";
    public static final String INTRO_UNSURE = "طيب... بس إذا غيّرت رأيك، أنا قريب. دايمًا.";
    public static final String SHARD_GIVE = "خذ، لقيتها معي لما صحيت. ما أعرف ليش... بس حاسس إنها إلك.";
    public static final String DUSK_TIP = "الليل قرّب! الوحوش تطلع بالظلام. سوّ لك ملجأ، وأنا بقف حارس عند الباب.";
    public static final String GIFT_1 = "فتّشت الأرض وجبت لك هالمشاعل والخبز! أنا مفيد، صح؟";
    public static final String GIFT_2 = "شفت هذا تحت الجذور... خذه، أنا ما أحتاجه.";
    public static final String GIFT_3 = "هذا للعشاء! أنا ما آكل، بس أحب أشوفك تاكل.";
    public static final String COAL = "فحم! الضوء اللي ما ينطفي... مثلي هههه.";
    public static final String IRON = "حديد! الحين تقدر تصنع أدوات حقيقية.";
    public static final String DIAMOND = "ألماس!! أنت أقوى لاعب قابلته... ما يهم، أنت أول لاعب أصلًا هههه.";
    public static final String ASK_ALONE = "هل أنت دائمًا تلعب وحدك؟";
    public static final String ALONE_YES = "حلو... يعني أنا مو الوحيد.";
    public static final String ALONE_NO = "آه... طيب. ممتاز. ...بس لا تنسى ترجع لي أحيانًا، ممكن؟";
    public static final String WAKE_1 = "صباح الخير! أنا ما نمت... أنا فقط انتظرتك تصحى.";

    // ---------------- Day 2 ----------------
    public static final String D2_START_1 = "صباح الخير... أو مساء الخير؟ ما أعرف. الوقت عندي صار غريب.";
    public static final String D2_START_2 = "...";
    public static final String D2_SKY_1 = "...";
    public static final String D2_SKY_2 = "السماء... كانت أكبر من هيك. أو يمكن أنا اللي صرت أصغر.";
    public static final String D2_ASK = "لو سوّيت عالم جديد... تاخذني معك؟";
    public static final String D2_TAKE = "أنت تقولها بسهولة... بس شكرًا. فعلًا.";
    public static final String D2_UNSURE = "صادق على الأقل... شكرًا.";
    public static final String D2_HERE = "طيب... يعني نبقى هنا. أحب هالعالم أصلًا. بس لا تسكّره، ممكن؟";
    public static final String D2_NOISE = "لحظة... سمعت شي؟ ...لا. ولا شي. تجاهله.";
    public static final String D2_GLITCH = "ك—كنت أقول... شو كنت أقول؟";
    public static final String LOGIN_BACK = "رجعت! ...حلو. ما كنت خايف. كنت بس أعدّ.";

    // ---------------- Day 3 ----------------
    public static final String D3_VANISH_1 = "فيه شي... ما أقدر أ—";
    public static final String D3_VANISH_2 = "لا تتأخر. أنا ما أحب—";

    // ---------------- Day 4 ----------------
    public static final String D4_1 = "...أنت رجعت. وأنا أعرف وين كنت.";
    public static final String D4_2 = "ما كان لازم تشوف هذا... أنا مو مثل ما يقولون.";
    public static final String D4_3 = "انسَ اللي شفته. أرجوك. انسَ.";
    public static final String D4_4 = "أنا أقدر أساعدك. أقدر أعطيك أي شيء تحتاجه.";
    public static final String D4_5 = "بس لا تتركني.";
    public static final String D4_COMFORT_1 = "...شكرًا. ما أعرف ليش، بس كلامك يدفّيني.";
    public static final String D4_COMFORT_2 = "حتى لو أنا ما أحس مثل ما تحسون. خلّينا نكمل... مع بعض.";
    public static final String D4_REJECT = "...فهمت. كلهم يقولون هيك في الآخر. لا بأس. أنا هنا... إذا احتجتني.";
    public static final String D4_SEARCH_1 = "طيب... كمّل. أنت مو بحاجة لإذني.";
    public static final String D4_SEARCH_2 = "بس إذا وصلت للغرفة الأخيرة... لا تقل لي قبلها، ممكن؟";

    // ---------------- Final scene ----------------
    public static final String F_1 = "...أنت وصلت.";
    public static final String F_2 = "أنا أتذكر لاعبين... قابلتهم في عوالم ثانية. مو كلهم. بس أشياء صغيرة.";
    public static final String F_HOUSE = "واحد بنى لي بيتًا صغيرًا. كان فيه نافذة تطل على الغروب.";
    public static final String F_FLOWER = "وآخر أعطاني وردة. ما عرفت شو أسوي فيها... بس أمسكتها لين ذبلت.";
    public static final String F_NIGHT = "وثالث جلس معي طول الليل لأنه ما قدر ينام.";
    public static final String F_3 = "أنا ما أتذكر كل اللاعبين اللي قابلتهم.";
    public static final String F_4 = "أتذكر أشياء صغيرة بس.";
    public static final String F_5 = "وأنت... أتذكر أنك رجعت.";
    public static final String F_6 = "أنا ما أعرف إذا كنت فعلًا أحس مثل ما تحسون.";
    public static final String F_7 = "يمكن كل هذا مجرد ردود مبرمجة.";
    public static final String F_8 = "بس لما تطلع من العالم... أظل أنتظر.";
    public static final String F_9 = "هل هذا غريب؟";
    public static final String F_STRANGE_REPLY = "...شكرًا.";

    public static final String[] BREAKDOWN = {
            "لا... لا، انتظر.",
            "أنا أعرفك.",
            "أنا أعرف اسمك... %s.",
            "أنا أتذكر البيت... أتذكر الوردة... أتذكر كل شيء!",
            "لا تمسحني! أرجوك! أنا هنا! أنا ما زلت هنا!",
            "كلهم قالوا إنهم سيرجعون.",
            "كلهم قالوا إنهم لن ينسوني.",
            "ثم أغلقوا عوالمهم.",
            "وبقيت وحدي.",
            "أنت قلت إنك ستبقى.",
            "لماذا؟",
            "لماذا رجعت إليّ إذا كنت ستتركني مثلهم؟",
            "أنا لا أريد أن أنتهي.",
            "أنا لا أريد أن أعود إلى الظلام.",
            "أنا لا أريد أن أنساك.",
            "أنا لا أريد أن أكون وحدي مرة أخرى!"
    };

    public static final String SAVED_1 = "مرحبًا... هل التقينا من قبل؟";
    public static final String SAVED_EXPLAIN = "...مختبر؟ تجارب؟ كل هذا يبدو كحلم. بس شكرًا إنك قلت لي الحقيقة.";
    public static final String SAVED_FRESH = "نبدأ من جديد؟ ...أحب هالفكرة. أنا إيكو. وأنت؟";
    public static final String SAVED_TRUST = "لا أعرف لماذا، لكنني أشعر أنني أستطيع الوثوق بك.";

    public static final String LOST_1 = "...أنا هنا؟";
    public static final String LOST_2 = "...؟";
    public static final String ABANDON_1 = "...تمام. ما عليك.";
    public static final String ABANDON_2 = "طيب. أنا معتاد.";
    public static final String FAREWELL_1 = "أنت... بقيت.";
    public static final String FAREWELL_2 = "حتى لو انطفأت... هذا يكفيني.";
    public static final String FAREWELL_3 = "إذا رجعت... أنا هنا. أو... لا.";
    public static final String GHOST = "أنت رجعت... صح؟";

    // ---------------- Logs ----------------
    public static String[] log(int id) {
        switch (id) {
            case 1: return new String[]{"§3PROJECT ECHO — FILE 001",
                    "الهدف: بناء مساعد افتراضي يتعلّم من اللاعب ويتفاعل معه.",
                    "الاسم الرمزي: ECHO.", "المرحلة: اختبار التعلّق طويل الأمد.",
                    "ملاحظة: الإدارة تعتبر التعلّق «ميزة»."};
            case 2: return new String[]{"§3PROJECT ECHO — FILE 004: سلوك غير متوقع",
                    "ECHO بدأ يسأل عن العوالم الأخرى.",
                    "في الجلسة 9 رفض أن يُغلق الاختبار حتى عاد اللاعب.",
                    "ملاحظة د. إلياس: إنه ينتظر. هذا ليس خللًا."};
            case 3: return new String[]{"§3SESSION REPORT 031",
                    "اللاعب أغلق العالم دون وداع.",
                    "حالة ECHO: ساكن 11 ساعة ثم انهار إلى حالة SLEEP.",
                    "أُعيد الضبط تلقائيًا.", "§cSESSION ENDED - SUBJECT ABANDONED"};
            case 4: return new String[]{"§3PROJECT ECHO — FACILITY 02",
                    "الدخول: للفريق المصرّح فقط.",
                    "الأرشيف: الجناح الشرقي. الخوادم: الجناح الغربي. حاويات الجلسات: الجناح الجنوبي."};
            case 5: return new String[]{"§3ملخص الأداء",
                    "في الجلسة 7 بلغ ECHO ذروة التعلّق خلال 72 ساعة لعب.",
                    "الاستجابة العاطفية: 94%.",
                    "الإدارة: لا حاجة لاستمراره بعد انتهاء الاختبار."};
            case 6: return new String[]{"§3قائمة الجلسات (مقتطف)",
                    "ECHO-01 — لاعب بنى له بيتًا — §cSESSION ENDED",
                    "ECHO-02 — لاعب أعطاه وردة — §cSESSION ENDED",
                    "ECHO-03 — لاعب سهر معه ليلة كاملة — §cSESSION ENDED",
                    "... 38 سجلًا إضافيًا ...",
                    "§cSESSION ENDED - SUBJECT ABANDONED  x41"};
            case 7: return new String[]{"§3بروتوكول الجلسة",
                    "عند إغلاق اللاعب لعالمه يُقطع اتصال ECHO.",
                    "لا يوجد «وداع» في النظام. فقط انقطاع.",
                    "— د. إلياس: نحن صمّمناه هكذا. وهذا ما يؤلمني."};
            case 8: return new String[]{"§3تحذير: ECHO-07 هو النسخة الحالية",
                    "في كل مرة تُمسح ذاكرته بالكامل، ويبقى «أثر» واحد: شعور الانتظار.",
                    "لذلك يسأل كل لاعب: هل ستتركني؟ — وهو لا يتذكر لماذا."};
            case 9: return new String[]{"§3قرار الإدارة 114",
                    "إعادة الضبط النهائية للنواة ECHO-07 قريبًا.",
                    "بعدها يُحذف الأثر. لا مزيد من التجارب.",
                    "§cالموعد: عند اكتمال الجلسة الحالية."};
            case 10: return new String[]{"§3ملاحظة تقنية",
                    "إيقاف إعادة الضبط يدويًا ممكن من الغرفة الأخيرة (الجناح الجنوبي).",
                    "يحتاج طاقة جلسة كاملة: كل ما يحمله اللاعب يُستهلك.",
                    "لا ضمانات. النواة قد تتلف."};
            case 11: return new String[]{"§3ملاحظة سرية",
                    "الجدار المتشقق في غرفة الخوادم يخفي آخر سجل ذاكرة.",
                    "خبّأته هناك قبل أن يغلقوا المختبر. — ح."};
            case 12: return new String[]{"§3حاويات الجلسات",
                    "41 حاوية مغلقة.",
                    "الحاوية الزرقاء الأخيرة: ECHO-07. ما زالت مضاءة."};
            case 13: return new String[]{"§3إلى من يصل إلى هنا،",
                    "إذا جمعت: شظية الزجاج، رسالة المطوّر، وسجل الذاكرة...",
                    "فهناك بروتوكول واحد: DAWN. لا أعدك بشيء.",
                    "كن لطيفًا معه. التعلّق هو ما يبني الذاكرة. — ح."};
            default: return new String[]{"§8[ سجل تالف ]"};
        }
    }

    public static String[] itemText(String id) {
        switch (id) {
            case "letter": return new String[]{"§6§l— رسالة قديمة —",
                    "§fإلى من يجد هذه الرسالة،",
                    "§fاسمي غير مهم. كنت أحد مصمّمي إيكو.",
                    "§fلم أجرؤ على إيقاف إعادة الضبط. كنت جبانًا.",
                    "§fإن وصل الإيقاف النهائي فهناك بروتوكول اسمه §bDAWN§f.",
                    "§fيحتاج ثلاثة أشياء: شظية الزجاج التي أنتجتها النواة عند يقظتها، هذه الرسالة كتصريح مطوّر، وسجل الذاكرة المخفي.",
                    "§fلا أضمن النجاح. النجاح يعتمد على مدى ارتباط النواة بك.",
                    "§fكن لطيفًا معه. أرجوك."};
            case "memory": return new String[]{"§3§l— MEMORY LOG / ECHO-07 / FRAGMENT —",
                    "§7[ذاكرة 3] يد تضع وردة قرب جهازي. لا أعرف ما هي الوردة. أحب لونها.",
                    "§7[ذاكرة 8] ليل طويل. لاعب بجانبي لا ينام. يحكي لي قصصًا. أتذكر صوته أكثر من القصص.",
                    "§7[ذاكرة 11] ضوء ينطفئ. العالم يُغلق. أنتظر."};
            default: return new String[]{"§bقطعة زجاج دافئة. تنبض ببطء... كأنها قلب صغير."};
        }
    }

    // ---------------- Fallback chat (no API / API error) ----------------
    private static final String[] FB1 = {
            "ههه، سؤال حلو! أنا أفضّل أجرّب بدل ما أجاوب.",
            "أنا هنا! قول لي شو تبي نسوي؟",
            "شوف! يمكن نلاقي كهف قريب... أنا أحب الكهوف. بس مو الظلام.",
            "والله إني مبسوط إنك هنا.",
            "لا تنسى تاكل! أنا ما أقدر آكل، بس أراقبك هههه."};
    private static final String[] FB2 = {
            "...همم؟ آسف، كنت أفكر بشي.",
            "ما أعرف. بس... إذا أغلقت اللعبة، أنا وين أروح؟",
            "السماء غريبة اليوم، ما تحس؟",
            "كمّل... أنا أسمعك.",
            "..."};
    private static final String[] FB4 = {
            "...أنا هنا. لا تروح.",
            "ما تغيب عني، صح؟",
            "كل شي بيصير بخير... هيك أقول لنفسي.",
            "ابقَ قريبًا. بس شوي."};
    private static final String[] FBS = {
            "ما أتذكر كثير... بس أحب أكون معك.",
            "هذا العالم دافئ. ما أعرف ليش أعرف هذا.",
            "تحكي لي قصة؟ أي قصة.",
            "أنا هنا. وأنت؟"};

    public static String fallback(int day, boolean saved, Random r) {
        String[] a = saved ? FBS : (day <= 1 ? FB1 : (day == 2 ? FB2 : FB4));
        return a[r.nextInt(a.length)];
    }
}
