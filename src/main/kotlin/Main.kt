package org.example

import org.example.data.CredentialManager

fun main() {
    val credentialManager = CredentialManager()

    println("Please enter your username:")
    val username = readLine() ?: ""

    println("Please enter your password:")
    val password = readLine() ?: ""

    credentialManager.saveCredentials(username, password)

    println("Credentials saved securely!")
}



