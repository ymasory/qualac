package qualac.common

import javax.mail.{Message, Session}
import javax.mail.internet.{ MimeMessage, InternetAddress }

import scala.util.Properties

object GMail {

  def sendMail(recipients: List[String], subject: String, body: String) {
    val host = "smtp.gmail.com"
    val from = "qualascala"
    val password = Env.getPassword()
    val props = System.getProperties
    props put ("mail.smtp.starttls.enable", "true")
    props put ("mail.smtp.host", host)
    props put ("mail.smtp.user", from)
    props put ("mail.smtp.password", password)
    props put ("mail.smtp.port", "587")
    props put ("mail.smtp.auth", "true")
    val session = Session getDefaultInstance (props, null)
    val message = new MimeMessage(session)
    message setFrom (new InternetAddress(from))
    val addresses = recipients map { r => new InternetAddress(r) }
    addresses foreach { a =>
      message addRecipient (Message.RecipientType.TO, a)
    }
    message setSubject subject
    message setText body
    message setFrom new InternetAddress("qualascala@gmail.com", "qualabot")
    val transport = session getTransport "smtp"
    transport connect (host, from, password)
    transport sendMessage (message, message.getAllRecipients())
    transport close()
  }
}
