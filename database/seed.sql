-- Seed initial roles idempotently into the frozen schema
USE finops_assistant;

INSERT IGNORE INTO Role (role_name) VALUES ('ADMIN');
INSERT IGNORE INTO Role (role_name) VALUES ('FINOPS_ENGINEER');
INSERT IGNORE INTO Role (role_name) VALUES ('USER');

-- Regions (All Major Global AWS Regions)
INSERT IGNORE INTO Region (region_code, region_name) VALUES 
('us-east-1', 'US East (N. Virginia)'),
('us-east-2', 'US East (Ohio)'),
('us-west-1', 'US West (N. California)'),
('us-west-2', 'US West (Oregon)'),
('eu-west-1', 'Europe (Ireland)'),
('eu-west-2', 'Europe (London)'),
('eu-central-1', 'Europe (Frankfurt)'),
('ap-south-1', 'Asia Pacific (Mumbai)'),
('ap-southeast-1', 'Asia Pacific (Singapore)'),
('ap-southeast-2', 'Asia Pacific (Sydney)'),
('ap-northeast-1', 'Asia Pacific (Tokyo)'),
('ca-central-1', 'Canada (Central)'),
('sa-east-1', 'South America (São Paulo)'),
('me-central-1', 'Middle East (UAE)');

-- AWS Services
INSERT IGNORE INTO AWS_Service (service_name, namespace) VALUES 
('EC2', 'AWS/EC2'),
('RDS', 'AWS/RDS'),
('S3', 'AWS/S3'),
('LAMBDA', 'AWS/Lambda'),
('DYNAMODB', 'AWS/DynamoDB'),
('EKS', 'AWS/EKS'),
('ECS', 'AWS/ECS'),
('CLOUDFRONT', 'AWS/CloudFront'),
('ELB', 'AWS/ApplicationELB'),
('REDSHIFT', 'AWS/Redshift'),
('ELASTICACHE', 'AWS/ElastiCache'),
('EBS', 'AWS/EBS'),
('SAGEMAKER', 'AWS/SageMaker'),
('BEDROCK', 'AWS/Bedrock'),
('API_GATEWAY', 'AWS/ApiGateway'),
('SNS', 'AWS/SNS'),
('SQS', 'AWS/SQS');

-- Resource Types
INSERT IGNORE INTO Resource_Type (service_id, resource_type_name) SELECT service_id, 'ec2:instance' FROM AWS_Service WHERE service_name = 'EC2';
INSERT IGNORE INTO Resource_Type (service_id, resource_type_name) SELECT service_id, 'rds:db' FROM AWS_Service WHERE service_name = 'RDS';
INSERT IGNORE INTO Resource_Type (service_id, resource_type_name) SELECT service_id, 's3:bucket' FROM AWS_Service WHERE service_name = 'S3';
INSERT IGNORE INTO Resource_Type (service_id, resource_type_name) SELECT service_id, 'lambda:function' FROM AWS_Service WHERE service_name = 'LAMBDA';
INSERT IGNORE INTO Resource_Type (service_id, resource_type_name) SELECT service_id, 'dynamodb:table' FROM AWS_Service WHERE service_name = 'DYNAMODB';
INSERT IGNORE INTO Resource_Type (service_id, resource_type_name) SELECT service_id, 'eks:cluster' FROM AWS_Service WHERE service_name = 'EKS';
INSERT IGNORE INTO Resource_Type (service_id, resource_type_name) SELECT service_id, 'ecs:service' FROM AWS_Service WHERE service_name = 'ECS';
INSERT IGNORE INTO Resource_Type (service_id, resource_type_name) SELECT service_id, 'cloudfront:distribution' FROM AWS_Service WHERE service_name = 'CLOUDFRONT';
INSERT IGNORE INTO Resource_Type (service_id, resource_type_name) SELECT service_id, 'elb:loadbalancer' FROM AWS_Service WHERE service_name = 'ELB';
INSERT IGNORE INTO Resource_Type (service_id, resource_type_name) SELECT service_id, 'redshift:cluster' FROM AWS_Service WHERE service_name = 'REDSHIFT';
INSERT IGNORE INTO Resource_Type (service_id, resource_type_name) SELECT service_id, 'elasticache:cluster' FROM AWS_Service WHERE service_name = 'ELASTICACHE';
INSERT IGNORE INTO Resource_Type (service_id, resource_type_name) SELECT service_id, 'ebs:volume' FROM AWS_Service WHERE service_name = 'EBS';
INSERT IGNORE INTO Resource_Type (service_id, resource_type_name) SELECT service_id, 'sagemaker:endpoint' FROM AWS_Service WHERE service_name = 'SAGEMAKER';
INSERT IGNORE INTO Resource_Type (service_id, resource_type_name) SELECT service_id, 'bedrock:model' FROM AWS_Service WHERE service_name = 'BEDROCK';

