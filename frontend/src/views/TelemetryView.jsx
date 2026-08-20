import React, { useState, useEffect } from 'react';
import { api } from '../api/client';
import { Line } from 'react-chartjs-2';
import { Activity, Plus, Server, Cpu, HardDrive, Network, X } from 'lucide-react';

export default function TelemetryView({ metrics = [], resources = [], onRefresh }) {
  const [showModal, setShowModal] = useState(false);
  const [selectedResourceId, setSelectedResourceId] = useState('');
  const [metricName, setMetricName] = useState('CPUUtilization');
  const [metricValue, setMetricValue] = useState('45.0');
  const [statistic, setStatistic] = useState('Average');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (resources.length > 0 && !selectedResourceId) {
      setSelectedResourceId(resources[0].id.toString());
    }
  }, [resources, selectedResourceId]);

  const handleIngest = async (e) => {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      const resId = parseInt(selectedResourceId || resources[0]?.id, 10);
      if (isNaN(resId)) {
        throw new Error('Please select an active Cloud Resource or create one first in Cloud Inventory.');
      }

      const payload = {
        resourceId: resId,
        metricName,
        metricValue: parseFloat(metricValue),
        statistic,
        collectedAt: new Date().toISOString()
      };
      await api.ingestMetric(payload);
      setShowModal(false);
      onRefresh();
    } catch (err) {
      setError(err.message || 'Failed to ingest metric');
    } finally {
      setSubmitting(false);
    }
  };

  // Group metrics for chart
  const cpuMetrics = metrics.filter(m => m.metricName === 'CPUUtilization');
  const chartLabels = cpuMetrics.map(m => m.collectedAt ? m.collectedAt.substring(11, 19) : 'Time');
  const chartData = {
    labels: chartLabels.length > 0 ? chartLabels : ['10:00', '10:05', '10:10', '10:15', '10:20'],
    datasets: [
      {
        label: 'CPU Utilization (%)',
        data: cpuMetrics.length > 0 ? cpuMetrics.map(m => parseFloat(m.metricValue)) : [12, 15, 8.5, 45, 94.2],
        borderColor: '#10B981',
        backgroundColor: 'rgba(16, 185, 129, 0.15)',
        fill: true,
        tension: 0.35,
        pointBackgroundColor: '#10B981',
      }
    ]
  };

  return (
    <div>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 24 }}>
        <div>
          <h2 style={{ fontSize: '1.25rem', fontWeight: 700 }}>Telemetry Performance Visualizer</h2>
          <p style={{ fontSize: '0.84rem', color: 'var(--text-secondary)' }}>
            Real-time CloudWatch telemetry streams (CPU, Memory, Disk, Network) across active instances
          </p>
        </div>
        <button onClick={() => { setError(null); setShowModal(true); }} className="btn btn-primary">
          <Plus size={16} /> Ingest Metric Stream
        </button>
      </div>

      {/* Real-time Line Chart */}
      <div className="card" style={{ marginBottom: 24 }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 16 }}>
          <div>
            <h3 style={{ fontSize: '1.05rem', fontWeight: 700 }}>Live CPU Utilization Stream</h3>
            <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Percentile telemetry observations</p>
          </div>
          <span className="pill pill-success">Live Metric Stream</span>
        </div>
        <div style={{ height: 260 }}>
          <Line
            data={chartData}
            options={{
              responsive: true,
              maintainAspectRatio: false,
              plugins: { legend: { display: false } },
              scales: {
                x: { grid: { color: 'rgba(255,255,255,0.05)' }, ticks: { color: '#9CA3AF' } },
                y: { grid: { color: 'rgba(255,255,255,0.05)' }, ticks: { color: '#9CA3AF' }, min: 0, max: 100 }
              }
            }}
          />
        </div>
      </div>

      {/* Telemetry Data Table */}
      <div className="card">
        <h3 style={{ fontSize: '1.05rem', fontWeight: 700, marginBottom: 16 }}>Raw Telemetry Log</h3>
        {metrics.length === 0 ? (
          <p style={{ color: 'var(--text-muted)', fontSize: '0.88rem' }}>No telemetry points ingested yet.</p>
        ) : (
          <div className="table-container">
            <table className="custom-table">
              <thead>
                <tr>
                  <th>Metric ID</th>
                  <th>Target Resource</th>
                  <th>Metric Name</th>
                  <th>Statistic</th>
                  <th>Observed Value</th>
                  <th>Unit</th>
                  <th>Timestamp</th>
                </tr>
              </thead>
              <tbody>
                {metrics.map(m => (
                  <tr key={m.id}>
                    <td style={{ fontFamily: 'var(--font-mono)', fontWeight: 600 }}>#{m.id}</td>
                    <td style={{ fontWeight: 600 }}>{m.resourceName}</td>
                    <td>
                      <span className="pill pill-info">{m.metricName}</span>
                    </td>
                    <td>{m.statistic}</td>
                    <td style={{ fontFamily: 'var(--font-mono)', fontWeight: 700, color: 'var(--primary)' }}>
                      {parseFloat(m.metricValue).toFixed(2)}
                    </td>
                    <td>{m.unitName || 'Percent'}</td>
                    <td style={{ color: 'var(--text-secondary)', fontSize: '0.8rem' }}>
                      {m.collectedAt ? m.collectedAt.replace('T', ' ').substring(0, 19) : ''}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Ingest Modal */}
      {showModal && (
        <div className="modal-backdrop">
          <div className="modal-card">
            <div className="modal-header">
              <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                <Activity size={20} color="var(--primary)" />
                <h3 style={{ fontSize: '1.1rem' }}>Ingest Telemetry Data</h3>
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

            <form onSubmit={handleIngest}>
              <div className="modal-body">
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
                        <option key={r.id} value={r.id}>
                          {r.resourceName} ({r.serviceName})
                        </option>
                      ))}
                    </select>
                  )}
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
                  <div className="form-group">
                    <label className="form-label">Metric Name</label>
                    <select
                      className="form-select"
                      value={metricName}
                      onChange={(e) => setMetricName(e.target.value)}
                    >
                      <option value="CPUUtilization">CPUUtilization (%)</option>
                      <option value="MemoryUtilization">MemoryUtilization (%)</option>
                      <option value="NetworkIn">NetworkIn (Bytes)</option>
                      <option value="NetworkOut">NetworkOut (Bytes)</option>
                    </select>
                  </div>

                  <div className="form-group">
                    <label className="form-label">Statistic</label>
                    <select
                      className="form-select"
                      value={statistic}
                      onChange={(e) => setStatistic(e.target.value)}
                    >
                      <option value="Average">Average</option>
                      <option value="Maximum">Maximum</option>
                      <option value="Minimum">Minimum</option>
                    </select>
                  </div>
                </div>

                <div className="form-group">
                  <label className="form-label">Metric Value</label>
                  <input
                    type="number"
                    step="0.1"
                    required
                    placeholder="8.5"
                    className="form-input"
                    value={metricValue}
                    onChange={(e) => setMetricValue(e.target.value)}
                  />
                </div>
              </div>

              <div className="modal-footer">
                <button type="button" onClick={() => setShowModal(false)} className="btn btn-secondary">Cancel</button>
                <button type="submit" disabled={submitting} className="btn btn-primary">
                  {submitting ? 'Ingesting...' : 'Ingest Metric'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
