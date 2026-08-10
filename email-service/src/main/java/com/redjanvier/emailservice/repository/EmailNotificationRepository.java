package com.redjanvier.emailservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.redjanvier.emailservice.entity.EmailNotification;

public interface EmailNotificationRepository extends JpaRepository<EmailNotification, Long> {
}
