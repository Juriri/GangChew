package com.educationplatform.gangchew.common.entity;

import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.educationplatform.gangchew.common.Constant;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@MappedSuperclass
public class BaseEntity {

    @Column(name = "createdAt", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime  createdAt;

    @Column(name = "updatedAt", nullable = false)
    @UpdateTimestamp
    private LocalDateTime  updatedAt;

}