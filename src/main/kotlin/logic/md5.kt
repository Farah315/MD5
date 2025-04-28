package org.example.logic

class MD5 {
    private val shifts = intArrayOf(
        7, 12, 17, 22, 7, 12, 17, 22, 7, 12, 17, 22, 7, 12, 17, 22,
        5, 9, 14, 20, 5, 9, 14, 20, 5, 9, 14, 20, 5, 9, 14, 20,
        4, 11, 16, 23, 4, 11, 16, 23, 4, 11, 16, 23, 4, 11, 16, 23,
        6, 10, 15, 21, 6, 10, 15, 21, 6, 10, 15, 21, 6, 10, 15, 21
    )

    private val k= IntArray(64) { i ->
        (kotlin.math.abs(kotlin.math.sin((i + 1).toDouble())) * (1L shl 32)).toLong().toInt()
    }

    fun hash(input: String): String {
        val bytes = input.toByteArray(Charsets.UTF_8)
        val bitLength = bytes.size * 8L

        // Calculating padding length
        val paddingLength = ((56 - (bytes.size + 1) % 64) + 64) % 64
        val padded = bytes + byteArrayOf(0x80.toByte()) + ByteArray(paddingLength) + toBytes(bitLength)

        // Initialize MD5 constants
        var a0 = 0x67452301
        var b0 = 0xefcdab89.toInt()
        var c0 = 0x98badcfe.toInt()
        var d0 = 0x10325476

        for (chunk in padded.toList().chunked(64)) {
            val m = IntArray(16) { i ->
                (chunk[i * 4].toInt() and 0xff) or
                        ((chunk[i * 4 + 1].toInt() and 0xff) shl 8) or
                        ((chunk[i * 4 + 2].toInt() and 0xff) shl 16) or
                        ((chunk[i * 4 + 3].toInt() and 0xff) shl 24)
            }

            var a = a0
            var b = b0
            var c = c0
            var d = d0

            for (i in 0 until 64) {
                val f: Int
                val g: Int
                when (i) {
                    in 0..15 -> {
                        f = (b and c) or (b.inv() and d)
                        g = i
                    }
                    in 16..31 -> {
                        f = (d and b) or (d.inv() and c)
                        g = (5 * i + 1) % 16
                    }
                    in 32..47 -> {
                        f = b xor c xor d
                        g = (3 * i + 5) % 16
                    }
                    else -> {
                        f = c xor (b or d.inv())
                        g = (7 * i) % 16
                    }
                }
                val temp = d
                d = c
                c = b
                b = b + leftRotate(a + f + k[i] + m[g], shifts[i])
                a = temp
            }

            a0 += a
            b0 += b
            c0 += c
            d0 += d
        }

        return toHex(a0) + toHex(b0) + toHex(c0) + toHex(d0)
    }

    private fun toHex(n: Int): String =
        "%02x%02x%02x%02x".format(n and 0xff, (n shr 8) and 0xff, (n shr 16) and 0xff, (n shr 24) and 0xff)

    private fun leftRotate(x: Int, c: Int): Int =
        (x shl c) or (x ushr (32 - c))

    private fun toBytes(value: Long): ByteArray =
        byteArrayOf(
            (value and 0xff).toByte(),
            ((value shr 8) and 0xff).toByte(),
            ((value shr 16) and 0xff).toByte(),
            ((value shr 24) and 0xff).toByte(),
            ((value shr 32) and 0xff).toByte(),
            ((value shr 40) and 0xff).toByte(),
            ((value shr 48) and 0xff).toByte(),
            ((value shr 56) and 0xff).toByte()
        )
}


/*
@OptIn(ExperimentalStdlibApi::class)
fun File.md5(): String {
    val md = MessageDigest.getInstance("MD5")
    val digest = md.digest(this.readBytes())
    return digest.toHexString()
}
*/
