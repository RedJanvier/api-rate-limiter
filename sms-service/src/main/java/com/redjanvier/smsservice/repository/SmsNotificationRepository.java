package com.redjanvier.smsservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.redjanvier.smsservice.entity.SmsNotification;

public interface SmsNotificationRepository extends JpaRepository<SmsNotification, Long> {
}
