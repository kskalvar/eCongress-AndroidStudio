package com.mycompany.app.smtp;

import java.util.Properties;

import javax.activation.DataHandler;
import javax.mail.Session;
import javax.mail.URLName;
import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.util.ByteArrayDataSource;

import android.util.Log;

import com.sun.mail.smtp.SMTPTransport;
import com.sun.mail.util.BASE64EncoderStream;

class GMailOauthSender {

	private Session session = null;
	private String message = null;

	private SMTPTransport connectToSmtp(String host, int port, String userEmail, String oauthToken, boolean debug) throws Exception {

		Properties props = new Properties();
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.starttls.required", "true");
		props.put("mail.smtp.sasl.enable", "false");

	    /*
	      Having timeout issue with the send response, set back to using the defaults

	      props.put("mail.smtp.connectiontimeout", "1500");
		  props.put("mail.smtp.timeout", "2500");
	     */

		props.put("mail.smtp.connectiontimeout", "1500");
		props.put("mail.smtp.timeout", "2500");

		session = Session.getInstance(props);
	   	session.setDebug(debug);

		final URLName unusedUrlName = null;
		SMTPTransport transport = new SMTPTransport(session, unusedUrlName);
		
		// If the password is non-null, SMTP tries to do AUTH LOGIN.
		final String emptyPassword = null;
		transport.connect(host, port, userEmail, emptyPassword);

		byte[] response = String.format("user=%s\1auth=Bearer %s\1\1", userEmail, oauthToken).getBytes();
		response = BASE64EncoderStream.encode(response);

		transport.issueCommand("AUTH XOAUTH2 " + new String(response), 235);

		return transport;
	}

	synchronized boolean sendMail(String subject, String body, String user, String oauthToken, String recipients) {

		this.message = null;

		try {

			SMTPTransport smtpTransport = connectToSmtp("smtp.gmail.com", 587, user, oauthToken, true);

			MimeMessage message = new MimeMessage(session);
			DataHandler handler = new DataHandler(new ByteArrayDataSource(body.getBytes(), "text/plain"));
			message.setSender(new InternetAddress(user));
			message.setSubject(subject);
			message.setDataHandler(handler);
			if (recipients.indexOf(',') > 0) {
				message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients));
			} else {
				message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipients));
			}

			smtpTransport.sendMessage(message, message.getAllRecipients());
			return true;

		} catch (Exception e) {
			Log.i("GMailOauthSender", "sendmail: " + e.getMessage());
			this.message = e.getMessage();
			return false;
		}
	}


	String getMessage() {
		return message;
	}
}
