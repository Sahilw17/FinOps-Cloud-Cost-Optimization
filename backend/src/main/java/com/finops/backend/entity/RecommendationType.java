package com.finops.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Recommendation_Type")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendationType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recommendation_type_id")
    private Integer id;

    @Column(name = "recommendation_type", nullable = false, unique = true, length = 100)
    private String recommendationType;
}
