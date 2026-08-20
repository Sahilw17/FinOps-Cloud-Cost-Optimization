import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { Cloud, Lock, Mail, User, ShieldCheck, ArrowRight, Sparkles, Zap, Eye } from 'lucide-react';

export default function LoginModal() {
  const { login, register } = useAuth();
  const [isRegister, setIsRegister] = useState(false);
  const [name, setName] = useState('');
  const [email, setEmail] = useState('finops_alert_3060@example.com');
  const [password, setPassword] = useState('Password123!');
  const [roleName, setRoleName] = useState('ADMIN');
  const [error, setError] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      if (isRegister) {
        await register(name, email, password, roleName);
      } else {
        await login(email, password);
      }
    } catch (err) {
      setError(err.message || 'Authentication failed');
    } finally {
      setSubmitting(false);
    }
  };

  const quickLoginAs = (targetRole, defaultName) => {
    const randomSuffix = Math.floor(1000 + Math.random() * 9000);
    setIsRegister(true);
    setName(defaultName);
    setEmail(`${targetRole.toLowerCase()}_${randomSuffix}@finops.io`);
    setPassword('Password123!');
    setRoleName(targetRole);
  };

  return (
    <div className="modal-backdrop">
      <div className="modal-card" style={{ maxWidth: 460 }}>
        <div style={{ padding: '28px 32px 20px', textAlign: 'center' }}>
          <div style={{
            width: 48,
            height: 48,
            borderRadius: 12,
            background: 'linear-gradient(135deg, #38BDF8 0%, #6366F1 100%)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            margin: '0 auto 16px',
            boxShadow: '0 8px 24px rgba(56, 189, 248, 0.3)'
          }}>
            <Cloud size={26} color="#fff" />
          </div>
          <h2 style={{ fontSize: '1.45rem', marginBottom: 6 }}>
            {isRegister ? 'Create Account' : 'FinOps Intelligence Portal'}
          </h2>
          <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
            {isRegister
              ? 'Select your role (Admin, FinOps Engineer, or Viewer)'
              : 'Sign in to access your cloud cost intelligence workspace'}
          </p>
        </div>

        {error && (
          <div style={{
            margin: '0 32px 16px',
            padding: '10px 14px',
            borderRadius: 8,
            background: 'var(--danger-bg)',
            border: '1px solid rgba(239, 68, 68, 0.3)',
            color: 'var(--danger)',
            fontSize: '0.84rem'
          }}>
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} style={{ padding: '0 32px 28px' }}>
          {isRegister && (
            <div className="form-group">
              <label className="form-label">Full Name</label>
              <div style={{ position: 'relative' }}>
                <User size={16} color="var(--text-muted)" style={{ position: 'absolute', left: 12, top: 12 }} />
                <input
                  type="text"
                  required
                  className="form-input"
                  placeholder="Alex Mercer"
                  style={{ width: '100%', paddingLeft: 36 }}
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                />
              </div>
            </div>
          )}

          <div className="form-group">
            <label className="form-label">Work Email</label>
            <div style={{ position: 'relative' }}>
              <Mail size={16} color="var(--text-muted)" style={{ position: 'absolute', left: 12, top: 12 }} />
              <input
                type="email"
                required
                className="form-input"
                placeholder="name@company.com"
                style={{ width: '100%', paddingLeft: 36 }}
                value={email}
                onChange={(e) => setEmail(e.target.value)}
              />
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">Password</label>
            <div style={{ position: 'relative' }}>
              <Lock size={16} color="var(--text-muted)" style={{ position: 'absolute', left: 12, top: 12 }} />
              <input
                type="password"
                required
                className="form-input"
                placeholder="••••••••"
                style={{ width: '100%', paddingLeft: 36 }}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
              />
            </div>
          </div>

          {isRegister && (
            <div className="form-group">
              <label className="form-label">Account Role</label>
              <select
                className="form-select"
                value={roleName}
                onChange={(e) => setRoleName(e.target.value)}
              >
                <option value="ADMIN">ADMIN (Full Platform & User Management)</option>
                <option value="FINOPS_ENGINEER">FINOPS_ENGINEER (Cost Optimization & Rightsizing)</option>
                <option value="USER">USER (Viewer Stakeholder)</option>
              </select>
            </div>
          )}

          <button
            type="submit"
            disabled={submitting}
            className="btn btn-primary"
            style={{ width: '100%', marginTop: 8, padding: '12px 16px' }}
          >
            {submitting ? 'Authenticating...' : (
              <>
                {isRegister ? 'Create Account' : 'Sign In'} <ArrowRight size={16} />
              </>
            )}
          </button>

          <div style={{ textAlign: 'center', marginTop: 14 }}>
            <button
              type="button"
              onClick={() => { setIsRegister(!isRegister); setError(null); }}
              style={{ background: 'none', border: 'none', color: 'var(--primary)', cursor: 'pointer', fontSize: '0.84rem' }}
            >
              {isRegister ? 'Already have an account? Sign in' : "Don't have an account? Register new user"}
            </button>
          </div>

          {/* 1-Click Demo Shortcut Roles */}
          <div style={{ marginTop: 20, paddingTop: 16, borderTop: '1px solid var(--border-subtle)' }}>
            <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginBottom: 8, textAlign: 'center', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
              Quick 1-Click Role Switcher
            </div>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: 8 }}>
              <button
                type="button"
                onClick={() => quickLoginAs('ADMIN', 'Enterprise Admin')}
                style={{
                  background: 'rgba(56, 189, 248, 0.1)',
                  border: '1px solid rgba(56, 189, 248, 0.3)',
                  borderRadius: 6,
                  padding: '6px 4px',
                  color: 'var(--primary)',
                  cursor: 'pointer',
                  fontSize: '0.75rem',
                  fontWeight: 600,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  gap: 4
                }}
              >
                <Sparkles size={12} /> Admin
              </button>

              <button
                type="button"
                onClick={() => quickLoginAs('FINOPS_ENGINEER', 'Lead FinOps Engineer')}
                style={{
                  background: 'rgba(16, 185, 129, 0.1)',
                  border: '1px solid rgba(16, 185, 129, 0.3)',
                  borderRadius: 6,
                  padding: '6px 4px',
                  color: 'var(--success)',
                  cursor: 'pointer',
                  fontSize: '0.75rem',
                  fontWeight: 600,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  gap: 4
                }}
              >
                <Zap size={12} /> Engineer
              </button>

              <button
                type="button"
                onClick={() => quickLoginAs('USER', 'Business Viewer')}
                style={{
                  background: 'rgba(99, 102, 241, 0.1)',
                  border: '1px solid rgba(99, 102, 241, 0.3)',
                  borderRadius: 6,
                  padding: '6px 4px',
                  color: 'var(--info)',
                  cursor: 'pointer',
                  fontSize: '0.75rem',
                  fontWeight: 600,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  gap: 4
                }}
              >
                <Eye size={12} /> Viewer
              </button>
            </div>
          </div>
        </form>
      </div>
    </div>
  );
}
