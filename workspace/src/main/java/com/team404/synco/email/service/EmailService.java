package com.team404.synco.email.service;

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
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    /**
     * 임시 비밀번호 생성
     * 영문 대소문자, 숫자, 특수문자를 포함한 10자리 임시 비밀번호 생성
     */
    public String createTempPassword() {
        StringBuilder tempPassword = new StringBuilder();
        Random random = new Random();
        
        // 영문 대문자, 소문자, 숫자, 특수문자 각각 최소 1개씩 포함
        String upperCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCase = "abcdefghijklmnopqrstuvwxyz";
        String numbers = "0123456789";
        String specialChars = "!@#$%^&*";
        
        // 각 카테고리에서 최소 1개씩 선택
        tempPassword.append(upperCase.charAt(random.nextInt(upperCase.length())));
        tempPassword.append(lowerCase.charAt(random.nextInt(lowerCase.length())));
        tempPassword.append(numbers.charAt(random.nextInt(numbers.length())));
        tempPassword.append(specialChars.charAt(random.nextInt(specialChars.length())));
        
        // 나머지 6자리는 랜덤으로 선택
        String allChars = upperCase + lowerCase + numbers + specialChars;
        for (int i = 0; i < 6; i++) {
            tempPassword.append(allChars.charAt(random.nextInt(allChars.length())));
        }
        
        // 섞기
        return shuffleString(tempPassword.toString());
    }

    /**
     * 문자열 섞기
     */
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

    /**
     * 이메일 내용 생성 (Thymeleaf 템플릿 사용)
     */
    private String setContext(String tempPassword) {
        Context context = new Context();
        TemplateEngine templateEngine = new TemplateEngine();
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();

        context.setVariable("tempPassword", tempPassword);

        templateResolver.setPrefix("templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCacheable(false);

        templateEngine.setTemplateResolver(templateResolver);

        return templateEngine.process("tempPassword", context);
    }

    /**
     * 임시 비밀번호 이메일 폼 생성
     */
    private MimeMessage createTempPasswordEmailForm(String email, String tempPassword) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setTo(email);
        helper.setSubject("[Synco] 임시 비밀번호가 발급되었습니다.");
        helper.setFrom(senderEmail);
        helper.setText(setContext(tempPassword), true);

        return message;
    }

    /**
     * 임시 비밀번호 이메일 발송
     */
    public void sendTempPassword(String email, String tempPassword) {
        try {
            log.info("임시 비밀번호 이메일 발송 시작: {}", email);
            MimeMessage emailForm = createTempPasswordEmailForm(email, tempPassword);
            javaMailSender.send(emailForm);
            log.info("임시 비밀번호 이메일 발송 완료: {}", email);
        } catch (MessagingException e) {
            log.error("임시 비밀번호 이메일 발송 실패: {}", email, e);
            throw new RuntimeException("이메일 발송에 실패했습니다. 잠시 후 다시 시도해주세요.");
        }
    }
}

