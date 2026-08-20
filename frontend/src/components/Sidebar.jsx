import React from 'react';
import { useAuth } from '../context/AuthContext';
import {
  LayoutDashboard,
  Shield,
  KeyRound,
  Layers,
  TrendingUp,
  Activity,
  Zap,
  Bell,
  LogOut,
  Cloud
} from 'lucide-react';

export default function Sidebar({ activeTab, setActiveTab, unreadAlertsCount }) {
  const { user, logout } = useAuth();
  const rawRole = user?.role;
  const userRole = (typeof rawRole === 'string' ? rawRole : rawRole?.roleName) || 'USER';

  const allNavItems = [
    { id: 'overview', label: 'Executive Overview', icon: LayoutDashboard, roles: ['ADMIN', 'FINOPS_ENGINEER', 'USER'] },
    { id: 'admin', label: 'Admin Console', icon: Shield, roles: ['ADMIN'] },
    { id: 'accounts', label: 'AWS Accounts', icon: KeyRound, roles: ['ADMIN', 'FINOPS_ENGINEER'] },
    { id: 'inventory', label: 'Cloud Inventory', icon: Layers, roles: ['ADMIN', 'FINOPS_ENGINEER', 'USER'] },
    { id: 'costs', label: 'Costs & Forecasts', icon: TrendingUp, roles: ['ADMIN', 'FINOPS_ENGINEER', 'USER'] },
    { id: 'telemetry', label: 'Telemetry Metrics', icon: Activity, roles: ['ADMIN', 'FINOPS_ENGINEER'] },
    { id: 'recommendations', label: 'Rightsizing Hub', icon: Zap, roles: ['ADMIN', 'FINOPS_ENGINEER', 'USER'] },
    { id: 'alerts', label: 'Anomaly & Alerts', icon: Bell, roles: ['ADMIN', 'FINOPS_ENGINEER', 'USER'], badge: unreadAlertsCount > 0 ? unreadAlertsCount : null },
  ];

  const visibleNavItems = allNavItems.filter(item => item.roles.includes(userRole));

  return (
    <aside className="sidebar">
      <div className="sidebar-brand">
        <div className="brand-icon">
          <Cloud size={22} />
        </div>
        <div>
          <div className="brand-title">FinOps AI</div>
          <div className="brand-subtitle">
            {userRole === 'ADMIN' && 'Enterprise Admin'}
            {userRole === 'FINOPS_ENGINEER' && 'Engineer Portal'}
            {userRole === 'USER' && 'Viewer Workspace'}
          </div>
        </div>
      </div>

      <nav className="sidebar-nav">
        {visibleNavItems.map((item) => {
          const Icon = item.icon;
          const isActive = activeTab === item.id;
          return (
            <div
              key={item.id}
              className={`nav-item ${isActive ? 'active' : ''}`}
              onClick={() => setActiveTab(item.id)}
            >
              <Icon size={18} />
              <span>{item.label}</span>
              {item.badge && <span className="nav-badge">{item.badge}</span>}
            </div>
          );
        })}
      </nav>

      <div className="sidebar-footer">
        <div className="user-profile-badge">
          <div className="user-avatar" style={{
            background: userRole === 'ADMIN' ? 'linear-gradient(135deg, #38BDF8, #6366F1)' : userRole === 'FINOPS_ENGINEER' ? '#10B981' : '#6B7280',
            color: '#fff'
          }}>
            {user?.name ? user.name.charAt(0).toUpperCase() : 'U'}
          </div>
          <div className="user-info">
            <div className="user-name">{user?.name || user?.email}</div>
            <div style={{ display: 'flex', alignItems: 'center', gap: 4, marginTop: 2 }}>
              <span className={`pill ${userRole === 'ADMIN' ? 'pill-primary' : userRole === 'FINOPS_ENGINEER' ? 'pill-success' : 'pill-info'}`} style={{ fontSize: '0.68rem', padding: '1px 6px' }}>
                {userRole}
              </span>
            </div>
          </div>
          <button
            onClick={logout}
            title="Log out"
            style={{
              background: 'none',
              border: 'none',
              color: 'var(--text-muted)',
              cursor: 'pointer',
              display: 'flex',
              alignItems: 'center',
              padding: 4
            }}
          >
            <LogOut size={16} />
          </button>
        </div>
      </div>
    </aside>
  );
}
