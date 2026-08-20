package com.finops.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "Resource_Type")
public class ResourceType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "resource_type_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "service_id", nullable = false)
    private AwsService service;

    @Column(name = "resource_type_name", nullable = false, length = 100)
    private String resourceTypeName;

    protected ResourceType() {}

    public ResourceType(AwsService service, String resourceTypeName) {
        this.service = service;
        this.resourceTypeName = resourceTypeName;
    }

    public Integer getId() {
        return id;
    }

    public AwsService getService() {
        return service;
    }

    public void setService(AwsService service) {
        this.service = service;
    }

    public String getResourceTypeName() {
        return resourceTypeName;
    }

    public void setResourceTypeName(String resourceTypeName) {
        this.resourceTypeName = resourceTypeName;
    }
}
