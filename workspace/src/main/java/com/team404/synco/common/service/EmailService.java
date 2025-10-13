package com.team404.synco.common.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;
    
    private static final String UPPER_CASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER_CASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String NUMBERS = "0123456789";
    private static final String SPECIAL_CHARS = "!@#$%^&*";
    private static final int PASSWORD_LENGTH = 10;
    private static final int REQUIRED_CHARS = 4;

    @Value("${spring.mail.username}")
    private String senderEmail;

    public String createTempPassword() {
        StringBuilder tempPassword = new StringBuilder();
        Random random = new Random();
        
        tempPassword.append(UPPER_CASE.charAt(random.nextInt(UPPER_CASE.length())));
        tempPassword.append(LOWER_CASE.charAt(random.nextInt(LOWER_CASE.length())));
        tempPassword.append(NUMBERS.charAt(random.nextInt(NUMBERS.length())));
        tempPassword.append(SPECIAL_CHARS.charAt(random.nextInt(SPECIAL_CHARS.length())));
        
        String allChars = UPPER_CASE + LOWER_CASE + NUMBERS + SPECIAL_CHARS;
        int remainingLength = PASSWORD_LENGTH - REQUIRED_CHARS;
        for (int i = 0; i < remainingLength; i++) {
            tempPassword.append(allChars.charAt(random.nextInt(allChars.length())));
        }
        
        return shuffleString(tempPassword.toString());
    }

    private String shuffleString(String input) {
        char[] chars = input.toCharArray();
        Random random = new Random();
        
        for (int i = chars.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }
        
        return new String(chars);
    }

    // 이메일 내용 생성 (Thymeleaf 템플릿 사용)
    private String setContext(String tempPassword) {
        Context context = new Context();
        context.setVariable("tempPassword", tempPassword);
        return templateEngine.process("tempPassword", context);
    }

    // 임시 비밀번호 이메일 폼 생성
    private MimeMessage createTempPasswordEmailForm(String email, String tempPassword) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setTo(email);
        helper.setSubject("[Synco] 임시 비밀번호가 발급되었습니다.");
        helper.setFrom(senderEmail);
        helper.setText(setContext(tempPassword), true);

        return message;
    }

    // 임시 비밀번호 이메일 발송
    public void sendTempPassword(String email, String tempPassword) {
        try {
            MimeMessage emailForm = createTempPasswordEmailForm(email, tempPassword);
            javaMailSender.send(emailForm);
        } catch (MessagingException e) {
            log.error("임시 비밀번호 이메일 발송 실패: {}", email, e);
            throw new RuntimeException("이메일 발송에 실패했습니다. 잠시 후 다시 시도해주세요.");
        }
    }
}

