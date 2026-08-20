import React, { useState } from 'react';
import { api } from '../api/client';
import { KeyRound, Plus, ShieldCheck, CheckCircle2, Lock, ArrowRight, X } from 'lucide-react';

export default function AccountsView({ accounts = [], onRefresh }) {
  const [showModal, setShowModal] = useState(false);
  const [awsAccountNumber, setAwsAccountNumber] = useState('');
  const [accountName, setAccountName] = useState('');
  const [accessKey, setAccessKey] = useState('');
  const [secretKey, setSecretKey] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);

  const handleLink = async (e) => {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      await api.linkAccount(awsAccountNumber, accountName, accessKey, secretKey);
      setShowModal(false);
      setAwsAccountNumber('');
      setAccountName('');
      setAccessKey('');
      setSecretKey('');
      onRefresh();
    } catch (err) {
      setError(err.message || 'Failed to link AWS account');
    } finally {
      setSubmitting(false);
    }
  };

  const fillSampleAccount = () => {
    const randAcc = Math.floor(100000000000 + Math.random() * 900000000000).toString();
    const randKey = 'AKIA' + Math.floor(100000000000 + Math.random() * 900000000000).toString();
    setAwsAccountNumber(randAcc);
    setAccountName('Production-Workload-AWS');
    setAccessKey(randKey);
    setSecretKey('wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY');
  };

  return (
    <div>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 24 }}>
        <div>
          <h2 style={{ fontSize: '1.25rem', fontWeight: 700 }}>Linked AWS Cloud Accounts</h2>
          <p style={{ fontSize: '0.84rem', color: 'var(--text-secondary)' }}>
            Manage your AWS multi-account environment with AES-256 encrypted IAM credentials
          </p>
        </div>
        <button onClick={() => setShowModal(true)} className="btn btn-primary">
          <Plus size={16} /> Link AWS Account
        </button>
      </div>

      <div className="card">
        {accounts.length === 0 ? (
          <div style={{ textAlign: 'center', padding: '48px 20px' }}>
            <KeyRound size={42} color="var(--text-muted)" style={{ margin: '0 auto 16px' }} />
            <h3 style={{ fontSize: '1.1rem', marginBottom: 6 }}>No AWS Accounts Linked</h3>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.88rem', marginBottom: 20 }}>
              Link your 12-digit AWS account and IAM keys to start ingesting inventory, metrics, and billing.
            </p>
            <button onClick={() => setShowModal(true)} className="btn btn-primary">
              <Plus size={16} /> Link Your First Account
            </button>
          </div>
        ) : (
          <div className="table-container">
            <table className="custom-table">
              <thead>
                <tr>
                  <th>Account ID</th>
                  <th>Account Name</th>
                  <th>AWS Account Number</th>
                  <th>Security Status</th>
                  <th>Associated User</th>
                </tr>
              </thead>
              <tbody>
                {accounts.map((acc) => (
                  <tr key={acc.id}>
                    <td style={{ fontFamily: 'var(--font-mono)', fontWeight: 600 }}>#{acc.id}</td>
                    <td style={{ fontWeight: 600 }}>{acc.accountName}</td>
                    <td style={{ fontFamily: 'var(--font-mono)', color: 'var(--primary)' }}>
                      {acc.awsAccountNumber}
                    </td>
                    <td>
                      <span className="pill pill-success">
                        <ShieldCheck size={13} /> AES-256 Encrypted
                      </span>
                    </td>
                    <td style={{ color: 'var(--text-secondary)' }}>{acc.userEmail}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Modal */}
      {showModal && (
        <div className="modal-backdrop">
          <div className="modal-card">
            <div className="modal-header">
              <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                <KeyRound size={20} color="var(--primary)" />
                <h3 style={{ fontSize: '1.1rem' }}>Link New AWS Account</h3>
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

            <form onSubmit={handleLink}>
              <div className="modal-body">
                <div className="form-group">
                  <label className="form-label">AWS 12-Digit Account Number</label>
                  <input
                    type="text"
                    required
                    pattern="[0-9]{12}"
                    placeholder="123456789012"
                    className="form-input"
                    value={awsAccountNumber}
                    onChange={(e) => setAwsAccountNumber(e.target.value)}
                  />
                </div>

                <div className="form-group">
                  <label className="form-label">Account Friendly Name</label>
                  <input
                    type="text"
                    required
                    placeholder="e.g. Production US-East"
                    className="form-input"
                    value={accountName}
                    onChange={(e) => setAccountName(e.target.value)}
                  />
                </div>

                <div className="form-group">
                  <label className="form-label">IAM Access Key ID</label>
                  <input
                    type="text"
                    required
                    placeholder="AKIAIOSFODNN7EXAMPLE"
                    className="form-input"
                    value={accessKey}
                    onChange={(e) => setAccessKey(e.target.value)}
                  />
                </div>

                <div className="form-group">
                  <label className="form-label">IAM Secret Access Key (Stored with AES-256)</label>
                  <input
                    type="password"
                    required
                    placeholder="wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY"
                    className="form-input"
                    value={secretKey}
                    onChange={(e) => setSecretKey(e.target.value)}
                  />
                </div>

                <div style={{ marginTop: 12 }}>
                  <button
                    type="button"
                    onClick={fillSampleAccount}
                    style={{ background: 'none', border: 'none', color: 'var(--primary)', cursor: 'pointer', fontSize: '0.8rem' }}
                  >
                    + Auto-fill Test AWS Account Data
                  </button>
                </div>
              </div>

              <div className="modal-footer">
                <button type="button" onClick={() => setShowModal(false)} className="btn btn-secondary">
                  Cancel
                </button>
                <button type="submit" disabled={submitting} className="btn btn-primary">
                  {submitting ? 'Encrypting & Linking...' : 'Link Account'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
