

## المقدمة
خوارزمية MD5 هي دالة تجزئة (Hash Function) تأخذ مدخلاً من أي طول وتنتج خرجًا ثابت الطول (128 بت أو 32 حرفًا ست عشريًا). تم تطويرها في عام 1991 لتحل محل MD4 الأقل أمانًا.

## مكونات الخوارزمية الرئيسية

### 1. الثوابت الأولية (Initial Constants)
```kotlin
private val md5Constants = IntArray(64) { i ->
    (kotlin.math.abs(kotlin.math.sin((i + 1).toDouble())) * (1L shl 32)).toLong().toInt()
}
```
- يتم إنشاء 64 ثابتًا باستخدام دالة الجيب (sin)
- `(i + 1)` لأن الفهرس يبدأ من 0
- `(1L shl 32)` يساوي 2^32 لضمان أن الناتج 32 بت
- الغرض: توفير قيم غير خطية لزيادة تعقيد الخوارزمية

### 2. قيم الانزياح (Shift Amounts)
```kotlin
private val shiftAmounts = intArrayOf(
    7, 12, 17, 22, 7, 12, 17, 22, 7, 12, 17, 22, 7, 12, 17, 22,
    5, 9, 14, 20, 5, 9, 14, 20, 5, 9, 14, 20, 5, 9, 14, 20,
    4, 11, 16, 23, 4, 11, 16, 23, 4, 11, 16, 23, 4, 11, 16, 23,
    6, 10, 15, 21, 6, 10, 15, 21, 6, 10, 15, 21, 6, 10, 15, 21
)
```
- تحدد عدد مرات الانزياح الدائري (rotation) في كل خطوة
- مقسمة إلى 4 مجموعات (16 قيمة لكل مجموعة)
- الغرض: زيادة تشتيت البيانات وعدم الانتظام

### 3. معالجة المدخلات (Input Processing)

#### أ. حساب طول الحشو (Padding)
```kotlin
val paddingLength = ((56 - (inputBytes.size + 1) % 64) + 64) % 64
```
- تضمن أن الطول الكلي بعد الحشو يكون congruent إلى 56 modulo 64
- `+1` لحساب البايت الإضافي (0x80) الذي سيضاف

#### ب. بناء الرسالة الممددة
```kotlin
val paddedInput = inputBytes +
        byteArrayOf(0x80.toByte()) +
        ByteArray(paddingLength) +
        convertLengthToBytes(bitLength)
```
- `0x80`: بايت البداية (10000000 في二进制)
- بايتات الصفر: حشو حتى الطول المطلوب
- `convertLengthToBytes`: طول الرسالة الأصلي (64 بت) مضافًا في النهاية

### 4. المتغيرات الأولية (Initial Hash Values)
```kotlin
var a0 = 0x67452301
var b0 = 0xefcdab89.toInt()
var c0 = 0x98badcfe.toInt()
var d0 = 0x10325476
```
- قيم أولية عشوائية ظاهريًا (لكن محددة في معيار MD5)
- تمثل سجل 32 بت A, B, C, D
- ستتم معالجتها في كل جولة

### 5. معالجة الكتل (Chunk Processing)

#### أ. تقسيم الكتلة إلى كلمات 32 بت
```kotlin
val messageBlock = IntArray(16) { i ->
    (chunk[i * 4].toInt() and 0xff) or
            ((chunk[i * 4 + 1].toInt() and 0xff) shl 8) or
            ((chunk[i * 4 + 2].toInt() and 0xff) shl 16) or
            ((chunk[i * 4 + 3].toInt() and 0xff) shl 24)
}
```
- يحول كل 4 بايتات إلى كلمة 32 بت
- `and 0xff` يضمن معالجة البايت كقيمة غير موقعة

