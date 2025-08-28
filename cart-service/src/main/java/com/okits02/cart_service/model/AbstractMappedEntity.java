package com.okits02.cart_service.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.awt.*;
import java.io.Serial;
import java.time.Instant;

@MappedSuperclass
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Data
public class AbstractMappedEntity {
    @Serial
    private static final long serialVersionUID = 1L;
    @CreatedDate
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "create_at")
    private Instant createAt;
    @LastModifiedDate
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "update_at")
    private Instant updateAt;
}