-- Resource States
INSERT IGNORE INTO Resource_State (resource_type_id, state_name) SELECT resource_type_id, 'RUNNING' FROM Resource_Type WHERE resource_type_name = 'ec2:instance';
INSERT IGNORE INTO Resource_State (resource_type_id, state_name) SELECT resource_type_id, 'STOPPED' FROM Resource_Type WHERE resource_type_name = 'ec2:instance';
INSERT IGNORE INTO Resource_State (resource_type_id, state_name) SELECT resource_type_id, 'TERMINATED' FROM Resource_Type WHERE resource_type_name = 'ec2:instance';
INSERT IGNORE INTO Resource_State (resource_type_id, state_name) SELECT resource_type_id, 'AVAILABLE' FROM Resource_Type WHERE resource_type_name = 'rds:db';
INSERT IGNORE INTO Resource_State (resource_type_id, state_name) SELECT resource_type_id, 'STOPPED' FROM Resource_Type WHERE resource_type_name = 'rds:db';
INSERT IGNORE INTO Resource_State (resource_type_id, state_name) SELECT resource_type_id, 'AVAILABLE' FROM Resource_Type WHERE resource_type_name = 's3:bucket';
INSERT IGNORE INTO Resource_State (resource_type_id, state_name) SELECT resource_type_id, 'ACTIVE' FROM Resource_Type WHERE resource_type_name = 'lambda:function';
INSERT IGNORE INTO Resource_State (resource_type_id, state_name) SELECT resource_type_id, 'ACTIVE' FROM Resource_Type WHERE resource_type_name = 'dynamodb:table';
INSERT IGNORE INTO Resource_State (resource_type_id, state_name) SELECT resource_type_id, 'ACTIVE' FROM Resource_Type WHERE resource_type_name = 'eks:cluster';
INSERT IGNORE INTO Resource_State (resource_type_id, state_name) SELECT resource_type_id, 'ACTIVE' FROM Resource_Type WHERE resource_type_name = 'ecs:service';
INSERT IGNORE INTO Resource_State (resource_type_id, state_name) SELECT resource_type_id, 'DEPLOYED' FROM Resource_Type WHERE resource_type_name = 'cloudfront:distribution';
INSERT IGNORE INTO Resource_State (resource_type_id, state_name) SELECT resource_type_id, 'ACTIVE' FROM Resource_Type WHERE resource_type_name = 'elb:loadbalancer';
INSERT IGNORE INTO Resource_State (resource_type_id, state_name) SELECT resource_type_id, 'AVAILABLE' FROM Resource_Type WHERE resource_type_name = 'redshift:cluster';
INSERT IGNORE INTO Resource_State (resource_type_id, state_name) SELECT resource_type_id, 'AVAILABLE' FROM Resource_Type WHERE resource_type_name = 'elasticache:cluster';
INSERT IGNORE INTO Resource_State (resource_type_id, state_name) SELECT resource_type_id, 'IN_USE' FROM Resource_Type WHERE resource_type_name = 'ebs:volume';
INSERT IGNORE INTO Resource_State (resource_type_id, state_name) SELECT resource_type_id, 'AVAILABLE' FROM Resource_Type WHERE resource_type_name = 'ebs:volume';
INSERT IGNORE INTO Resource_State (resource_type_id, state_name) SELECT resource_type_id, 'IN_SERVICE' FROM Resource_Type WHERE resource_type_name = 'sagemaker:endpoint';
INSERT IGNORE INTO Resource_State (resource_type_id, state_name) SELECT resource_type_id, 'ACTIVE' FROM Resource_Type WHERE resource_type_name = 'bedrock:model';

