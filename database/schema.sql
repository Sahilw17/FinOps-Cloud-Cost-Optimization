CREATE DATABASE IF NOT EXISTS finops_assistant;
USE finops_assistant;

CREATE TABLE Role (
    role_id     INT AUTO_INCREMENT PRIMARY KEY,
    role_name   VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE User (
    user_id         INT AUTO_INCREMENT PRIMARY KEY,
    role_id         INT NOT NULL,
    name            VARCHAR(100) NOT NULL,
    email           VARCHAR(150) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_role
        FOREIGN KEY (role_id) REFERENCES Role(role_id)
);

CREATE TABLE AWS_Credentials (
    credential_id   INT AUTO_INCREMENT PRIMARY KEY,
    access_key      VARCHAR(100) NOT NULL,
    secret_key      VARCHAR(255) NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE AWS_Account (
    aws_account_id      INT AUTO_INCREMENT PRIMARY KEY,
    user_id             INT NOT NULL,
    credential_id       INT NOT NULL,
    aws_account_number  VARCHAR(20) NOT NULL UNIQUE,
    account_name        VARCHAR(100) NOT NULL,
    status              ENUM('ACTIVE','INACTIVE') DEFAULT 'ACTIVE',
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_account_user
        FOREIGN KEY (user_id) REFERENCES User(user_id),
    CONSTRAINT fk_account_credentials
        FOREIGN KEY (credential_id) REFERENCES AWS_Credentials(credential_id)
);

CREATE TABLE Region (
    region_id    INT AUTO_INCREMENT PRIMARY KEY,
    region_code  VARCHAR(30) NOT NULL UNIQUE,
    region_name  VARCHAR(100) NOT NULL
);

CREATE TABLE AWS_Service (
    service_id    INT AUTO_INCREMENT PRIMARY KEY,
    service_name  VARCHAR(100) NOT NULL UNIQUE,
    namespace     VARCHAR(100) NOT NULL
);

CREATE TABLE Resource_Type (
    resource_type_id    INT AUTO_INCREMENT PRIMARY KEY,
    service_id          INT NOT NULL,
    resource_type_name  VARCHAR(100) NOT NULL,
    CONSTRAINT fk_resource_service
        FOREIGN KEY (service_id) REFERENCES AWS_Service(service_id),
    CONSTRAINT uq_resource_type UNIQUE (service_id, resource_type_name)
);

CREATE TABLE Resource_State (
    state_id           INT AUTO_INCREMENT PRIMARY KEY,
    resource_type_id   INT NOT NULL,
    state_name         VARCHAR(50) NOT NULL,
    CONSTRAINT fk_state_resource_type
        FOREIGN KEY (resource_type_id) REFERENCES Resource_Type(resource_type_id),
    CONSTRAINT uq_state UNIQUE (resource_type_id, state_name)
);

CREATE TABLE AWS_Resource (
    resource_id     INT AUTO_INCREMENT PRIMARY KEY,
    aws_account_id  INT NOT NULL,
    region_id       INT NOT NULL,
    state_id        INT NOT NULL,
    resource_name   VARCHAR(150) NOT NULL,
    resource_arn    VARCHAR(255) NOT NULL UNIQUE,
    discovered_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_resource_account
        FOREIGN KEY (aws_account_id) REFERENCES AWS_Account(aws_account_id),
    CONSTRAINT fk_resource_region
        FOREIGN KEY (region_id) REFERENCES Region(region_id),
    CONSTRAINT fk_resource_state
        FOREIGN KEY (state_id) REFERENCES Resource_State(state_id)
);

CREATE TABLE Unit (
    unit_id    INT AUTO_INCREMENT PRIMARY KEY,
    unit_name  VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE Metric_Name (
    metric_name_id  INT AUTO_INCREMENT PRIMARY KEY,
    metric_name     VARCHAR(100) NOT NULL UNIQUE,
    unit_id         INT NOT NULL,
    CONSTRAINT fk_metric_name_unit
        FOREIGN KEY (unit_id) REFERENCES Unit(unit_id)
);

CREATE TABLE Resource_Metrics (
    metric_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    resource_id      INT NOT NULL,
    metric_name_id   INT NOT NULL,
    metric_value     DECIMAL(15,4) NOT NULL,
    statistic        ENUM('Average','Maximum','Minimum','Sum') NOT NULL,
    collected_at     DATETIME NOT NULL,
    CONSTRAINT fk_metric_resource
        FOREIGN KEY (resource_id) REFERENCES AWS_Resource(resource_id),
    CONSTRAINT fk_metric_name
        FOREIGN KEY (metric_name_id) REFERENCES Metric_Name(metric_name_id)
);

CREATE TABLE Cost_History (
    cost_id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    resource_id     INT NULL,
    aws_account_id  INT NULL,
    service_id      INT NULL,
    usage_date      DATE NOT NULL,
    usage_amount    DECIMAL(15,4),
    unit_id         INT NOT NULL,
    cost_amount     DECIMAL(15,2) NOT NULL,
    recorded_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cost_resource
        FOREIGN KEY (resource_id) REFERENCES AWS_Resource(resource_id),
    CONSTRAINT fk_cost_account
        FOREIGN KEY (aws_account_id) REFERENCES AWS_Account(aws_account_id),
    CONSTRAINT fk_cost_service
        FOREIGN KEY (service_id) REFERENCES AWS_Service(service_id),
    CONSTRAINT fk_cost_unit
        FOREIGN KEY (unit_id) REFERENCES Unit(unit_id),
    CONSTRAINT chk_cost_scope CHECK (
        (resource_id IS NOT NULL AND aws_account_id IS NULL AND service_id IS NULL)
        OR
        (resource_id IS NULL AND aws_account_id IS NOT NULL AND service_id IS NOT NULL)
    )
);

CREATE TABLE Prediction_Type (
    prediction_type_id  INT AUTO_INCREMENT PRIMARY KEY,
    prediction_type     VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE Prediction (
    prediction_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    resource_id         INT NULL,
    aws_account_id      INT NULL,
    service_id          INT NULL,
    prediction_type_id  INT NOT NULL,
    predicted_cost       DECIMAL(15,2),
    confidence_score     DECIMAL(5,2),
    prediction_date      DATE,
    CONSTRAINT fk_prediction_resource
        FOREIGN KEY (resource_id) REFERENCES AWS_Resource(resource_id),
    CONSTRAINT fk_prediction_account
        FOREIGN KEY (aws_account_id) REFERENCES AWS_Account(aws_account_id),
    CONSTRAINT fk_prediction_service
        FOREIGN KEY (service_id) REFERENCES AWS_Service(service_id),
    CONSTRAINT fk_prediction_type
        FOREIGN KEY (prediction_type_id) REFERENCES Prediction_Type(prediction_type_id),
    CONSTRAINT chk_prediction_scope CHECK (
        (resource_id IS NOT NULL AND aws_account_id IS NULL AND service_id IS NULL)
        OR
        (resource_id IS NULL AND aws_account_id IS NOT NULL AND service_id IS NOT NULL)
    )
);

CREATE TABLE Recommendation_Type (
    recommendation_type_id  INT AUTO_INCREMENT PRIMARY KEY,
    recommendation_type     VARCHAR(100) UNIQUE NOT NULL
);

CREATE TABLE Recommendation (
    recommendation_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    resource_id             INT NULL,
    aws_account_id          INT NULL,
    service_id              INT NULL,
    recommendation_type_id  INT NOT NULL,
    title                    VARCHAR(200) NOT NULL,
    description              TEXT,
    estimated_monthly_saving DECIMAL(15,2),
    status                   ENUM('NEW','VIEWED','APPLIED','DISMISSED') DEFAULT 'NEW',
    created_at               TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_rec_resource
        FOREIGN KEY (resource_id) REFERENCES AWS_Resource(resource_id),
    CONSTRAINT fk_rec_account
        FOREIGN KEY (aws_account_id) REFERENCES AWS_Account(aws_account_id),
    CONSTRAINT fk_rec_service
        FOREIGN KEY (service_id) REFERENCES AWS_Service(service_id),
    CONSTRAINT fk_rec_type
        FOREIGN KEY (recommendation_type_id) REFERENCES Recommendation_Type(recommendation_type_id),
    CONSTRAINT chk_rec_scope CHECK (
        (resource_id IS NOT NULL AND aws_account_id IS NULL AND service_id IS NULL)
        OR
        (resource_id IS NULL AND aws_account_id IS NOT NULL AND service_id IS NOT NULL)
    )
);

CREATE TABLE Alert_Type (
    alert_type_id  INT AUTO_INCREMENT PRIMARY KEY,
    alert_type     VARCHAR(100) UNIQUE NOT NULL
);

CREATE TABLE Alert (
    alert_id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    resource_id      INT NULL,
    aws_account_id   INT NULL,
    service_id       INT NULL,
    alert_type_id    INT NOT NULL,
    title             VARCHAR(200) NOT NULL,
    message           TEXT,
    threshold_value   DECIMAL(15,4),
    actual_value      DECIMAL(15,4),
    unit_id           INT NULL,
    status             ENUM('UNREAD','READ','RESOLVED') DEFAULT 'UNREAD',
    created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_alert_resource
        FOREIGN KEY (resource_id) REFERENCES AWS_Resource(resource_id),
    CONSTRAINT fk_alert_account
        FOREIGN KEY (aws_account_id) REFERENCES AWS_Account(aws_account_id),
    CONSTRAINT fk_alert_service
        FOREIGN KEY (service_id) REFERENCES AWS_Service(service_id),
    CONSTRAINT fk_alert_type
        FOREIGN KEY (alert_type_id) REFERENCES Alert_Type(alert_type_id),
    CONSTRAINT fk_alert_unit
        FOREIGN KEY (unit_id) REFERENCES Unit(unit_id),
    CONSTRAINT chk_alert_scope CHECK (
        (resource_id IS NOT NULL AND aws_account_id IS NULL AND service_id IS NULL)
        OR
        (resource_id IS NULL AND aws_account_id IS NOT NULL AND service_id IS NOT NULL)
    )
);

CREATE TABLE Resource_Tag (
    tag_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    resource_id  INT NOT NULL,
    tag_key      VARCHAR(100) NOT NULL,
    tag_value    VARCHAR(255),
    CONSTRAINT fk_tag_resource
        FOREIGN KEY (resource_id) REFERENCES AWS_Resource(resource_id),
    CONSTRAINT uq_resource_tag UNIQUE (resource_id, tag_key)
);

CREATE VIEW v_resource_detail AS
SELECT
    r.resource_id,
    r.aws_account_id,
    rt.service_id,
    rt.resource_type_id,
    rt.resource_type_name,
    r.region_id,
    r.state_id,
    rs.state_name,
    r.resource_name,
    r.resource_arn,
    r.discovered_at
FROM AWS_Resource r
JOIN Resource_State rs ON rs.state_id = r.state_id
JOIN Resource_Type rt ON rt.resource_type_id = rs.resource_type_id;

CREATE VIEW v_cost_history_flat AS
SELECT
    c.cost_id,
    COALESCE(c.aws_account_id, res.aws_account_id) AS aws_account_id,
    COALESCE(c.service_id, res.service_id)         AS service_id,
    c.resource_id,
    c.usage_date,
    c.usage_amount,
    c.unit_id,
    c.cost_amount,
    c.recorded_at
FROM Cost_History c
LEFT JOIN v_resource_detail res ON res.resource_id = c.resource_id;

CREATE VIEW v_prediction_flat AS
SELECT
    p.prediction_id,
    COALESCE(p.aws_account_id, res.aws_account_id) AS aws_account_id,
    COALESCE(p.service_id, res.service_id)         AS service_id,
    p.resource_id,
    p.prediction_type_id,
    p.predicted_cost,
    p.confidence_score,
    p.prediction_date
FROM Prediction p
LEFT JOIN v_resource_detail res ON res.resource_id = p.resource_id;

CREATE VIEW v_recommendation_flat AS
SELECT
    r.recommendation_id,
    COALESCE(r.aws_account_id, res.aws_account_id) AS aws_account_id,
    COALESCE(r.service_id, res.service_id)         AS service_id,
    r.resource_id,
    r.recommendation_type_id,
    r.title,
    r.description,
    r.estimated_monthly_saving,
    r.status,
    r.created_at
FROM Recommendation r
LEFT JOIN v_resource_detail res ON res.resource_id = r.resource_id;

CREATE VIEW v_alert_flat AS
SELECT
    a.alert_id,
    COALESCE(a.aws_account_id, res.aws_account_id) AS aws_account_id,
    COALESCE(a.service_id, res.service_id)         AS service_id,
    a.resource_id,
    a.alert_type_id,
    a.title,
    a.message,
    a.threshold_value,
    a.actual_value,
    a.unit_id,
    a.status,
    a.created_at
FROM Alert a
LEFT JOIN v_resource_detail res ON res.resource_id = a.resource_id;
