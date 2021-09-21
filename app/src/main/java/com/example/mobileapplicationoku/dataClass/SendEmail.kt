package com.example.mobileapplicationoku.dataClass


import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.MimeMessage

class SendEmail(
    private var to     : String  = "",
    private var subject: String  = "",
    private var content: String  = "",
    private var isHtml : Boolean = false,
) {
    // --- UPDATE THE FOLLOWINGS -------------------------------------------------------------------

    private val username = "yangdongdong31@gmail.com"
    private val password = "iluvchenhoe123"
    private val personal = "Okie"

    // ---------------------------------------------------------------------------------------------

    private val from = "$personal<$username>"
    private var message: MimeMessage? = null

    fun to(to: String): SendEmail {
        this.to = to
        return this
    }

    fun subject(subject: String): SendEmail {
        this.subject = subject
        return this
    }

    fun content(content: String): SendEmail {
        this.content = content
        return this
    }

    fun isHtml(isHtml: Boolean = true): SendEmail {
        this.isHtml = isHtml
        return this
    }

    private fun getMessage(): MimeMessage {
        if (message == null) {
            val prop = Properties()
            prop["mail.smtp.host"] = "smtp.gmail.com"
            prop["mail.smtp.port"] = "587"
            prop["mail.smtp.starttls.enable"] = "true"
            prop["mail.smtp.auth"] = "true"

            val auth = object : Authenticator() {
                override fun getPasswordAuthentication() =
                    PasswordAuthentication(username, password)
            }

            val sess = Session.getDefaultInstance(prop, auth)

            message = MimeMessage(sess)
        }

        return message!!
    }

    fun send(callback: () -> Unit = {}) {
        val type = if (isHtml) "text/html;charset=utf-8" else "text/plain;charset=utf-8"

        val msg = getMessage()
        msg.setFrom(from)
        msg.setRecipients(Message.RecipientType.TO, to)
        msg.setSubject(subject)
        msg.setContent(content, type)

        CoroutineScope(Dispatchers.IO).launch {
            // NOTE: Use try-catch-finally block to silent runtime error
            Transport.send(msg)
            withContext(Dispatchers.Main) { callback() }
        }
    }
}