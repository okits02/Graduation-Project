package com.okits02.delivery_serivce.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.lang.annotation.Target;

@Entity
@Table(name = "store_info")
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StoreInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    String pickName;
    String pickAddress;
    String pickProvince;
    String pickDistrict;
    String pickTell;
}
