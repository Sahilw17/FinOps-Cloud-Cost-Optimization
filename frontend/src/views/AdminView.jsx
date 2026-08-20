import React, { useState, useEffect } from 'react';
import { api } from '../api/client';
import { Shield, Users, UserCheck, ShieldAlert, Check, RefreshCw } from 'lucide-react';

export default function AdminView() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [updatingUserId, setUpdatingUserId] = useState(null);
  const [error, setError] = useState(null);
  const [successMsg, setSuccessMsg] = useState(null);

  const fetchUsers = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await api.getUsers();
      setUsers(data || []);
    } catch (err) {
      setError(err.message || 'Failed to load users');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers();
  }, []);

  const handleRoleChange = async (userId, newRole) => {
    setUpdatingUserId(userId);
    setError(null);
    setSuccessMsg(null);
    try {
      await api.updateUserRole(userId, newRole);
      setSuccessMsg(`User #${userId} role successfully updated to ${newRole}`);
      await fetchUsers();
    } catch (err) {
      setError(err.message || 'Failed to update user role');
    } finally {
      setUpdatingUserId(null);
    }
  };

  return (
    <div>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 24 }}>
        <div>
          <h2 style={{ fontSize: '1.25rem', fontWeight: 700 }}>Admin Console & User Role Management</h2>
          <p style={{ fontSize: '0.84rem', color: 'var(--text-secondary)' }}>
            Manage platform users and assign multi-tenant security roles (ADMIN, FINOPS_ENGINEER, USER)
          </p>
        </div>
        <button onClick={fetchUsers} disabled={loading} className="btn btn-secondary btn-sm">
          <RefreshCw size={14} className={loading ? 'spin' : ''} /> Refresh Users
        </button>
      </div>

      {error && (
        <div style={{ padding: '12px 16px', borderRadius: 8, background: 'var(--danger-bg)', border: '1px solid rgba(239, 68, 68, 0.3)', color: 'var(--danger)', fontSize: '0.86rem', marginBottom: 20 }}>
          {error}
        </div>
      )}

      {successMsg && (
        <div style={{ padding: '12px 16px', borderRadius: 8, background: 'var(--success-bg)', border: '1px solid rgba(16, 185, 129, 0.3)', color: 'var(--success)', fontSize: '0.86rem', marginBottom: 20 }}>
          {successMsg}
        </div>
      )}

      {/* Role Privileges Cards */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: 20, marginBottom: 28 }}>
        <div className="card" style={{ borderLeft: '4px solid #38BDF8' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 8 }}>
            <Shield size={18} color="#38BDF8" />
            <h4 style={{ fontSize: '0.95rem', fontWeight: 700 }}>ADMIN Role</h4>
          </div>
          <p style={{ fontSize: '0.82rem', color: 'var(--text-secondary)' }}>
            Full system control. Access to user role delegations, AWS account linking & IAM credentials, rightsizing executions, and alert resolutions.
          </p>
        </div>

        <div className="card" style={{ borderLeft: '4px solid #10B981' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 8 }}>
            <Users size={18} color="#10B981" />
            <h4 style={{ fontSize: '0.95rem', fontWeight: 700 }}>FINOPS_ENGINEER Role</h4>
          </div>
          <p style={{ fontSize: '0.82rem', color: 'var(--text-secondary)' }}>
            Engineering specialist. Can explore inventory, run ML spend forecasts, trigger rightsizing analysis, and execute cost savings actions.
          </p>
        </div>

        <div className="card" style={{ borderLeft: '4px solid #6366F1' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 8 }}>
            <UserCheck size={18} color="#6366F1" />
            <h4 style={{ fontSize: '0.95rem', fontWeight: 700 }}>USER (Viewer) Role</h4>
          </div>
          <p style={{ fontSize: '0.82rem', color: 'var(--text-secondary)' }}>
            Read-only stakeholder. Can view Executive KPIs, cost graphs, and optimization reports without permission to delete or modify assets.
          </p>
        </div>
      </div>

      {/* Users Table */}
      <div className="card">
        <h3 style={{ fontSize: '1.05rem', fontWeight: 700, marginBottom: 16 }}>Registered Organization Users</h3>
        {loading ? (
          <p style={{ color: 'var(--text-muted)', padding: '20px 0' }}>Loading users...</p>
        ) : users.length === 0 ? (
          <p style={{ color: 'var(--text-muted)', padding: '20px 0' }}>No users found.</p>
        ) : (
          <div className="table-container">
            <table className="custom-table">
              <thead>
                <tr>
                  <th>User ID</th>
                  <th>Full Name</th>
                  <th>Email Address</th>
                  <th>Current Role</th>
                  <th>Change Role</th>
                </tr>
              </thead>
              <tbody>
                {users.map((u) => {
                  const roleName = typeof u.role === 'string' ? u.role : (u.role?.roleName || 'USER');
                  return (
                    <tr key={u.id}>
                      <td style={{ fontFamily: 'var(--font-mono)', fontWeight: 600 }}>#{u.id}</td>
                      <td style={{ fontWeight: 600 }}>{u.name}</td>
                      <td style={{ color: 'var(--text-secondary)' }}>{u.email}</td>
                      <td>
                        <span className={`pill ${roleName === 'ADMIN' ? 'pill-primary' : roleName === 'FINOPS_ENGINEER' ? 'pill-success' : 'pill-info'}`}>
                          {roleName}
                        </span>
                      </td>
                      <td>
                        <select
                          className="form-select"
                          style={{ padding: '4px 10px', fontSize: '0.82rem', width: 180 }}
                          disabled={updatingUserId === u.id}
                          value={roleName}
                          onChange={(e) => handleRoleChange(u.id, e.target.value)}
                        >
                          <option value="ADMIN">ADMIN</option>
                          <option value="FINOPS_ENGINEER">FINOPS_ENGINEER</option>
                          <option value="USER">USER (Viewer)</option>
                        </select>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
