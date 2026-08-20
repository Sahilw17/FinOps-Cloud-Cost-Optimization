package com.finops.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Metric_Name")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetricName {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "metric_name_id")
    private Integer id;

    @Column(name = "metric_name", nullable = false, unique = true, length = 100)
    private String metricName;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "unit_id", nullable = false)
    private Unit unit;
}
