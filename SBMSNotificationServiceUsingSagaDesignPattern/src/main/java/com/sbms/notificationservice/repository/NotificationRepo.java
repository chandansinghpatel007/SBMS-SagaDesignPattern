package com.sbms.notificationservice.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.sbms.notificationservice.entity.Notification;

@Repository
public interface NotificationRepo extends MongoRepository<Notification, String> {
}
