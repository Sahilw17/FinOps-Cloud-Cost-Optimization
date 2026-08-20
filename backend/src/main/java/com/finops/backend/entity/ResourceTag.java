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
@Table(name = "Resource_Tag")
public class ResourceTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resource_id", nullable = false)
    private AwsResource awsResource;

    @Column(name = "tag_key", nullable = false, length = 100)
    private String tagKey;

    @Column(name = "tag_value", length = 255)
    private String tagValue;

    protected ResourceTag() {}

    public ResourceTag(AwsResource awsResource, String tagKey, String tagValue) {
        this.awsResource = awsResource;
        this.tagKey = tagKey;
        this.tagValue = tagValue;
    }

    public Long getId() {
        return id;
    }

    public AwsResource getAwsResource() {
        return awsResource;
    }

    public void setAwsResource(AwsResource awsResource) {
        this.awsResource = awsResource;
    }

    public String getTagKey() {
        return tagKey;
    }

    public void setTagKey(String tagKey) {
        this.tagKey = tagKey;
    }

    public String getTagValue() {
        return tagValue;
    }

    public void setTagValue(String tagValue) {
        this.tagValue = tagValue;
    }
}
