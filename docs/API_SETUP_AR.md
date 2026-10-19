# إعداد الذكاء الاصطناعي والصوت — THE LAST PLAYER

تقدر تضبط الإعدادات من قائمة **Mods** في Forge ثم تختار **The Last Player → Config**، أو تعدل الملف التالي بعد إغلاق اللعبة:

`config/thelastplayer-common.toml`

## 1. اختيار مزود المحادثة

من مجموعة `[ai]` غيّر `provider` إلى واحد من الخيارات:

- `OPENAI`: الواجهة المتوافقة مع OpenAI، مع الحقول `endpoint` و`apiKey` و`model`.
- `GROQ`: استخدم `groqApiKey` و`groqModel`، واترك `groqEndpoint` كما هو عادةً.
- `GEMINI`: ضع مفتاح Google AI Studio في `geminiApiKey` وحدد `geminiModel`. المود يتعامل مع صيغة Gemini مباشرة.
- `CUSTOM`: لخدمة متوافقة مع OpenAI، مثل خادم محلي؛ استخدم `endpoint` و`apiKey` و`model`.

مثال Groq:

```toml
[ai]
  enabled = true
  provider = "GROQ"
  groqApiKey = "ضع_مفتاح_Groq_هنا"
  groqModel = "llama-3.3-70b-versatile"
```

مثال Gemini:

```toml
[ai]
  enabled = true
  provider = "GEMINI"
  geminiApiKey = "ضع_مفتاح_Google_AI_Studio_هنا"
  geminiModel = "gemini-2.5-flash"
```

أسماء النماذج تتغير أحيانًا؛ إذا أعطت الخدمة خطأً عن اسم النموذج، عدّل حقل النموذج إلى اسم متاح لحسابك. إنشاء المفتاح يكون من حسابك لدى المزود، ولا يأتي المفتاح مع المود.

## 2. تفعيل الصوت المولّد

ميزة الصوت تستخدم Gemini Text-to-Speech، وهي اختيارية ومعطلة افتراضيًا حتى لا ترسل طلبات صوتية من دون علمك. ضمن `[voice]`:

```toml
[voice]
  enabled = true
  apiKey = "" # اتركه فارغًا لإعادة استخدام geminiApiKey من قسم ai
  model = "gemini-2.5-flash-preview-tts"
  voiceName = "Kore"
  timeoutSeconds = 35
```

تقدر تضع مفتاحًا مستقلًا في `voice.apiKey`، أو تتركه فارغًا ليستخدم مفتاح Gemini. يدعم النظام متغير البيئة `ECHO_VOICE_API_KEY` أيضًا. الصوت الناتج يُرسل إلى اللاعب المطلوب فقط، بينما يظل المفتاح على جهة الخادم.

## 3. مجاني أم لا؟

المود لا يفرض عليك اشتراكًا أو مفتاحًا مدفوعًا، لكن مجانية Gemini/Groq والحصص وحدود الطلبات يقررها المزود والحساب والمنطقة وقد تتغير. إذا نفدت الحصة أو تعذر نموذج الصوت، تستمر اللعبة بالأصوات المسجلة الموجودة أصلًا. يُنصح بترك `voice.enabled = false` حتى تتأكد أن النموذج متاح ضمن الحصة المجانية لحسابك.

## 4. حماية المفاتيح

لا ترفع ملف `thelastplayer-common.toml` إلى Git إذا كان يحتوي على مفاتيح حقيقية. بدلًا من ذلك تقدر تستخدم متغيرات البيئة:

- `ECHO_API_KEY`
- `GROQ_API_KEY` أو `ECHO_GROQ_API_KEY`
- `GEMINI_API_KEY` أو `ECHO_GEMINI_API_KEY`
- `ECHO_VOICE_API_KEY`

في اللعب الجماعي تُضبط مفاتيح الـ API على الخادم، بينما يستقبل العميل بيانات الصوت الناتجة فقط.
