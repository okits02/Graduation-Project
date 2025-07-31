package com.example.rating_service.model;


import com.example.rating_service.listener.CustomAuditingEntityListener;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

import java.time.ZonedDateTime;

@MappedSuperclass
@Getter
@Setter
@EntityListeners(CustomAuditingEntityListener.class)
public class AbstractAuditEntity {
    @CreationTimestamp
    private ZonedDateTime createOn;
    @CreatedBy
    private String createBy;
    @UpdateTimestamp
    private ZonedDateTime lastModifiedOn;
    @LastModifiedBy
    private String lastModifiedBy;
}
