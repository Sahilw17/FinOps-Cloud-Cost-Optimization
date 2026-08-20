package com.finops.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Unit")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Unit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "unit_id")
    private Integer id;

    @Column(name = "unit_name", nullable = false, unique = true, length = 50)
    private String unitName;
}
