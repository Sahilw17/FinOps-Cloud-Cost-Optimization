package com.finops.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Alert_Type")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alert_type_id")
    private Integer id;

    @Column(name = "alert_type", nullable = false, unique = true, length = 100)
    private String alertType;
}
