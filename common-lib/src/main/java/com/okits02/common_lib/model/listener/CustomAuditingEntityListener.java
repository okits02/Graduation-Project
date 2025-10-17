package com.okits02.common_lib.model.listener;

import com.okits02.common_lib.model.AbstractAuditEntity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.AuditingHandler;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import org.springframework.beans.factory.ObjectFactory;


@Configuration
public class CustomAuditingEntityListener extends AuditingEntityListener {
    public CustomAuditingEntityListener(ObjectFactory<AuditingHandler> handler) {
        super.setAuditingHandler(handler);
    }

    @Override
    @PrePersist
    public void touchForCreate(Object target) {
        AbstractAuditEntity entity = (AbstractAuditEntity) target;
        if (entity.getCreatedBy() == null) {
            super.touchForCreate(target);
        } else {
            if (entity.getLastModifiedBy() == null) {
                entity.setLastModifiedBy(entity.getCreatedBy());
            }
        }
    }

    @Override
    @PreUpdate
    public void touchForUpdate(Object target) {
        AbstractAuditEntity entity = (AbstractAuditEntity) target;
        if (entity.getLastModifiedBy() == null) {
            super.touchForUpdate(target);
        }
    }
}
