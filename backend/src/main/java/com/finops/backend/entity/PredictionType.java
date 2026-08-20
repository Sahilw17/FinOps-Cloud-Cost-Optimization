package com.finops.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Prediction_Type")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PredictionType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "prediction_type_id")
    private Integer id;

    @Column(name = "prediction_type", nullable = false, unique = true, length = 50)
    private String predictionType;
}
