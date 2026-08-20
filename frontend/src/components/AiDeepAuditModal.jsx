import React, { useState, useEffect } from 'react';
import { api } from '../api/client';
import { Sparkles, X, Shield, ArrowRight, TrendingDown, Check, Copy, Terminal, Layers, AlertTriangle, Cpu, CheckCircle2 } from 'lucide-react';

export default function AiDeepAuditModal({ resourceId, onClose }) {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [copiedCli, setCopiedCli] = useState(false);
  const [copiedTf, setCopiedTf] = useState(false);

  useEffect(() => {
    if (!resourceId) return;
    const fetchAudit = async () => {
      setLoading(true);
      setError(null);
      try {
        const audit = await api.aiAnalyzeResource(resourceId);
        setData(audit);
      } catch (err) {
        setError(err.message || 'Failed to fetch AI audit');
      } finally {
        setLoading(false);
      }
    };
    fetchAudit();
  }, [resourceId]);

  if (!resourceId) return null;

  const copyCli = () => {
    if (!data?.cliSnippet) return;
    navigator.clipboard.writeText(data.cliSnippet);
    setCopiedCli(true);
    setTimeout(() => setCopiedCli(false), 2000);
  };

  const copyTf = () => {
    if (!data?.terraformSnippet) return;
    navigator.clipboard.writeText(data.terraformSnippet);
    setCopiedTf(true);
    setTimeout(() => setCopiedTf(false), 2000);
  };

  return (
    <div className="modal-backdrop" style={{ zIndex: 1100 }}>
      <div className="modal-card" style={{ maxWidth: 720, maxHeight: '90vh', overflowY: 'auto' }}>
        {/* Header */}
        <div style={{
          padding: '20px 24px',
          borderBottom: '1px solid var(--border-subtle)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          background: 'linear-gradient(135deg, rgba(56, 189, 248, 0.1) 0%, rgba(99, 102, 241, 0.05) 100%)'
        }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
            <div style={{
              width: 38,
              height: 38,
              borderRadius: 8,
              background: 'linear-gradient(135deg, #38BDF8, #6366F1)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              boxShadow: '0 0 16px rgba(56, 189, 248, 0.4)'
            }}>
              <Sparkles size={20} color="#fff" />
            </div>
            <div>
              <h3 style={{ fontSize: '1.15rem', fontWeight: 700, display: 'flex', alignItems: 'center', gap: 8 }}>
                AI Deep Optimization Audit
                <span className="pill pill-primary" style={{ fontSize: '0.68rem' }}>
                  Google Gemini 2.0
                </span>
              </h3>
              <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                Multi-dimensional CloudWatch telemetry & architecture modernization analysis
              </p>
            </div>
          </div>
          <button onClick={onClose} style={{ background: 'none', border: 'none', color: 'var(--text-muted)', cursor: 'pointer', padding: 4 }}>
            <X size={20} />
          </button>
        </div>

        {/* Body */}
        <div style={{ padding: '24px' }}>
          {loading ? (
            <div style={{ textAlign: 'center', padding: '40px 0' }}>
              <Sparkles size={32} color="var(--primary)" className="spin" style={{ margin: '0 auto 16px' }} />
              <p style={{ fontWeight: 600, fontSize: '0.95rem' }}>Analyzing CloudWatch Telemetry with AI...</p>
              <p style={{ color: 'var(--text-muted)', fontSize: '0.82rem', marginTop: 4 }}>
                Evaluating CPU P95 distribution, memory headroom, Graviton compatibility, and pricing models
              </p>
            </div>
          ) : error ? (
            <div style={{ padding: 16, background: 'var(--danger-bg)', color: 'var(--danger)', borderRadius: 8, fontSize: '0.88rem' }}>
              {error}
            </div>
          ) : data ? (
            <div style={{ display: 'flex', flexDirection: 'column', gap: 20 }}>
              {/* Top Banner: Workload Classification & Risk */}
              <div style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
                gap: 12,
                padding: 16,
                background: 'rgba(15, 23, 42, 0.6)',
                border: '1px solid var(--border-subtle)',
                borderRadius: 10
              }}>
                <div>
                  <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>Workload Type</div>
                  <div style={{ fontSize: '0.92rem', fontWeight: 700, color: 'var(--primary)', marginTop: 2 }}>{data.workloadClassification}</div>
                </div>
                <div>
                  <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>Migration Risk</div>
                  <div style={{ marginTop: 2 }}>
                    <span className={`pill ${data.riskLevel === 'LOW' ? 'pill-success' : data.riskLevel === 'MEDIUM' ? 'pill-warning' : 'pill-danger'}`} style={{ fontSize: '0.75rem', fontWeight: 700 }}>
                      {data.riskLevel} RISK
                    </span>
                  </div>
                </div>
                <div>
                  <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>Monthly Savings</div>
                  <div style={{ fontSize: '1.05rem', fontWeight: 700, color: 'var(--success)', marginTop: 2 }}>
                    ${parseFloat(data.estimatedMonthlySavings || 0).toFixed(2)} <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>/mo</span>
                  </div>
                </div>
                <div>
                  <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>Annual Impact (ROI)</div>
                  <div style={{ fontSize: '1.05rem', fontWeight: 700, color: '#38BDF8', marginTop: 2 }}>
                    ${parseFloat(data.estimatedAnnualSavings || 0).toFixed(2)} <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>/yr</span>
                  </div>
                </div>
              </div>

              {/* Architecture Shift */}
              <div className="card" style={{ padding: 16 }}>
                <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)', fontWeight: 600, marginBottom: 10, textTransform: 'uppercase' }}>
                  Architecture Modernization Plan
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: 14, flexWrap: 'wrap' }}>
                  <div style={{
                    flex: 1,
                    minWidth: 180,
                    padding: '12px 14px',
                    background: 'rgba(30, 41, 59, 0.5)',
                    border: '1px solid var(--border-subtle)',
                    borderRadius: 8
                  }}>
                    <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)' }}>Current Architecture</div>
                    <div style={{ fontSize: '0.88rem', fontWeight: 600, color: '#E2E8F0', marginTop: 2 }}>{data.currentArchitecture}</div>
                  </div>

                  <div style={{
                    width: 32,
                    height: 32,
                    borderRadius: '50%',
                    background: 'rgba(56, 189, 248, 0.15)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center'
                  }}>
                    <ArrowRight size={16} color="var(--primary)" />
                  </div>

                  <div style={{
                    flex: 1,
                    minWidth: 180,
                    padding: '12px 14px',
                    background: 'rgba(16, 185, 129, 0.08)',
                    border: '1px solid rgba(16, 185, 129, 0.3)',
                    borderRadius: 8
                  }}>
                    <div style={{ fontSize: '0.72rem', color: 'var(--success)' }}>Recommended Target</div>
                    <div style={{ fontSize: '0.88rem', fontWeight: 700, color: '#A7F3D0', marginTop: 2 }}>{data.targetArchitecture}</div>
                  </div>
                </div>
              </div>

              {/* Technical Rationale & Performance Impact */}
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: 14 }}>
                <div className="card" style={{ padding: 16 }}>
                  <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)', fontWeight: 600, marginBottom: 6, textTransform: 'uppercase' }}>
                    Telemetry & Technical Rationale
                  </div>
                  <p style={{ fontSize: '0.84rem', lineHeight: 1.5, color: '#E2E8F0' }}>{data.technicalRationale}</p>
                </div>

                <div className="card" style={{ padding: 16 }}>
                  <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)', fontWeight: 600, marginBottom: 6, textTransform: 'uppercase' }}>
                    Performance & Throughput Delta
                  </div>
                  <p style={{ fontSize: '0.84rem', lineHeight: 1.5, color: '#BAE6FD' }}>{data.performanceImpact}</p>
                </div>
              </div>

              {/* AWS CLI Command */}
              {data.cliSnippet && (
                <div className="card" style={{ padding: 16, background: '#0F172A' }}>
                  <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 8 }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 6, fontSize: '0.78rem', color: 'var(--primary)', fontWeight: 600 }}>
                      <Terminal size={14} /> Executable AWS CLI Command
                    </div>
                    <button
                      onClick={copyCli}
                      className="btn btn-secondary btn-sm"
                      style={{ padding: '3px 8px', fontSize: '0.72rem' }}
                    >
                      {copiedCli ? <><Check size={12} /> Copied</> : <><Copy size={12} /> Copy CLI</>}
                    </button>
                  </div>
                  <pre style={{
                    margin: 0,
                    padding: '10px 12px',
                    background: 'rgba(0,0,0,0.4)',
                    border: '1px solid rgba(255,255,255,0.08)',
                    borderRadius: 6,
                    fontSize: '0.78rem',
                    fontFamily: 'var(--font-mono)',
                    color: '#F8FAFC',
                    overflowX: 'auto',
                    lineHeight: 1.45
                  }}>
                    <code>{data.cliSnippet}</code>
                  </pre>
                </div>
              )}

              {/* Terraform Module */}
              {data.terraformSnippet && (
                <div className="card" style={{ padding: 16, background: '#0F172A' }}>
                  <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 8 }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 6, fontSize: '0.78rem', color: '#A78BFA', fontWeight: 600 }}>
                      <Layers size={14} /> Terraform Configuration (HCL)
                    </div>
                    <button
                      onClick={copyTf}
                      className="btn btn-secondary btn-sm"
                      style={{ padding: '3px 8px', fontSize: '0.72rem' }}
                    >
                      {copiedTf ? <><Check size={12} /> Copied</> : <><Copy size={12} /> Copy HCL</>}
                    </button>
                  </div>
                  <pre style={{
                    margin: 0,
                    padding: '10px 12px',
                    background: 'rgba(0,0,0,0.4)',
                    border: '1px solid rgba(255,255,255,0.08)',
                    borderRadius: 6,
                    fontSize: '0.78rem',
                    fontFamily: 'var(--font-mono)',
                    color: '#F8FAFC',
                    overflowX: 'auto',
                    lineHeight: 1.45
                  }}>
                    <code>{data.terraformSnippet}</code>
                  </pre>
                </div>
              )}

              {/* Rollback & Safety Plan */}
              {data.rollbackPlan && data.rollbackPlan.length > 0 && (
                <div className="card" style={{ padding: 16, borderLeft: '4px solid var(--warning)' }}>
                  <div style={{ fontSize: '0.78rem', color: 'var(--warning)', fontWeight: 700, marginBottom: 8, display: 'flex', alignItems: 'center', gap: 6 }}>
                    <AlertTriangle size={15} /> Step-by-Step Rollback & Safety Guardrails
                  </div>
                  <ul style={{ margin: 0, paddingLeft: 18, fontSize: '0.82rem', color: 'var(--text-secondary)', display: 'flex', flexDirection: 'column', gap: 4 }}>
                    {data.rollbackPlan.map((step, sIdx) => (
                      <li key={sIdx}>{step}</li>
                    ))}
                  </ul>
                </div>
              )}
            </div>
          ) : null}
        </div>
      </div>
    </div>
  );
}
