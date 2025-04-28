package org.example.logic

class MD5 {
        private val md5Constants = IntArray(64) { i ->
            (kotlin.math.abs(kotlin.math.sin((i + 1).toDouble())) * (1L shl 32)).toLong().toInt()
        }

        private val shiftAmounts = intArrayOf(
            7, 12, 17, 22,  7, 12, 17, 22,  7, 12, 17, 22,  7, 12, 17, 22,
            5,  9, 14, 20,  5,  9, 14, 20,  5,  9, 14, 20,  5,  9, 14, 20,
            4, 11, 16, 23,  4, 11, 16, 23,  4, 11, 16, 23,  4, 11, 16, 23,
            6, 10, 15, 21,  6, 10, 15, 21,  6, 10, 15, 21,  6, 10, 15, 21
        )

        fun generateHash(input: String): String {
            val inputBytes = input.toByteArray(Charsets.UTF_8)
            val bitLength = inputBytes.size * 8L

            val paddingLength = ((56 - (inputBytes.size + 1) % 64) + 64) % 64
            val paddedInput = inputBytes +
                    byteArrayOf(0x80.toByte()) +
                    ByteArray(paddingLength) +
                    convertLengthToBytes(bitLength)

            var a0 = 0x67452301
            var b0 = 0xefcdab89.toInt()
            var c0 = 0x98badcfe.toInt()
            var d0 = 0x10325476

            for (chunk in paddedInput.toList().chunked(64)) {
                val messageBlock = IntArray(16) { i ->
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

                a0 += a
                b0 += b
                c0 += c
                d0 += d
            }

            return buildString {
                append(convertToHexString(a0))
                append(convertToHexString(b0))
                append(convertToHexString(c0))
                append(convertToHexString(d0))
            }
        }

        private fun leftRotate(value: Int, bits: Int): Int =
            (value shl bits) or (value ushr (32 - bits))

        private fun convertToHexString(value: Int): String =
            "%02x%02x%02x%02x".format(
                value and 0xff,
                (value shr 8) and 0xff,
                (value shr 16) and 0xff,
                (value shr 24) and 0xff
            )

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
    }

