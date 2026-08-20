import React, { useState } from 'react';
import { api } from '../api/client';
import { Bell, ShieldAlert, CheckCircle2, Eye, Sparkles, AlertTriangle, X } from 'lucide-react';

export default function AlertsView({ alerts = [], onRefresh, onDetect, detecting }) {
  const [filterStatus, setFilterStatus] = useState('ALL');
  const [updatingId, setUpdatingId] = useState(null);

  const filteredAlerts = alerts.filter((a) => {
    if (filterStatus === 'ALL') return true;
    return a.status === filterStatus;
  });

  const unreadCount = alerts.filter(a => a.status === 'UNREAD').length;

  const handleUpdateStatus = async (id, newStatus) => {
    setUpdatingId(id);
    try {
      await api.updateAlertStatus(id, newStatus);
      onRefresh();
    } catch (err) {
      alert(err.message || 'Failed to update alert status');
    } finally {
      setUpdatingId(null);
    }
  };

  return (
    <div>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 24 }}>
        <div>
          <h2 style={{ fontSize: '1.25rem', fontWeight: 700 }}>Real-time Anomaly & Alert Center</h2>
          <p style={{ fontSize: '0.84rem', color: 'var(--text-secondary)' }}>
            Instant detection of sudden billing spikes, budget breaches, and critical telemetry thresholds
          </p>
        </div>
        <button onClick={onDetect} disabled={detecting} className="btn btn-primary">
          <ShieldAlert size={16} /> {detecting ? 'Scanning for Anomalies...' : 'Run Anomaly Scan'}
        </button>
      </div>

      {/* Unread Alert Notification Banner */}
      {unreadCount > 0 && (
        <div
          className="card"
          style={{
            marginBottom: 20,
            background: 'var(--danger-bg)',
            borderColor: 'rgba(239, 68, 68, 0.3)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            padding: '16px 20px'
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
            <AlertTriangle size={22} color="var(--danger)" />
            <div>
              <div style={{ fontWeight: 700, color: 'var(--danger)', fontSize: '0.95rem' }}>
                {unreadCount} Active Unresolved Anomaly Alert{unreadCount > 1 ? 's' : ''}
              </div>
              <div style={{ fontSize: '0.82rem', color: 'var(--text-secondary)' }}>
                Immediate review recommended to avoid unexpected cost overruns or instance failures.
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Filter Tabs */}
      <div style={{ display: 'flex', gap: 8, marginBottom: 20 }}>
        {['ALL', 'UNREAD', 'READ', 'RESOLVED'].map((status) => (
          <button
            key={status}
            onClick={() => setFilterStatus(status)}
            className={`btn btn-sm ${filterStatus === status ? 'btn-primary' : 'btn-secondary'}`}
          >
            {status}
          </button>
        ))}
      </div>

      {/* Alerts List */}
      {filteredAlerts.length === 0 ? (
        <div className="card" style={{ textAlign: 'center', padding: '48px 20px' }}>
          <Bell size={42} color="var(--text-muted)" style={{ margin: '0 auto 16px' }} />
          <h3 style={{ fontSize: '1.1rem', marginBottom: 6 }}>No Alerts Found</h3>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.88rem', marginBottom: 20 }}>
            Click "Run Anomaly Scan" above to evaluate recent billing logs and performance telemetry.
          </p>
          <button onClick={onDetect} disabled={detecting} className="btn btn-primary">
            <ShieldAlert size={16} /> Run Anomaly Scan
          </button>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
          {filteredAlerts.map((alert) => (
            <div
              key={alert.id}
              className="card"
              style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                gap: 20,
                borderLeft: alert.status === 'UNREAD' ? '4px solid var(--danger)' : '4px solid var(--border-card)'
              }}
            >
              <div style={{ flex: 1 }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 6 }}>
                  <span className={`pill ${alert.alertType === 'COST_SPIKE_DETECTED' ? 'pill-danger' : 'pill-warning'}`}>
                    {alert.alertType}
                  </span>
                  <span className={`pill ${alert.status === 'UNREAD' ? 'pill-danger' : alert.status === 'RESOLVED' ? 'pill-success' : 'pill-info'}`}>
                    {alert.status}
                  </span>
                  <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                    Target: <strong>{alert.resourceName || alert.awsAccountNumber || 'Account'}</strong>
                  </span>
                </div>
                <h4 style={{ fontSize: '1.05rem', fontWeight: 700, marginBottom: 4 }}>{alert.title}</h4>
                <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>{alert.message}</p>
                {alert.thresholdValue && alert.actualValue && (
                  <div style={{ display: 'flex', gap: 16, marginTop: 8, fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                    <span>Threshold: <strong style={{ color: 'var(--text-primary)' }}>{parseFloat(alert.thresholdValue).toFixed(2)}</strong></span>
                    <span>Observed Value: <strong style={{ color: 'var(--danger)' }}>{parseFloat(alert.actualValue).toFixed(2)} {alert.unitName}</strong></span>
                  </div>
                )}
              </div>

              <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                {alert.status === 'UNREAD' && (
                  <button
                    onClick={() => handleUpdateStatus(alert.id, 'READ')}
                    disabled={updatingId === alert.id}
                    className="btn btn-secondary btn-sm"
                  >
                    <Eye size={14} /> Mark Read
                  </button>
                )}

                {alert.status !== 'RESOLVED' && (
                  <button
                    onClick={() => handleUpdateStatus(alert.id, 'RESOLVED')}
                    disabled={updatingId === alert.id}
                    className="btn btn-success btn-sm"
                  >
                    <CheckCircle2 size={14} /> Resolve
                  </button>
                )}

                {alert.status === 'RESOLVED' && (
                  <span className="pill pill-success">
                    <CheckCircle2 size={13} /> Resolved
                  </span>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
