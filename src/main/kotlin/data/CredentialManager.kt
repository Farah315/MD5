package org.example.data
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.buffer
import org.example.logic.MD5

class CredentialManager {
    private val folderPath = "src/main/kotlin/data/security".toPath()
    private val usernamesPath = folderPath.resolve("usernames.csv")
    private val passwordsPath = folderPath.resolve("passwords.csv")
    private val fileSystem = FileSystem.SYSTEM
    private val md5 = MD5()

    init {
        if (!fileSystem.exists(folderPath)) {
            fileSystem.createDirectories(folderPath)
        }
        if (!fileSystem.exists(usernamesPath)) {
            fileSystem.write(usernamesPath) {
                writeUtf8("HashedUsernames\n")
            }
        }
        if (!fileSystem.exists(passwordsPath)) {
            fileSystem.write(passwordsPath) {
                writeUtf8("HashedPasswords\n")
            }
        }
    }

    fun saveCredentials(username: String, password: String) {
        val hashedUsername = md5.generateHash(username)
        val hashedPassword = md5.generateHash(password)

        fileSystem.appendingSink(usernamesPath).buffer().use { it.writeUtf8("$hashedUsername\n") }
        fileSystem.appendingSink(passwordsPath).buffer().use { it.writeUtf8("$hashedPassword\n") }
    }
}
