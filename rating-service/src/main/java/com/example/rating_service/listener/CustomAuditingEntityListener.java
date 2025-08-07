package com.example.rating_service.listener;

import com.example.rating_service.model.AbstractAuditEntity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.AuditingHandler;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Configuration
public class CustomAuditingEntityListener extends AuditingEntityListener{
    public CustomAuditingEntityListener(ObjectFactory<AuditingHandler> handler) {
        super.setAuditingHandler(handler);
    }

    @Override
    @PrePersist
    public void touchForCreate(Object target){
        AbstractAuditEntity entity = (AbstractAuditEntity) target;
        if(entity.getCreateBy() == null){
            super.touchForCreate(target);
        }else {
            if(entity.getLastModifiedBy() == null){
                entity.setLastModifiedBy(entity.getCreateBy());
            }
        }
    }

    @Override
    @PreUpdate
    public void touchForUpdate(Object target){
        AbstractAuditEntity entity = (AbstractAuditEntity) target;
        if(entity.getLastModifiedBy() == null){
            super.touchForUpdate(target);
        }
    }
}
