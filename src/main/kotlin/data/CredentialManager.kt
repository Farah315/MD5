package org.example.data
import java.io.File
import org.example.logic.MD5

class CredentialManager {
    private val folder = File("security")
    //private val folder = File("org.example.data")
    private val usernamesFile = File(folder, "usernames.csv")
    private val passwordsFile = File(folder, "passwords.csv")
    private val md5 = MD5()

    init {
        if (!folder.exists()) {
            folder.mkdirs()
        }
        if (!usernamesFile.exists()) {
            usernamesFile.createNewFile()
            usernamesFile.appendText("HashedUsernames\n")
        }
        if (!passwordsFile.exists()) {
            passwordsFile.createNewFile()
            passwordsFile.appendText("HashedPasswords\n")
        }
    }

    fun saveCredentials(username: String, password: String) {
        val hashedUsername = md5.hash(username)
        val hashedPassword = md5.hash(password)

        usernamesFile.appendText("$hashedUsername\n")
        passwordsFile.appendText("$hashedPassword\n")
    }
}




