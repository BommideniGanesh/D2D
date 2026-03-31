package com.example.demo.userregistration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class EmailOtpService {

    @Autowired
    private EmailOtpRepository emailOtpRepository;

    @Autowired
    private JavaMailSender mailSender;

    public void sendOtp(String email) {
        String otp = generateOtp();

        EmailOtp emailOtp = emailOtpRepository.findByEmail(email).orElse(new EmailOtp());
        emailOtp.setEmail(email);
        emailOtp.setOtpCode(otp);
        emailOtp.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        emailOtp.setVerified(false);
        emailOtpRepository.save(emailOtp);

        sendEmail(email, otp);
    }

    public boolean verifyOtp(String email, String code) {
        Optional<EmailOtp> optionalOtp = emailOtpRepository.findByEmail(email);
        if (optionalOtp.isPresent()) {
            EmailOtp emailOtp = optionalOtp.get();
            if (emailOtp.getOtpCode().equals(code) && emailOtp.getExpiresAt().isAfter(LocalDateTime.now())) {
                emailOtp.setVerified(true);
                emailOtpRepository.save(emailOtp);
                return true;
            }
        }
        return false;
    }

    public boolean isEmailVerified(String email) {
        Optional<EmailOtp> optionalOtp = emailOtpRepository.findByEmail(email);
        return optionalOtp.isPresent() && optionalOtp.get().isVerified();
    }

    private String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    private void sendEmail(String to, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Your OTP for Registration");
        message.setText("Your OTP code is: " + otp + "\nIt is valid for 10 minutes.");
        mailSender.send(message);
    }
}
