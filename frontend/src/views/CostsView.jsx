import React, { useState, useEffect, useMemo } from 'react';
import { api } from '../api/client';
import { Line } from 'react-chartjs-2';
import { TrendingUp, Plus, Sparkles, DollarSign, Calendar, Filter, X, Cloud, ChevronRight } from 'lucide-react';

export default function CostsView({
  costs = [],
  allCosts = [],
  predictions = [],
  allPredictions = [],
  resources = [],
  accounts = [],
  selectedAccountId = '',
  onSelectAccount,
  onRefresh
}) {
  const [showCostModal, setShowCostModal] = useState(false);
  const [showPredModal, setShowPredModal] = useState(false);

  // Ingest Cost Form
  const [costScope, setCostScope] = useState('resource'); // 'resource' or 'account'
  const [selectedResourceId, setSelectedResourceId] = useState('');
  const [targetAccountId, setTargetAccountId] = useState('');
  const [selectedServiceName, setSelectedServiceName] = useState('EC2');
  const [costAmount, setCostAmount] = useState('');
  const [usageDate, setUsageDate] = useState(new Date().toISOString().split('T')[0]);

  // Generate Prediction Form
  const [predScope, setPredScope] = useState('resource');
  const [predResourceId, setPredResourceId] = useState('');
  const [predAccountId, setPredAccountId] = useState('');
  const [predServiceName, setPredServiceName] = useState('EC2');
  const [daysAhead, setDaysAhead] = useState(30);
  const [predictionType, setPredictionType] = useState('LINEAR_REGRESSION');

  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (resources.length > 0 && !selectedResourceId) {
      setSelectedResourceId(resources[0].id.toString());
      setPredResourceId(resources[0].id.toString());
    }
  }, [resources, selectedResourceId]);

  useEffect(() => {
    if (accounts.length > 0 && !targetAccountId) {
      setTargetAccountId(accounts[0].id.toString());
      setPredAccountId(accounts[0].id.toString());
    }
  }, [accounts, targetAccountId]);

  const handleIngestCost = async (e) => {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      const payload = {
        usageDate,
        unitName: 'USD',
        costAmount: parseFloat(costAmount)
      };

      if (costScope === 'resource') {
        const resId = parseInt(selectedResourceId || resources[0]?.id, 10);
        if (isNaN(resId)) {
          throw new Error('Please select an active Cloud Resource or create one first in Cloud Inventory.');
        }
        payload.resourceId = resId;
      } else {
        const accId = parseInt(targetAccountId || accounts[0]?.id, 10);
        if (isNaN(accId)) {
          throw new Error('Please select a linked AWS Account or link one first in AWS Accounts.');
        }
        payload.awsAccountId = accId;
        payload.serviceName = selectedServiceName || 'EC2';
      }

      await api.ingestCost(payload);
      setShowCostModal(false);
      setCostAmount('');
      onRefresh();
    } catch (err) {
      setError(err.message || 'Failed to ingest cost record');
    } finally {
      setSubmitting(false);
    }
  };

  const handleGeneratePrediction = async (e) => {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      const payload = {
        daysAhead: parseInt(daysAhead, 10),
        predictionType
      };

      if (predScope === 'resource') {
        const resId = parseInt(predResourceId || resources[0]?.id, 10);
        if (isNaN(resId)) {
          throw new Error('Please select an active Cloud Resource first.');
        }
        payload.resourceId = resId;
      } else {
        const accId = parseInt(predAccountId || accounts[0]?.id, 10);
        if (isNaN(accId)) {
          throw new Error('Please select a linked AWS Account first.');
        }
        payload.awsAccountId = accId;
        payload.serviceName = predServiceName || 'EC2';
      }

      await api.generatePrediction(payload);
      setShowPredModal(false);
      onRefresh();
    } catch (err) {
      setError(err.message || 'Failed to generate prediction');
    } finally {
      setSubmitting(false);
    }
  };

  // Build Chart Data
  const chartLabels = costs.map(c => c.usageDate);
  const chartValues = costs.map(c => parseFloat(c.costAmount) || 0);

  // Predictions overlay
  predictions.forEach(p => {
    chartLabels.push(`Forecast: ${p.predictionDate}`);
    chartValues.push(parseFloat(p.predictedCost) || 0);
  });

  const chartData = {
    labels: chartLabels.length > 0 ? chartLabels : ['2026-08-01', '2026-08-05', '2026-08-10', '2026-09-01 (Forecast)'],
    datasets: [
      {
        label: 'Historical / Projected Spend ($)',
        data: chartValues.length > 0 ? chartValues : [15, 30, 45, 120],
        borderColor: '#38BDF8',
        backgroundColor: 'rgba(56, 189, 248, 0.15)',
        fill: true,
        tension: 0.3,
        pointBackgroundColor: '#38BDF8',
      }
    ]
  };

  // Account-Wise Summary Computation
  const accountBreakdown = useMemo(() => {
    const dataSourceCosts = allCosts.length > 0 ? allCosts : costs;
    const dataSourcePreds = allPredictions.length > 0 ? allPredictions : predictions;

    return accounts.map(acc => {
      const accCosts = dataSourceCosts.filter(c => c.awsAccountId === acc.id);
      const totalSpend = accCosts.reduce((s, c) => s + (parseFloat(c.costAmount) || 0), 0);
      const accPreds = dataSourcePreds.filter(p => p.awsAccountId === acc.id);
      const latestPred = accPreds.length > 0 ? accPreds[accPreds.length - 1] : null;

      return {
        ...acc,
        spend: totalSpend,
        recordCount: accCosts.length,
        forecast: latestPred ? parseFloat(latestPred.predictedCost) : 0
      };
    });
  }, [accounts, allCosts, costs, allPredictions, predictions]);

  const activeAccount = accounts.find(a => a.id.toString() === selectedAccountId);

  return (
    <div>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 24 }}>
        <div>
          <h2 style={{ fontSize: '1.25rem', fontWeight: 700 }}>Cost Explorer & Spend Forecasting</h2>
          <p style={{ fontSize: '0.84rem', color: 'var(--text-secondary)' }}>
            Track historical usage costs and run AI/ML forecasting models to project future billing
          </p>
        </div>
        <div style={{ display: 'flex', gap: 12 }}>
          <button onClick={() => { setError(null); setShowCostModal(true); }} className="btn btn-secondary">
            <Plus size={16} /> Ingest Cost Record
          </button>
          <button onClick={() => { setError(null); setShowPredModal(true); }} className="btn btn-primary">
            <Sparkles size={16} /> Generate ML Forecast
          </button>
        </div>
      </div>

      {/* Account-Wise Filter Tabs */}
      <div style={{ marginBottom: 20 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 8, flexWrap: 'wrap' }}>
          <button
            onClick={() => onSelectAccount && onSelectAccount('')}
            className={`btn btn-sm ${!selectedAccountId ? 'btn-primary' : 'btn-secondary'}`}
            style={{ display: 'flex', alignItems: 'center', gap: 6 }}
          >
            <Cloud size={14} /> All AWS Accounts
          </button>
          {accountBreakdown.map(acc => {
            const isSelected = selectedAccountId === acc.id.toString();
            return (
              <button
                key={acc.id}
                onClick={() => onSelectAccount && onSelectAccount(acc.id.toString())}
                className={`btn btn-sm ${isSelected ? 'btn-primary' : 'btn-secondary'}`}
                style={{ display: 'flex', alignItems: 'center', gap: 6 }}
              >
                <span>{acc.accountName}</span>
                <span className="pill pill-info" style={{ padding: '2px 6px', fontSize: '0.72rem' }}>
                  ${acc.spend.toFixed(2)}
                </span>
              </button>
            );
          })}
        </div>
      </div>

      {/* Active Account Filter Banner */}
      {selectedAccountId && activeAccount && (
        <div style={{
          marginBottom: 20,
          padding: '10px 16px',
          borderRadius: 8,
          background: 'rgba(56, 189, 248, 0.08)',
          border: '1px solid rgba(56, 189, 248, 0.25)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between'
        }}>
          <span style={{ fontSize: '0.86rem' }}>
            Filtered to AWS Account: <strong>{activeAccount.accountName}</strong> (AWS #{activeAccount.awsAccountNumber})
          </span>
          <button
            onClick={() => onSelectAccount && onSelectAccount('')}
            className="btn btn-secondary btn-sm"
            style={{ fontSize: '0.78rem' }}
          >
            <X size={12} /> Clear Filter
          </button>
        </div>
      )}

      {/* Main Interactive Spend Chart */}
      <div className="card" style={{ marginBottom: 24 }}>
        <h3 style={{ fontSize: '1.05rem', fontWeight: 700, marginBottom: 4 }}>Time-Series Spend & AI Predictions</h3>
        <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginBottom: 20 }}>
          Blue line represents historical billing data leading into ML regression forecasts
        </p>
        <div style={{ height: 300 }}>
          <Line
            data={chartData}
            options={{
              responsive: true,
              maintainAspectRatio: false,
              plugins: { legend: { display: false } },
              scales: {
                x: { grid: { color: 'rgba(255,255,255,0.05)' }, ticks: { color: '#9CA3AF' } },
                y: { grid: { color: 'rgba(255,255,255,0.05)' }, ticks: { color: '#9CA3AF' } }
              }
            }}
          />
        </div>
      </div>

      {/* Predictions Table */}
      <div className="card" style={{ marginBottom: 24 }}>
        <h3 style={{ fontSize: '1.05rem', fontWeight: 700, marginBottom: 16 }}>Generated ML Cost Predictions</h3>
        {predictions.length === 0 ? (
          <p style={{ color: 'var(--text-muted)', fontSize: '0.88rem' }}>
            No predictions generated yet for this scope. Click "Generate ML Forecast" above to run predictive models.
          </p>
        ) : (
          <div className="table-container">
            <table className="custom-table">
              <thead>
                <tr>
                  <th>Prediction ID</th>
                  <th>Target Scope</th>
                  <th>AWS Account</th>
                  <th>Model Algorithm</th>
                  <th>Target Date</th>
                  <th>Predicted Cost</th>
                  <th>Confidence Score</th>
                </tr>
              </thead>
              <tbody>
                {predictions.map(p => (
                  <tr key={p.id}>
                    <td style={{ fontFamily: 'var(--font-mono)', fontWeight: 600 }}>#{p.id}</td>
                    <td>
                      {p.resourceName
                        ? <span className="pill pill-info">Resource: {p.resourceName}</span>
                        : <span className="pill pill-warning">Service: {p.serviceName}</span>}
                    </td>
                    <td style={{ fontFamily: 'var(--font-mono)', fontSize: '0.84rem' }}>
                      {p.awsAccountNumber || '—'}
                    </td>
                    <td style={{ fontWeight: 600 }}>{p.predictionType}</td>
                    <td>{p.predictionDate}</td>
                    <td style={{ fontFamily: 'var(--font-mono)', fontWeight: 700, color: 'var(--primary)' }}>
                      ${parseFloat(p.predictedCost).toFixed(2)} USD
                    </td>
                    <td>
                      <span className="pill pill-success">
                        {(parseFloat(p.confidenceScore) * 100).toFixed(0)}% Confidence
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Cost History Table */}
      <div className="card">
        <h3 style={{ fontSize: '1.05rem', fontWeight: 700, marginBottom: 16 }}>Historical Billing Records</h3>
        {costs.length === 0 ? (
          <p style={{ color: 'var(--text-muted)', fontSize: '0.88rem' }}>No billing records found for this scope.</p>
        ) : (
          <div className="table-container">
            <table className="custom-table">
              <thead>
                <tr>
                  <th>Record ID</th>
                  <th>Scope</th>
                  <th>AWS Account</th>
                  <th>Usage Date</th>
                  <th>Cost Amount</th>
                  <th>Currency</th>
                </tr>
              </thead>
              <tbody>
                {costs.map(c => (
                  <tr key={c.id}>
                    <td style={{ fontFamily: 'var(--font-mono)', fontWeight: 600 }}>#{c.id}</td>
                    <td>
                      {c.resourceName
                        ? <span>Resource: <strong>{c.resourceName}</strong></span>
                        : <span>Service: <strong>{c.serviceName}</strong></span>}
                    </td>
                    <td style={{ fontFamily: 'var(--font-mono)', fontSize: '0.84rem' }}>
                      {c.awsAccountNumber || '—'}
                    </td>
                    <td>{c.usageDate}</td>
                    <td style={{ fontFamily: 'var(--font-mono)', fontWeight: 700 }}>
                      ${parseFloat(c.costAmount).toFixed(2)}
                    </td>
                    <td>{c.unitName || 'USD'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Generate Prediction Modal */}
      {showPredModal && (
        <div className="modal-backdrop">
          <div className="modal-card">
            <div className="modal-header">
              <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                <Sparkles size={20} color="var(--primary)" />
                <h3 style={{ fontSize: '1.1rem' }}>Run ML Cost Forecast</h3>
              </div>
              <button onClick={() => setShowPredModal(false)} style={{ background: 'none', border: 'none', color: 'var(--text-muted)', cursor: 'pointer' }}>
                <X size={20} />
              </button>
            </div>

            {error && (
              <div style={{ margin: '16px 24px 0', padding: '10px 14px', borderRadius: 8, background: 'var(--danger-bg)', color: 'var(--danger)', fontSize: '0.84rem' }}>
                {error}
              </div>
            )}

            <form onSubmit={handleGeneratePrediction}>
              <div className="modal-body">
                <div className="form-group">
                  <label className="form-label">Prediction Scope</label>
                  <div style={{ display: 'flex', gap: 12 }}>
                    <label style={{ display: 'flex', alignItems: 'center', gap: 6, cursor: 'pointer', fontSize: '0.88rem' }}>
                      <input type="radio" name="predScope" checked={predScope === 'resource'} onChange={() => setPredScope('resource')} />
                      Resource Level
                    </label>
                    <label style={{ display: 'flex', alignItems: 'center', gap: 6, cursor: 'pointer', fontSize: '0.88rem' }}>
                      <input type="radio" name="predScope" checked={predScope === 'account'} onChange={() => setPredScope('account')} />
                      Account / Service Level
                    </label>
                  </div>
                </div>

                {predScope === 'resource' ? (
                  <div className="form-group">
                    <label className="form-label">Cloud Resource</label>
                    {resources.length === 0 ? (
                      <p style={{ color: 'var(--warning)', fontSize: '0.84rem' }}>No cloud resources found. Please add a resource in Cloud Inventory first.</p>
                    ) : (
                      <select
                        className="form-select"
                        value={predResourceId || resources[0]?.id}
                        onChange={(e) => setPredResourceId(e.target.value)}
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
                        <p style={{ color: 'var(--warning)', fontSize: '0.84rem' }}>No AWS accounts found.</p>
                      ) : (
                        <select
                          className="form-select"
                          value={predAccountId || accounts[0]?.id}
                          onChange={(e) => setPredAccountId(e.target.value)}
                        >
                          {accounts.map(acc => (
                            <option key={acc.id} value={acc.id}>{acc.accountName}</option>
                          ))}
                        </select>
                      )}
                    </div>
                    <div className="form-group">
                      <label className="form-label">AWS Service</label>
                      <select
                        className="form-select"
                        value={predServiceName}
                        onChange={(e) => setPredServiceName(e.target.value)}
                      >
                        <option value="EC2">EC2 (Elastic Compute)</option>
                        <option value="RDS">RDS (Databases)</option>
                        <option value="S3">S3 (Simple Storage)</option>
                        <option value="LAMBDA">Lambda (Serverless Compute)</option>
                        <option value="DYNAMODB">DynamoDB (NoSQL Database)</option>
                        <option value="EKS">EKS (Kubernetes Service)</option>
                        <option value="ECS">ECS (Container Service)</option>
                        <option value="CLOUDFRONT">CloudFront (CDN)</option>
                        <option value="ELB">ELB (Load Balancing)</option>
                        <option value="REDSHIFT">Redshift (Data Warehouse)</option>
                        <option value="ELASTICACHE">ElastiCache (In-Memory)</option>
                        <option value="EBS">EBS (Block Storage)</option>
                        <option value="SAGEMAKER">SageMaker (ML/AI)</option>
                        <option value="BEDROCK">Bedrock (GenAI Models)</option>
                      </select>
                    </div>
                  </div>
                )}

                <div className="form-group">
                  <label className="form-label">Forecast Time Horizon</label>
                  <select
                    className="form-select"
                    value={daysAhead}
                    onChange={(e) => setDaysAhead(e.target.value)}
                  >
                    <option value={15}>15 Days Ahead</option>
                    <option value={30}>30 Days Ahead (Monthly)</option>
                    <option value={60}>60 Days Ahead</option>
                    <option value={90}>90 Days Ahead (Quarterly)</option>
                  </select>
                </div>

                <div className="form-group">
                  <label className="form-label">Forecasting Model Algorithm</label>
                  <select
                    className="form-select"
                    value={predictionType}
                    onChange={(e) => setPredictionType(e.target.value)}
                  >
                    <option value="LINEAR_REGRESSION">Linear Regression (Trend-based)</option>
                    <option value="MOVING_AVERAGE">Moving Average (Smoothing)</option>
                  </select>
                </div>
              </div>

              <div className="modal-footer">
                <button type="button" onClick={() => setShowPredModal(false)} className="btn btn-secondary">Cancel</button>
                <button type="submit" disabled={submitting} className="btn btn-primary">
                  {submitting ? 'Forecasting...' : 'Generate Prediction'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Ingest Cost Modal */}
      {showCostModal && (
        <div className="modal-backdrop">
          <div className="modal-card">
            <div className="modal-header">
              <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                <DollarSign size={20} color="var(--primary)" />
                <h3 style={{ fontSize: '1.1rem' }}>Ingest Historical Billing Record</h3>
              </div>
              <button onClick={() => setShowCostModal(false)} style={{ background: 'none', border: 'none', color: 'var(--text-muted)', cursor: 'pointer' }}>
                <X size={20} />
              </button>
            </div>

            {error && (
              <div style={{ margin: '16px 24px 0', padding: '10px 14px', borderRadius: 8, background: 'var(--danger-bg)', color: 'var(--danger)', fontSize: '0.84rem' }}>
                {error}
              </div>
            )}

            <form onSubmit={handleIngestCost}>
              <div className="modal-body">
                <div className="form-group">
                  <label className="form-label">Billing Scope</label>
                  <div style={{ display: 'flex', gap: 12 }}>
                    <label style={{ display: 'flex', alignItems: 'center', gap: 6, cursor: 'pointer', fontSize: '0.88rem' }}>
                      <input type="radio" name="costScope" checked={costScope === 'resource'} onChange={() => setCostScope('resource')} />
                      Resource Level
                    </label>
                    <label style={{ display: 'flex', alignItems: 'center', gap: 6, cursor: 'pointer', fontSize: '0.88rem' }}>
                      <input type="radio" name="costScope" checked={costScope === 'account'} onChange={() => setCostScope('account')} />
                      Account / Service Level
                    </label>
                  </div>
                </div>

                {costScope === 'resource' ? (
                  <div className="form-group">
                    <label className="form-label">Cloud Resource</label>
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
                        <select
                          className="form-select"
                          value={targetAccountId || accounts[0]?.id}
                          onChange={(e) => setTargetAccountId(e.target.value)}
                        >
                          {accounts.map(acc => (
                            <option key={acc.id} value={acc.id}>{acc.accountName}</option>
                          ))}
                        </select>
                      )}
                    </div>
                    <div className="form-group">
                      <label className="form-label">AWS Service</label>
                      <select
                        className="form-select"
                        value={selectedServiceName}
                        onChange={(e) => setSelectedServiceName(e.target.value)}
                      >
                        <option value="EC2">EC2 (Elastic Compute)</option>
                        <option value="RDS">RDS (Databases)</option>
                        <option value="S3">S3 (Simple Storage)</option>
                        <option value="LAMBDA">Lambda (Serverless Compute)</option>
                        <option value="DYNAMODB">DynamoDB (NoSQL Database)</option>
                        <option value="EKS">EKS (Kubernetes Service)</option>
                        <option value="ECS">ECS (Container Service)</option>
                        <option value="CLOUDFRONT">CloudFront (CDN)</option>
                        <option value="ELB">ELB (Load Balancing)</option>
                        <option value="REDSHIFT">Redshift (Data Warehouse)</option>
                        <option value="ELASTICACHE">ElastiCache (In-Memory)</option>
                        <option value="EBS">EBS (Block Storage)</option>
                        <option value="SAGEMAKER">SageMaker (ML/AI)</option>
                        <option value="BEDROCK">Bedrock (GenAI Models)</option>
                      </select>
                    </div>
                  </div>
                )}

                <div className="form-group">
                  <label className="form-label">Usage Date</label>
                  <input
                    type="date"
                    required
                    className="form-input"
                    value={usageDate}
                    onChange={(e) => setUsageDate(e.target.value)}
                  />
                </div>

                <div className="form-group">
                  <label className="form-label">Cost Amount ($ USD)</label>
                  <input
                    type="number"
                    step="0.01"
                    min="0"
                    required
                    placeholder="75.00"
                    className="form-input"
                    value={costAmount}
                    onChange={(e) => setCostAmount(e.target.value)}
                  />
                </div>
              </div>

              <div className="modal-footer">
                <button type="button" onClick={() => setShowCostModal(false)} className="btn btn-secondary">Cancel</button>
                <button type="submit" disabled={submitting} className="btn btn-primary">
                  {submitting ? 'Ingesting...' : 'Save Billing Record'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
