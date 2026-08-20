package com.finops.backend.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "AWS_Resource")
public class AwsResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "resource_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "aws_account_id", nullable = false)
    private AwsAccount awsAccount;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "state_id", nullable = false)
    private ResourceState resourceState;

    @Column(name = "resource_name", nullable = false, length = 150)
    private String resourceName;

    @Column(name = "resource_arn", nullable = false, unique = true, length = 255)
    private String resourceArn;

    @Column(name = "discovered_at", insertable = false, updatable = false)
    private LocalDateTime discoveredAt;

    @OneToMany(mappedBy = "awsResource", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResourceTag> tags = new ArrayList<>();

    protected AwsResource() {}

    public AwsResource(AwsAccount awsAccount, Region region, ResourceState resourceState, String resourceName, String resourceArn) {
        this.awsAccount = awsAccount;
        this.region = region;
        this.resourceState = resourceState;
        this.resourceName = resourceName;
        this.resourceArn = resourceArn;
    }

    public Integer getId() {
        return id;
    }

    public AwsAccount getAwsAccount() {
        return awsAccount;
    }

    public void setAwsAccount(AwsAccount awsAccount) {
        this.awsAccount = awsAccount;
    }

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public ResourceState getResourceState() {
        return resourceState;
    }

    public void setResourceState(ResourceState resourceState) {
        this.resourceState = resourceState;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getResourceArn() {
        return resourceArn;
    }

    public void setResourceArn(String resourceArn) {
        this.resourceArn = resourceArn;
    }

    public LocalDateTime getDiscoveredAt() {
        return discoveredAt;
    }

    public List<ResourceTag> getTags() {
        return tags;
    }

    public void addTag(ResourceTag tag) {
        tags.add(tag);
        tag.setAwsResource(this);
    }

    public void removeTag(ResourceTag tag) {
        tags.remove(tag);
        tag.setAwsResource(null);
    }
}