-- Units
INSERT IGNORE INTO Unit (unit_name) VALUES ('Percent');
INSERT IGNORE INTO Unit (unit_name) VALUES ('Bytes');
INSERT IGNORE INTO Unit (unit_name) VALUES ('Count');
INSERT IGNORE INTO Unit (unit_name) VALUES ('Hours');
INSERT IGNORE INTO Unit (unit_name) VALUES ('GB-Mo');
INSERT IGNORE INTO Unit (unit_name) VALUES ('USD');
INSERT IGNORE INTO Unit (unit_name) VALUES ('Milliseconds');

-- Metric Names
INSERT IGNORE INTO Metric_Name (metric_name, unit_id) SELECT 'CPUUtilization', unit_id FROM Unit WHERE unit_name = 'Percent';
INSERT IGNORE INTO Metric_Name (metric_name, unit_id) SELECT 'MemoryUtilization', unit_id FROM Unit WHERE unit_name = 'Percent';
INSERT IGNORE INTO Metric_Name (metric_name, unit_id) SELECT 'NetworkIn', unit_id FROM Unit WHERE unit_name = 'Bytes';
INSERT IGNORE INTO Metric_Name (metric_name, unit_id) SELECT 'NetworkOut', unit_id FROM Unit WHERE unit_name = 'Bytes';
INSERT IGNORE INTO Metric_Name (metric_name, unit_id) SELECT 'DiskReadOps', unit_id FROM Unit WHERE unit_name = 'Count';
INSERT IGNORE INTO Metric_Name (metric_name, unit_id) SELECT 'DiskWriteOps', unit_id FROM Unit WHERE unit_name = 'Count';
INSERT IGNORE INTO Metric_Name (metric_name, unit_id) SELECT 'Duration', unit_id FROM Unit WHERE unit_name = 'Milliseconds';
INSERT IGNORE INTO Metric_Name (metric_name, unit_id) SELECT 'Invocations', unit_id FROM Unit WHERE unit_name = 'Count';
INSERT IGNORE INTO Metric_Name (metric_name, unit_id) SELECT 'DatabaseConnections', unit_id FROM Unit WHERE unit_name = 'Count';
INSERT IGNORE INTO Metric_Name (metric_name, unit_id) SELECT 'FreeStorageSpace', unit_id FROM Unit WHERE unit_name = 'Bytes';

-- Prediction Types
INSERT IGNORE INTO Prediction_Type (prediction_type) VALUES ('LINEAR_REGRESSION');
INSERT IGNORE INTO Prediction_Type (prediction_type) VALUES ('MOVING_AVERAGE');
INSERT IGNORE INTO Prediction_Type (prediction_type) VALUES ('MONTHLY_FORECAST');

-- Recommendation Types
INSERT IGNORE INTO Recommendation_Type (recommendation_type) VALUES ('RIGHTSIZE_INSTANCE');
INSERT IGNORE INTO Recommendation_Type (recommendation_type) VALUES ('TERMINATE_IDLE_RESOURCE');
INSERT IGNORE INTO Recommendation_Type (recommendation_type) VALUES ('PURCHASE_RESERVED_INSTANCE');
INSERT IGNORE INTO Recommendation_Type (recommendation_type) VALUES ('STORAGE_TIER_OPTIMIZATION');

-- Alert Types
INSERT IGNORE INTO Alert_Type (alert_type) VALUES ('COST_SPIKE_DETECTED');
INSERT IGNORE INTO Alert_Type (alert_type) VALUES ('HIGH_UTILIZATION_THRESHOLD');
INSERT IGNORE INTO Alert_Type (alert_type) VALUES ('BUDGET_EXCEEDED');
INSERT IGNORE INTO Alert_Type (alert_type) VALUES ('IDLE_HIGH_COST_RESOURCE');
