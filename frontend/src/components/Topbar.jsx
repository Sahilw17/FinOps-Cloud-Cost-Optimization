import React from 'react';
import { Zap, ShieldAlert, RefreshCw, Cloud, Sparkles } from 'lucide-react';

export default function Topbar({
  title,
  subtitle,
  accounts = [],
  selectedAccountId = '',
  onSelectAccount,
  onRefresh,
  refreshing,
  onQuickAnomalyScan,
  scanningAnomalies,
  onQuickRightsizing,
  analyzingRightsizing,
  onOpenAiCopilot
}) {
  return (
    <header className="topbar">
      <div>
        <h2 style={{ fontSize: '1.25rem', fontWeight: 700 }}>{title}</h2>
        {subtitle && <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>{subtitle}</p>}
      </div>

      <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
        {/* Global AWS Account Selector */}
        <div style={{ display: 'flex', alignItems: 'center', gap: 6, background: 'var(--bg-card)', padding: '4px 10px', borderRadius: 8, border: '1px solid var(--border-subtle)' }}>
          <Cloud size={16} color="var(--primary)" />
          <select
            className="form-select"
            style={{ width: 'auto', padding: '4px 8px', fontSize: '0.82rem', fontWeight: 600, border: 'none', background: 'transparent', cursor: 'pointer' }}
            value={selectedAccountId || ''}
            onChange={(e) => onSelectAccount(e.target.value)}
          >
            <option value="">All AWS Accounts (Company-wide)</option>
            {accounts.map(acc => (
              <option key={acc.id} value={acc.id.toString()}>
                {acc.accountName} ({acc.awsAccountNumber})
              </option>
            ))}
          </select>
        </div>

        <button
          onClick={onOpenAiCopilot}
          className="btn btn-sm"
          style={{
            background: 'linear-gradient(135deg, #0284C7, #4F46E5)',
            border: '1px solid rgba(56, 189, 248, 0.4)',
            color: '#fff',
            fontWeight: 700,
            display: 'flex',
            alignItems: 'center',
            gap: 6,
            boxShadow: '0 0 14px rgba(56, 189, 248, 0.35)'
          }}
          title="Open FinOps AI Copilot (Powered by Google Gemini)"
        >
          <Sparkles size={14} color="#BAE6FD" />
          AI Copilot
        </button>

        <button
          onClick={onQuickAnomalyScan}
          disabled={scanningAnomalies}
          className="btn btn-secondary btn-sm"
          title="Run real-time anomaly detection scan"
        >
          <ShieldAlert size={14} color="var(--danger)" />
          {scanningAnomalies ? 'Scanning...' : 'Anomaly Scan'}
        </button>

        <button
          onClick={onQuickRightsizing}
          disabled={analyzingRightsizing}
          className="btn btn-secondary btn-sm"
          title="Run automated rightsizing analysis"
        >
          <Zap size={14} color="var(--warning)" />
          {analyzingRightsizing ? 'Analyzing...' : 'Rightsizing Scan'}
        </button>

        <button
          onClick={onRefresh}
          disabled={refreshing}
          className="btn btn-secondary btn-sm"
          title="Refresh dashboard data"
        >
          <RefreshCw size={14} className={refreshing ? 'spin' : ''} />
          Refresh
        </button>
      </div>
    </header>
  );
}
