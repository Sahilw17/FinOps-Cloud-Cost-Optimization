import React, { useState, useEffect } from 'react';
import { api } from '../api/client';
import { Zap, CheckCircle2, XCircle, Sparkles, DollarSign, Plus, X } from 'lucide-react';

export default function RecommendationsView({
  recommendations = [],
  resources = [],
  accounts = [],
  onRefresh,
  onAnalyze,
  analyzing,
  onOpenAiAudit,
  onOpenAiCopilot
}) {
  const [filterStatus, setFilterStatus] = useState('ALL');
  const [updatingId, setUpdatingId] = useState(null);
  const [showModal, setShowModal] = useState(false);

  // Manual Recommendation Form
  const [recScope, setRecScope] = useState('resource'); // 'resource' or 'account'
  const [selectedResourceId, setSelectedResourceId] = useState('');
  const [selectedAccountId, setSelectedAccountId] = useState('');
  const [selectedServiceName, setSelectedServiceName] = useState('EC2');
  const [recommendationType, setRecommendationType] = useState('RIGHTSIZE_INSTANCE');
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [estimatedMonthlySaving, setEstimatedMonthlySaving] = useState('45.00');

  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);
  const [successMsg, setSuccessMsg] = useState(null);

  useEffect(() => {
    if (resources.length > 0 && !selectedResourceId) {
      setSelectedResourceId(resources[0].id.toString());
    }
  }, [resources, selectedResourceId]);

  useEffect(() => {
    if (accounts.length > 0 && !selectedAccountId) {
      setSelectedAccountId(accounts[0].id.toString());
    }
  }, [accounts, selectedAccountId]);

  const filteredRecs = recommendations.filter((r) => {
    if (filterStatus === 'ALL') return true;
    return r.status === filterStatus;
  });

  const potentialSavings = recommendations
    .filter(r => r.status === 'NEW' || r.status === 'VIEWED')
    .reduce((sum, r) => sum + (parseFloat(r.estimatedMonthlySaving) || 0), 0);

  const realizedSavings = recommendations
    .filter(r => r.status === 'APPLIED')
    .reduce((sum, r) => sum + (parseFloat(r.estimatedMonthlySaving) || 0), 0);

  const handleUpdateStatus = async (id, newStatus) => {
    setUpdatingId(id);
    setError(null);
    try {
      await api.updateRecommendationStatus(id, newStatus);
      setSuccessMsg(`Recommendation marked as ${newStatus}!`);
      setTimeout(() => setSuccessMsg(null), 3000);
      onRefresh();
    } catch (err) {
      setError(err.message || 'Failed to update recommendation status');
    } finally {
      setUpdatingId(null);
    }
  };

  const handleCreateRecommendation = async (e) => {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      const payload = {
        recommendationType,
        title,
        description,
        estimatedMonthlySaving: parseFloat(estimatedMonthlySaving) || 0
      };

      if (recScope === 'resource') {
        const resId = parseInt(selectedResourceId || resources[0]?.id, 10);
        if (isNaN(resId)) {
          throw new Error('Please select an active Cloud Resource.');
        }
        payload.resourceId = resId;
      } else {
        const accId = parseInt(selectedAccountId || accounts[0]?.id, 10);
        if (isNaN(accId)) {
          throw new Error('Please select an AWS Account.');
        }
        payload.awsAccountId = accId;
        payload.serviceName = selectedServiceName || 'EC2';
      }

      await api.createRecommendation(payload);
      setShowModal(false);
      setTitle('');
      setDescription('');
      setSuccessMsg('Optimization recommendation created successfully!');
      setTimeout(() => setSuccessMsg(null), 3000);
      onRefresh();
    } catch (err) {
      setError(err.message || 'Failed to create recommendation');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 24 }}>
        <div>
          <h2 style={{ fontSize: '1.25rem', fontWeight: 700 }}>FinOps Rightsizing & Optimization Hub</h2>
          <p style={{ fontSize: '0.84rem', color: 'var(--text-secondary)' }}>
            AI-driven rightsizing suggestions, idle asset terminations, and storage tier recommendations
          </p>
        </div>
        <div style={{ display: 'flex', gap: 12 }}>
          <button onClick={() => { setError(null); setShowModal(true); }} className="btn btn-secondary">
            <Plus size={16} /> Add Recommendation
          </button>
          <button onClick={onAnalyze} disabled={analyzing} className="btn btn-primary">
            <Sparkles size={16} /> {analyzing ? 'Evaluating Telemetry...' : 'Trigger Rightsizing Scan'}
          </button>
        </div>
      </div>

      {successMsg && (
        <div style={{ marginBottom: 20, padding: '12px 16px', borderRadius: 8, background: 'var(--success-bg)', color: 'var(--success)', fontSize: '0.88rem', display: 'flex', alignItems: 'center', gap: 8 }}>
          <CheckCircle2 size={16} /> {successMsg}
        </div>
      )}

      {/* Savings Summary Banner */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 20, marginBottom: 24 }}>
        <div className="card" style={{ borderLeft: '4px solid var(--success)' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <div>
              <div style={{ fontSize: '0.82rem', color: 'var(--text-muted)', fontWeight: 600, textTransform: 'uppercase' }}>
                Potential Monthly Savings
              </div>
              <div style={{ fontSize: '1.85rem', fontWeight: 800, color: 'var(--success)', fontFamily: 'var(--font-mono)' }}>
                ${potentialSavings.toFixed(2)}/mo
              </div>
            </div>
            <span className="pill pill-success">{recommendations.filter(r => r.status === 'NEW').length} Actionable</span>
          </div>
        </div>

        <div className="card" style={{ borderLeft: '4px solid var(--primary)' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <div>
              <div style={{ fontSize: '0.82rem', color: 'var(--text-muted)', fontWeight: 600, textTransform: 'uppercase' }}>
                Realized Savings (Applied)
              </div>
              <div style={{ fontSize: '1.85rem', fontWeight: 800, color: 'var(--primary)', fontFamily: 'var(--font-mono)' }}>
                ${realizedSavings.toFixed(2)}/mo
              </div>
            </div>
            <span className="pill pill-info">{recommendations.filter(r => r.status === 'APPLIED').length} Applied</span>
          </div>
        </div>
      </div>

      {/* Filter Tabs */}
      <div style={{ display: 'flex', gap: 8, marginBottom: 20 }}>
        {['ALL', 'NEW', 'APPLIED', 'DISMISSED'].map((status) => (
          <button
            key={status}
            onClick={() => setFilterStatus(status)}
            className={`btn btn-sm ${filterStatus === status ? 'btn-primary' : 'btn-secondary'}`}
          >
            {status}
          </button>
        ))}
      </div>

      {/* Recommendation Cards */}
      {filteredRecs.length === 0 ? (
        <div className="card" style={{ textAlign: 'center', padding: '48px 20px' }}>
          <Zap size={42} color="var(--text-muted)" style={{ margin: '0 auto 16px' }} />
          <h3 style={{ fontSize: '1.1rem', marginBottom: 6 }}>No Recommendations in this Category</h3>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.88rem', marginBottom: 20 }}>
            Click "Trigger Rightsizing Scan" above to evaluate telemetry metrics and find idle or oversized assets.
          </p>
          <button onClick={onAnalyze} disabled={analyzing} className="btn btn-primary">
            <Sparkles size={16} /> {analyzing ? 'Evaluating...' : 'Run Automated Scan'}
          </button>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
          {filteredRecs.map((rec) => (
            <div
              key={rec.id}
              className="card"
              style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                gap: 20,
                borderLeft: rec.status === 'APPLIED' ? '4px solid var(--primary)' : '4px solid var(--success)'
              }}
            >
              <div style={{ flex: 1 }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 6 }}>
                  <span className="pill pill-info">{rec.recommendationType}</span>
                  <span className={`pill ${rec.status === 'APPLIED' ? 'pill-primary' : rec.status === 'DISMISSED' ? 'pill-warning' : 'pill-success'}`}>
                    {rec.status}
                  </span>
                  <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                    Target: <strong>{rec.resourceName || (rec.awsAccountNumber ? `Account ${rec.awsAccountNumber}` : 'Account-wide')}</strong>
                  </span>
                </div>
                <h4 style={{ fontSize: '1.05rem', fontWeight: 700, marginBottom: 4 }}>{rec.title}</h4>
                <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>{rec.description}</p>
              </div>

              <div style={{ display: 'flex', alignItems: 'center', gap: 20 }}>
                <div style={{ textAlign: 'right' }}>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', textTransform: 'uppercase' }}>Est. Savings</div>
                  <div style={{ fontSize: '1.35rem', fontWeight: 800, color: 'var(--success)', fontFamily: 'var(--font-mono)' }}>
                    +${parseFloat(rec.estimatedMonthlySaving).toFixed(2)}/mo
                  </div>
                </div>

                <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                  {rec.resourceId && (
                    <button
                      onClick={() => onOpenAiAudit(rec.resourceId)}
                      className="btn btn-secondary btn-sm"
                      style={{
                        background: 'rgba(56, 189, 248, 0.1)',
                        border: '1px solid rgba(56, 189, 248, 0.3)',
                        color: 'var(--primary)',
                        fontWeight: 600
                      }}
                      title="Run Google Gemini AI Deep Audit on this resource"
                    >
                      <Sparkles size={14} color="var(--primary)" /> AI Deep Audit
                    </button>
                  )}

                  {rec.status === 'NEW' || rec.status === 'VIEWED' ? (
                    <>
                      <button
                        onClick={() => handleUpdateStatus(rec.id, 'APPLIED')}
                        disabled={updatingId === rec.id}
                        className="btn btn-success btn-sm"
                        title="Apply recommendation and realize savings"
                      >
                        <CheckCircle2 size={14} /> Apply
                      </button>
                      <button
                        onClick={() => handleUpdateStatus(rec.id, 'DISMISSED')}
                        disabled={updatingId === rec.id}
                        className="btn btn-secondary btn-sm"
                        title="Dismiss this recommendation"
                      >
                        <XCircle size={14} /> Dismiss
                      </button>
                    </>
                  ) : (
                    <span style={{ fontSize: '0.82rem', color: 'var(--text-muted)' }}>
                      Status: <strong>{rec.status}</strong>
                    </span>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Manual Recommendation Modal */}
      {showModal && (
        <div className="modal-backdrop">
          <div className="modal-card">
            <div className="modal-header">
              <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                <DollarSign size={20} color="var(--primary)" />
                <h3 style={{ fontSize: '1.1rem' }}>Create Optimization Recommendation</h3>
              </div>
              <button onClick={() => setShowModal(false)} style={{ background: 'none', border: 'none', color: 'var(--text-muted)', cursor: 'pointer' }}>
                <X size={20} />
              </button>
            </div>

            {error && (
              <div style={{ margin: '16px 24px 0', padding: '10px 14px', borderRadius: 8, background: 'var(--danger-bg)', color: 'var(--danger)', fontSize: '0.84rem' }}>
                {error}
              </div>
            )}

            <form onSubmit={handleCreateRecommendation}>
              <div className="modal-body">
                <div className="form-group">
                  <label className="form-label">Scope</label>
                  <div style={{ display: 'flex', gap: 12 }}>
                    <label style={{ display: 'flex', alignItems: 'center', gap: 6, cursor: 'pointer', fontSize: '0.88rem' }}>
                      <input type="radio" name="recScope" checked={recScope === 'resource'} onChange={() => setRecScope('resource')} />
                      Resource Level
                    </label>
                    <label style={{ display: 'flex', alignItems: 'center', gap: 6, cursor: 'pointer', fontSize: '0.88rem' }}>
                      <input type="radio" name="recScope" checked={recScope === 'account'} onChange={() => setRecScope('account')} />
                      Account Level
                    </label>
                  </div>
                </div>

                {recScope === 'resource' ? (
                  <div className="form-group">
                    <label className="form-label">Target Cloud Resource</label>
                    {resources.length === 0 ? (
                      <p style={{ color: 'var(--warning)', fontSize: '0.84rem' }}>No cloud resources found. Please add a resource in Cloud Inventory first.</p>
                    ) : (
                      <select
                        className="form-select"
                        value={selectedResourceId || resources[0]?.id}
                        onChange={(e) => setSelectedResourceId(e.target.value)}
                      >
                        {resources.map(r => (
                          <option key={r.id} value={r.id}>{r.resourceName} ({r.serviceName})</option>
                        ))}
                      </select>
                    )}
                  </div>
                ) : (
                  <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
                    <div className="form-group">
                      <label className="form-label">AWS Account</label>
                      {accounts.length === 0 ? (
                        <p style={{ color: 'var(--warning)', fontSize: '0.84rem' }}>No AWS accounts linked.</p>
                      ) : (
                        <select className="form-select" value={selectedAccountId || accounts[0]?.id} onChange={(e) => setSelectedAccountId(e.target.value)}>
                          {accounts.map(acc => (
                            <option key={acc.id} value={acc.id}>{acc.accountName}</option>
                          ))}
                        </select>
                      )}
                    </div>
                    <div className="form-group">
                      <label className="form-label">Service</label>
                      <select className="form-select" value={selectedServiceName} onChange={(e) => setSelectedServiceName(e.target.value)}>
                        <option value="EC2">EC2 (Compute)</option>
                        <option value="RDS">RDS (Databases)</option>
                        <option value="S3">S3 (Storage)</option>
                        <option value="LAMBDA">Lambda (Serverless)</option>
                        <option value="DYNAMODB">DynamoDB (NoSQL)</option>
                        <option value="EKS">EKS (Kubernetes)</option>
                        <option value="ECS">ECS (Containers)</option>
                        <option value="CLOUDFRONT">CloudFront (CDN)</option>
                        <option value="ELB">ELB (Load Balancing)</option>
                        <option value="REDSHIFT">Redshift (Data Warehouse)</option>
                        <option value="ELASTICACHE">ElastiCache (In-Memory)</option>
                        <option value="EBS">EBS (Block Storage)</option>
                        <option value="SAGEMAKER">SageMaker (ML/AI)</option>
                        <option value="BEDROCK">Bedrock (GenAI)</option>
                      </select>
                    </div>
                  </div>
                )}

                <div className="form-group">
                  <label className="form-label">Optimization Type</label>
                  <select className="form-select" value={recommendationType} onChange={(e) => setRecommendationType(e.target.value)}>
                    <option value="RIGHTSIZE_INSTANCE">Rightsize Instance (Downsize tier)</option>
                    <option value="TERMINATE_IDLE_RESOURCE">Terminate Idle / Zombie Resource</option>
                    <option value="PURCHASE_RESERVED_INSTANCE">Purchase Compute Savings Plan / RI</option>
                    <option value="STORAGE_TIER_OPTIMIZATION">Storage Tier Optimization (S3 Glacier / gp3)</option>
                  </select>
                </div>

                <div className="form-group">
                  <label className="form-label">Recommendation Title</label>
                  <input
                    type="text"
                    required
                    placeholder="e.g. Downsize c5.2xlarge to c5.large"
                    className="form-input"
                    value={title}
                    onChange={(e) => setTitle(e.target.value)}
                  />
                </div>

                <div className="form-group">
                  <label className="form-label">Detailed Description & Action Plan</label>
                  <textarea
                    rows={3}
                    required
                    placeholder="Explain the technical rationalization and steps to implement the change."
                    className="form-input"
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                  />
                </div>

                <div className="form-group">
                  <label className="form-label">Estimated Monthly Savings ($ USD / month)</label>
                  <input
                    type="number"
                    step="0.01"
                    min="0"
                    required
                    placeholder="45.00"
                    className="form-input"
                    value={estimatedMonthlySaving}
                    onChange={(e) => setEstimatedMonthlySaving(e.target.value)}
                  />
                </div>
              </div>

              <div className="modal-footer">
                <button type="button" onClick={() => setShowModal(false)} className="btn btn-secondary">Cancel</button>
                <button type="submit" disabled={submitting} className="btn btn-primary">
                  {submitting ? 'Saving...' : 'Add Recommendation'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