#### ب. الجولات الأربع (64 خطوة)
```kotlin
for (i in 0 until 64) {
    val (f, g) = when (i) {
        in 0..15 -> ((b and c) or (b.inv() and d)) to i
        in 16..31 -> ((d and b) or (d.inv() and c)) to (5 * i + 1) % 16
        in 32..47 -> (b xor c xor d) to (3 * i + 5) % 16
        else      -> (c xor (b or d.inv())) to (7 * i) % 16
    }

    val temp = d
    d = c
    c = b
    b += leftRotate(a + f + md5Constants[i] + messageBlock[g], shiftAmounts[i])
    a = temp
}
```
- كل جولة لها دالة مختلفة:
  - الجولة 1 (0-15): F(b,c,d) = (b AND c) OR (NOT b AND d)
  - الجولة 2 (16-31): G(b,c,d) = (d AND b) OR (NOT d AND c)
  - الجولة 3 (32-47): H(b,c,d) = b XOR c XOR d
  - الجولة 4 (48-63): I(b,c,d) = c XOR (b OR NOT d)
- `g` تحدد أي كلمة من الكتلة تستخدم
- `leftRotate` تنفذ دورانًا دائريًا

### 6. دوال المساعدة

#### أ. الدوران الدائري (Left Rotate)
```kotlin
private fun leftRotate(value: Int, bits: Int): Int =
    (value shl bits) or (value ushr (32 - bits))
```
- تدوير البتات إلى اليسار مع نقل البتات الخارجة إلى الطرف الآخر

#### ب. تحويل إلى HEX
```kotlin
private fun convertToHexString(value: Int): String =
    "%02x%02x%02x%02x".format(
        value and 0xff,
        (value shr 8) and 0xff,
        (value shr 16) and 0xff,
        (value shr 24) and 0xff
    )
```
- يحول كل بايت من القيمة 32 بت إلى تمثيل سداسي عشري
- `%02x` يضمن أن كل بايت يمثل بحرفين

#### ج. تحويل الطول إلى بايتات
```kotlin
private fun convertLengthToBytes(length: Long): ByteArray =
    byteArrayOf(
        (length and 0xff).toByte(),
        ((length shr 8) and 0xff).toByte(),
        ((length shr 16) and 0xff).toByte(),
        ((length shr 24) and 0xff).toByte(),
        ((length shr 32) and 0xff).toByte(),
        ((length shr 40) and 0xff).toByte(),
        ((length shr 48) and 0xff).toByte(),
        ((length shr 56) and 0xff).toByte()
    )
```
- يحول الطول 64 بت إلى 8 بايتات (Little-endian)

## لماذا هذه التصميمات؟

1. **الدوال غير الخطية (F, G, H, I)**:
   - تمنع إنشاء علاقات خطية بين المدخلات والمخرجات
   - كل دالة تضيف نمطًا مختلفًا من عدم الانتظام

2. **الثوابت من دالة الجيب**:
   - توفر قيمًا تبدو عشوائية دون الحاجة إلى تخزينها
   - تضمن عدم وجود نمطية في الثوابت

3. **الإزاحات الدائرية المختلفة**:
   - تكسر أي أنماط قد تنشأ من تكرار العمليات
   - تزيد من تشتت البتات

4. **إضافة طول الرسالة**:
   - تجعل الهجمات بطول الرسالة المختارة أكثر صعوبة
   - تضمن أن الرسائل المختلفة الطول ستعطي قيم تجزئة مختلفة حتى لو كانت متشابهة

5. **المتغيرات الأولية**:
   - تم اختيارها بعناية لبدء عملية الخلط بقيم غير صفرية
   - تساعد في تجنب تصادمات التجزئة

## نقاط الضعف في MD5

رغم أن هذا التصميم كان آمنًا في التسعينيات، إلا أنه الآن يعتبر غير آمن بسبب:
1. هجمات التصادم (Collision Attacks) التي تجد رسائل مختلفة تعطي نفس التجزئة
2. هجمات ما قبل الصورة (Preimage Attacks) أصبحت أسهل
3. التقدم في قوة الحوسبة جعل الهجمات الغاشمة أكثر فعالية
