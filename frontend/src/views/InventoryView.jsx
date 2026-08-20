import React, { useState } from 'react';
import { api } from '../api/client';
import { Layers, Plus, Filter, Tag, Server, Database, HardDrive, Cpu, Cloud, Box, Network, Globe, Activity, X } from 'lucide-react';

const REGIONS = [
  { code: 'us-east-1', name: 'US East (N. Virginia)' },
  { code: 'us-east-2', name: 'US East (Ohio)' },
  { code: 'us-west-1', name: 'US West (N. California)' },
  { code: 'us-west-2', name: 'US West (Oregon)' },
  { code: 'eu-west-1', name: 'Europe (Ireland)' },
  { code: 'eu-west-2', name: 'Europe (London)' },
  { code: 'eu-central-1', name: 'Europe (Frankfurt)' },
  { code: 'ap-south-1', name: 'Asia Pacific (Mumbai)' },
  { code: 'ap-southeast-1', name: 'Asia Pacific (Singapore)' },
  { code: 'ap-southeast-2', name: 'Asia Pacific (Sydney)' },
  { code: 'ap-northeast-1', name: 'Asia Pacific (Tokyo)' },
  { code: 'ca-central-1', name: 'Canada (Central)' },
  { code: 'sa-east-1', name: 'South America (São Paulo)' },
  { code: 'me-central-1', name: 'Middle East (UAE)' }
];

const RESOURCE_TYPES = [
  { type: 'ec2:instance', service: 'EC2', label: 'EC2 Virtual Instance (ec2:instance)' },
  { type: 'rds:db', service: 'RDS', label: 'RDS Relational Database (rds:db)' },
  { type: 's3:bucket', service: 'S3', label: 'S3 Storage Bucket (s3:bucket)' },
  { type: 'lambda:function', service: 'LAMBDA', label: 'Lambda Serverless Function (lambda:function)' },
  { type: 'dynamodb:table', service: 'DYNAMODB', label: 'DynamoDB NoSQL Table (dynamodb:table)' },
  { type: 'eks:cluster', service: 'EKS', label: 'EKS Kubernetes Cluster (eks:cluster)' },
  { type: 'ecs:service', service: 'ECS', label: 'ECS Container Service (ecs:service)' },
  { type: 'cloudfront:distribution', service: 'CLOUDFRONT', label: 'CloudFront CDN Distribution (cloudfront:distribution)' },
  { type: 'elb:loadbalancer', service: 'ELB', label: 'Elastic Load Balancer (elb:loadbalancer)' },
  { type: 'redshift:cluster', service: 'REDSHIFT', label: 'Redshift Data Warehouse (redshift:cluster)' },
  { type: 'elasticache:cluster', service: 'ELASTICACHE', label: 'ElastiCache In-Memory Cluster (elasticache:cluster)' },
  { type: 'ebs:volume', service: 'EBS', label: 'EBS Persistent Block Volume (ebs:volume)' },
  { type: 'sagemaker:endpoint', service: 'SAGEMAKER', label: 'SageMaker ML Model Endpoint (sagemaker:endpoint)' },
  { type: 'bedrock:model', service: 'BEDROCK', label: 'Bedrock GenAI Foundation Model (bedrock:model)' }
];

const SERVICES_LIST = [
  'EC2', 'RDS', 'S3', 'LAMBDA', 'DYNAMODB', 'EKS', 'ECS', 'CLOUDFRONT', 'ELB', 'REDSHIFT', 'ELASTICACHE', 'EBS', 'SAGEMAKER', 'BEDROCK'
];

