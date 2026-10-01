package com.vajrax.server.auth

import com.vajrax.server.config.AppConfig
import jakarta.mail.Message
import jakarta.mail.Session
import jakarta.mail.Transport
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import java.util.Properties

/** Sends account emails (password reset). */
interface Mailer {
    suspend fun send(to: String, subject: String, body: String)
}

/** Any SMTP provider (SendGrid, Mailgun, Postmark, Workspace relay…) configured through SMTP_*. */
class SmtpMailer(private val config: AppConfig.SmtpConfig) : Mailer {
    private val session: Session = Session.getInstance(
        Properties().apply {
            put("mail.smtp.host", config.host)
            put("mail.smtp.port", config.port.toString())
            put("mail.smtp.auth", config.user.isNotEmpty().toString())
            put("mail.smtp.starttls.enable", config.startTls.toString())
            put("mail.smtp.starttls.required", config.startTls.toString())
            put("mail.smtp.connectiontimeout", "10000")
            put("mail.smtp.timeout", "10000")
        }
    )

    override suspend fun send(to: String, subject: String, body: String) = withContext(Dispatchers.IO) {
        val message = MimeMessage(session).apply {
            setFrom(InternetAddress(config.from))
            setRecipients(Message.RecipientType.TO, InternetAddress.parse(to))
            setSubject(subject, "UTF-8")
            setText(body, "UTF-8")
        }
        Transport.send(message, config.user.ifEmpty { null }, config.password.ifEmpty { null })
    }
}

/**
 * No SMTP configured. In development the email (including the link) is logged so the flow can be
 * tried; in production only the fact that a mail could not be sent is logged — never the link.
 */
class LogMailer(private val showContent: Boolean) : Mailer {
    private val log = LoggerFactory.getLogger(LogMailer::class.java)

    override suspend fun send(to: String, subject: String, body: String) {
        if (showContent) {
            log.info("DEV email to {} — {}\n{}", to, subject, body)
        } else {
            log.warn("Email not sent (SMTP_HOST not set): {}", subject)
        }
    }
}