export default function InventoryView({ resources = [], accounts = [], onRefresh }) {
  const [showModal, setShowModal] = useState(false);
  const [filterService, setFilterService] = useState('');
  const [filterAccount, setFilterAccount] = useState('');
  const [filterRegion, setFilterRegion] = useState('');

  // Form State
  const [awsAccountId, setAwsAccountId] = useState(accounts[0]?.id || '');
  const [regionCode, setRegionCode] = useState('us-east-1');
  const [resourceTypeName, setResourceTypeName] = useState('ec2:instance');
  const [stateName, setStateName] = useState('RUNNING');
  const [resourceName, setResourceName] = useState('');
  const [resourceArn, setResourceArn] = useState('');
  const [tagKey, setTagKey] = useState('Env');
  const [tagValue, setTagValue] = useState('Production');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);

  const filteredResources = resources.filter((res) => {
    if (filterService && res.serviceName?.toUpperCase() !== filterService.toUpperCase()) return false;
    if (filterAccount && res.awsAccountId?.toString() !== filterAccount) return false;
    if (filterRegion && res.regionCode !== filterRegion) return false;
    return true;
  });

  const handleCreate = async (e) => {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      const selectedTypeObj = RESOURCE_TYPES.find(r => r.type === resourceTypeName);
      const svc = selectedTypeObj ? selectedTypeObj.service.toLowerCase() : 'ec2';

      const payload = {
        awsAccountId: parseInt(awsAccountId || accounts[0]?.id, 10),
        regionCode,
        resourceTypeName,
        stateName,
        resourceName,
        resourceArn: resourceArn || `arn:aws:${svc}:${regionCode}:123456789012:resource/${resourceName.toLowerCase().replace(/\s+/g, '-')}`,
        tags: tagKey ? [{ tagKey, tagValue }] : []
      };
      await api.createResource(payload);
      setShowModal(false);
      setResourceName('');
      setResourceArn('');
      onRefresh();
    } catch (err) {
      setError(err.message || 'Failed to create cloud resource');
    } finally {
      setSubmitting(false);
    }
  };

  const fillSampleResource = (type) => {
    const rId = Math.floor(1000 + Math.random() * 9000);
    setAwsAccountId(accounts[0]?.id || 1);
    setRegionCode('us-east-1');
    setResourceTypeName('ec2:instance');
    setStateName('RUNNING');
    setResourceName(`prod-app-worker-${rId}`);
    setResourceArn(`arn:aws:ec2:us-east-1:123456789012:instance/i-0a${rId}bc4d5e`);
    setTagKey('Environment');
    setTagValue('Production');
  };

  const getServiceIcon = (sName) => {
    switch (sName?.toUpperCase()) {
      case 'EC2': return <Server size={14} color="var(--primary)" />;
      case 'RDS': return <Database size={14} color="var(--info)" />;
      case 'S3': return <HardDrive size={14} color="var(--success)" />;
      case 'LAMBDA': return <Cpu size={14} color="var(--warning)" />;
      case 'DYNAMODB': return <Database size={14} color="#EC4899" />;
      case 'EKS':
      case 'ECS': return <Box size={14} color="#8B5CF6" />;
      case 'CLOUDFRONT': return <Globe size={14} color="#06B6D4" />;
      case 'ELB': return <Network size={14} color="#10B981" />;
      case 'SAGEMAKER':
      case 'BEDROCK': return <Activity size={14} color="#F59E0B" />;
      default: return <Cloud size={14} color="var(--text-muted)" />;
    }
  };

  return (
    <div>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 24 }}>
        <div>
          <h2 style={{ fontSize: '1.25rem', fontWeight: 700 }}>Cloud Resource Inventory</h2>
          <p style={{ fontSize: '0.84rem', color: 'var(--text-secondary)' }}>
            Real-time catalog and state mapping across your AWS multi-account & multi-region infrastructure
          </p>
        </div>
        <button onClick={() => { setShowModal(true); setAwsAccountId(accounts[0]?.id || ''); }} className="btn btn-primary">
          <Plus size={16} /> Add Cloud Asset
        </button>
      </div>

      {/* Filter Bar */}
      <div className="card" style={{ padding: '14px 20px', marginBottom: 20, display: 'flex', alignItems: 'center', gap: 12, flexWrap: 'wrap' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 8, color: 'var(--text-secondary)', fontSize: '0.84rem' }}>
          <Filter size={15} /> Filters:
        </div>

        <select
          className="form-select"
          style={{ width: 170, padding: '6px 10px', fontSize: '0.82rem' }}
          value={filterService}
          onChange={(e) => setFilterService(e.target.value)}
        >
          <option value="">All Services ({SERVICES_LIST.length})</option>
          {SERVICES_LIST.map(s => (
            <option key={s} value={s}>{s}</option>
          ))}
        </select>

        <select
          className="form-select"
          style={{ width: 190, padding: '6px 10px', fontSize: '0.82rem' }}
          value={filterRegion}
          onChange={(e) => setFilterRegion(e.target.value)}
        >
          <option value="">All Regions ({REGIONS.length})</option>
          {REGIONS.map(r => (
            <option key={r.code} value={r.code}>{r.code} ({r.name.split(' ')[0]})</option>
          ))}
        </select>

        <select
          className="form-select"
          style={{ width: 220, padding: '6px 10px', fontSize: '0.82rem' }}
          value={filterAccount}
          onChange={(e) => setFilterAccount(e.target.value)}
        >
          <option value="">All AWS Accounts</option>
          {accounts.map(acc => (
            <option key={acc.id} value={acc.id}>
              {acc.accountName} ({acc.awsAccountNumber})
            </option>
          ))}
        </select>

        <div style={{ marginLeft: 'auto', fontSize: '0.82rem', color: 'var(--text-muted)' }}>
          Showing {filteredResources.length} of {resources.length} resources
        </div>
      </div>

      {/* Resource Table */}
      <div className="card">
        {filteredResources.length === 0 ? (
          <div style={{ textAlign: 'center', padding: '48px 20px' }}>
            <Layers size={42} color="var(--text-muted)" style={{ margin: '0 auto 16px' }} />
            <h3 style={{ fontSize: '1.1rem', marginBottom: 6 }}>No Resources Found</h3>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.88rem', marginBottom: 20 }}>
              Add cloud resources to track telemetry metrics, cost allocation, and optimization recommendations.
            </p>
            <button onClick={() => setShowModal(true)} className="btn btn-primary">
              <Plus size={16} /> Add First Resource
            </button>
          </div>
        ) : (
          <div className="table-container">
            <table className="custom-table">
              <thead>
                <tr>
                  <th>Resource ID</th>
                  <th>Resource Name</th>
                  <th>Service & Type</th>
                  <th>Region</th>
                  <th>State</th>
                  <th>AWS Account</th>
                  <th>Tags</th>
                </tr>
              </thead>
              <tbody>
                {filteredResources.map((res) => (
                  <tr key={res.id}>
                    <td style={{ fontFamily: 'var(--font-mono)', fontWeight: 600 }}>#{res.id}</td>
                    <td style={{ fontWeight: 600 }}>{res.resourceName}</td>
                    <td>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                        {getServiceIcon(res.serviceName)}
                        <span><strong>{res.serviceName}</strong> ({res.resourceTypeName})</span>
                      </div>
                    </td>
                    <td>
                      <span className="pill pill-secondary" style={{ fontFamily: 'var(--font-mono)', fontSize: '0.78rem' }}>
                        {res.regionCode}
                      </span>
                    </td>
                    <td>
                      <span className={`pill ${res.stateName === 'RUNNING' || res.stateName === 'AVAILABLE' || res.stateName === 'ACTIVE' || res.stateName === 'DEPLOYED' || res.stateName === 'IN_SERVICE' ? 'pill-success' : 'pill-warning'}`}>
                        {res.stateName}
                      </span>
                    </td>
                    <td style={{ fontFamily: 'var(--font-mono)', color: 'var(--text-secondary)' }}>
                      {res.awsAccountNumber}
                    </td>
                    <td>
                      {res.tags && res.tags.length > 0 ? (
                        <div style={{ display: 'flex', gap: 4, flexWrap: 'wrap' }}>
                          {res.tags.map(t => (
                            <span key={t.tagKey} className="pill pill-info" style={{ fontSize: '0.7rem' }}>
                              {t.tagKey}: {t.tagValue}
                            </span>
                          ))}
                        </div>
                      ) : (
                        <span style={{ color: 'var(--text-muted)', fontSize: '0.75rem' }}>No tags</span>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Add Modal */}
      {showModal && (
        <div className="modal-backdrop">
          <div className="modal-card">
            <div className="modal-header">
              <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                <Layers size={20} color="var(--primary)" />
                <h3 style={{ fontSize: '1.1rem' }}>Provision Cloud Resource</h3>
              </div>
              <button
                onClick={() => setShowModal(false)}
                style={{ background: 'none', border: 'none', color: 'var(--text-muted)', cursor: 'pointer' }}
              >
                <X size={20} />
              </button>
            </div>

            {error && (
              <div style={{ margin: '16px 24px 0', padding: '10px 14px', borderRadius: 8, background: 'var(--danger-bg)', color: 'var(--danger)', fontSize: '0.84rem' }}>
                {error}
              </div>
            )}

            <form onSubmit={handleCreate}>
              <div className="modal-body">
                <div className="form-group">
                  <label className="form-label">Target AWS Account</label>
                  <select
                    className="form-select"
                    required
                    value={awsAccountId}
                    onChange={(e) => setAwsAccountId(e.target.value)}
                  >
                    {accounts.map(acc => (
                      <option key={acc.id} value={acc.id}>
                        {acc.accountName} ({acc.awsAccountNumber})
                      </option>
                    ))}
                  </select>
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
                  <div className="form-group">
                    <label className="form-label">Cloud Region</label>
                    <select
                      className="form-select"
                      value={regionCode}
                      onChange={(e) => setRegionCode(e.target.value)}
                    >
                      {REGIONS.map(r => (
                        <option key={r.code} value={r.code}>{r.code} - {r.name}</option>
                      ))}
                    </select>
                  </div>

                  <div className="form-group">
                    <label className="form-label">Resource State</label>
                    <select
                      className="form-select"
                      value={stateName}
                      onChange={(e) => setStateName(e.target.value)}
                    >
                      <option value="RUNNING">RUNNING</option>
                      <option value="ACTIVE">ACTIVE</option>
                      <option value="AVAILABLE">AVAILABLE</option>
                      <option value="STOPPED">STOPPED</option>
                      <option value="IN_SERVICE">IN_SERVICE</option>
                      <option value="DEPLOYED">DEPLOYED</option>
                    </select>
                  </div>
                </div>

                <div className="form-group">
                  <label className="form-label">Resource Type & Service</label>
                  <select
                    className="form-select"
                    value={resourceTypeName}
                    onChange={(e) => setResourceTypeName(e.target.value)}
                  >
                    {RESOURCE_TYPES.map(rt => (
                      <option key={rt.type} value={rt.type}>{rt.label}</option>
                    ))}
                  </select>
                </div>

                <div className="form-group">
                  <label className="form-label">Resource Name</label>
                  <input
                    type="text"
                    required
                    placeholder="e.g. prod-payment-service-cluster"
                    className="form-input"
                    value={resourceName}
                    onChange={(e) => setResourceName(e.target.value)}
                  />
                </div>

                <div className="form-group">
                  <label className="form-label">Resource ARN (Optional - auto generated if empty)</label>
                  <input
                    type="text"
                    placeholder="arn:aws:eks:us-east-1:123456789012:cluster/prod-cluster"
                    className="form-input"
                    value={resourceArn}
                    onChange={(e) => setResourceArn(e.target.value)}
                  />
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
                  <div className="form-group">
                    <label className="form-label">Tag Key</label>
                    <input
                      type="text"
                      className="form-input"
                      placeholder="Environment"
                      value={tagKey}
                      onChange={(e) => setTagKey(e.target.value)}
                    />
                  </div>
                  <div className="form-group">
                    <label className="form-label">Tag Value</label>
                    <input
                      type="text"
                      className="form-input"
                      placeholder="Production"
                      value={tagValue}
                      onChange={(e) => setTagValue(e.target.value)}
                    />
                  </div>
                </div>

                <button
                  type="button"
                  onClick={fillSampleResource}
                  style={{ background: 'none', border: 'none', color: 'var(--primary)', cursor: 'pointer', fontSize: '0.8rem', marginTop: 4 }}
                >
                  + Auto-fill Test EC2 Resource Data
                </button>
              </div>

              <div className="modal-footer">
                <button type="button" onClick={() => setShowModal(false)} className="btn btn-secondary">
                  Cancel
                </button>
                <button type="submit" disabled={submitting} className="btn btn-primary">
                  {submitting ? 'Creating...' : 'Create Resource'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
