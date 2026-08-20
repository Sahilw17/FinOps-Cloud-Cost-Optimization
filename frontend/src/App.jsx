import React, { useState, useEffect, useCallback, useMemo } from 'react';
import { useAuth } from './context/AuthContext';
import { api } from './api/client';
import Sidebar from './components/Sidebar';
import Topbar from './components/Topbar';
import LoginModal from './components/LoginModal';
import AiCopilotDrawer from './components/AiCopilotDrawer';
import AiDeepAuditModal from './components/AiDeepAuditModal';

import OverviewView from './views/OverviewView';
import AdminView from './views/AdminView';
import AccountsView from './views/AccountsView';
import InventoryView from './views/InventoryView';
import CostsView from './views/CostsView';
import TelemetryView from './views/TelemetryView';
import RecommendationsView from './views/RecommendationsView';
import AlertsView from './views/AlertsView';

export default function App() {
  const { user, loading: authLoading } = useAuth();
  const [activeTab, setActiveTab] = useState('overview');

  // Global AWS Account Filter State
  const [selectedAccountId, setSelectedAccountId] = useState('');

  // AI Copilot & Deep Audit Modal States
  const [isAiCopilotOpen, setIsAiCopilotOpen] = useState(false);
  const [aiAuditResourceId, setAiAuditResourceId] = useState(null);

  // Application Data States
  const [accounts, setAccounts] = useState([]);
  const [resources, setResources] = useState([]);
  const [metrics, setMetrics] = useState([]);
  const [costs, setCosts] = useState([]);
  const [predictions, setPredictions] = useState([]);
  const [recommendations, setRecommendations] = useState([]);
  const [alerts, setAlerts] = useState([]);
  const [unreadAlertsCount, setUnreadAlertsCount] = useState(0);

  const [refreshing, setRefreshing] = useState(false);
  const [scanningAnomalies, setScanningAnomalies] = useState(false);
  const [analyzingRightsizing, setAnalyzingRightsizing] = useState(false);

  const rawRole = user?.role;
  const userRole = (typeof rawRole === 'string' ? rawRole : rawRole?.roleName) || 'USER';

  const loadAllData = useCallback(async () => {
    if (!user) return;
    setRefreshing(true);
    try {
      const [
        accs,
        resList,
        metricsList,
        costsList,
        predsList,
        recsList,
        alertsList,
        unreadData
      ] = await Promise.allSettled([
        api.getAccounts(),
        api.getResources(),
        api.getMetrics(),
        api.getCosts(),
        api.getPredictions(),
        api.getRecommendations(),
        api.getAlerts(),
        api.getUnreadAlertsCount()
      ]);

      if (accs.status === 'fulfilled') setAccounts(accs.value || []);
      if (resList.status === 'fulfilled') setResources(resList.value || []);
      if (metricsList.status === 'fulfilled') setMetrics(metricsList.value || []);
      if (costsList.status === 'fulfilled') setCosts(costsList.value || []);
      if (predsList.status === 'fulfilled') setPredictions(predsList.value || []);
      if (recsList.status === 'fulfilled') setRecommendations(recsList.value || []);
      if (alertsList.status === 'fulfilled') setAlerts(alertsList.value || []);
      if (unreadData.status === 'fulfilled') setUnreadAlertsCount(unreadData.value?.unreadCount || 0);
    } catch (err) {
      console.error('Error loading dashboard data', err);
    } finally {
      setRefreshing(false);
    }
  }, [user]);

  useEffect(() => {
    if (user) {
      loadAllData();
    }
  }, [user, loadAllData]);

  // Filtered views based on Global AWS Account Selector
  const targetAccId = selectedAccountId ? parseInt(selectedAccountId, 10) : null;

  const filteredResources = useMemo(() => {
    if (!targetAccId) return resources;
    return resources.filter(r => r.awsAccountId === targetAccId);
  }, [resources, targetAccId]);

  const filteredCosts = useMemo(() => {
    if (!targetAccId) return costs;
    return costs.filter(c => c.awsAccountId === targetAccId);
  }, [costs, targetAccId]);

  const filteredPredictions = useMemo(() => {
    if (!targetAccId) return predictions;
    return predictions.filter(p => p.awsAccountId === targetAccId);
  }, [predictions, targetAccId]);

  const filteredRecommendations = useMemo(() => {
    if (!targetAccId) return recommendations;
    return recommendations.filter(r => r.awsAccountId === targetAccId);
  }, [recommendations, targetAccId]);

  const filteredAlerts = useMemo(() => {
    if (!targetAccId) return alerts;
    return alerts.filter(a => a.awsAccountId === targetAccId);
  }, [alerts, targetAccId]);

  const filteredMetrics = useMemo(() => {
    if (!targetAccId) return metrics;
    return metrics.filter(m => {
      const matchedRes = resources.find(r => r.id === m.resourceId);
      return matchedRes && matchedRes.awsAccountId === targetAccId;
    });
  }, [metrics, resources, targetAccId]);

  // Quick Action: Run Real-time Anomaly Scan
  const handleQuickAnomalyScan = async () => {
    if (userRole === 'USER') {
      alert('Read-only view: Anomaly scans can only be triggered by FinOps Engineers or Admins.');
      return;
    }
    setScanningAnomalies(true);
    try {
      const generated = await api.detectAnomalies(targetAccId);
      await loadAllData();
      alert(`Anomaly Scan Complete! Identified ${generated?.length || 0} anomaly alert(s).`);
    } catch (err) {
      alert(err.message || 'Failed to execute anomaly scan');
    } finally {
      setScanningAnomalies(false);
    }
  };

  // Quick Action: Run Rightsizing Analysis
  const handleQuickRightsizing = async () => {
    if (userRole === 'USER') {
      alert('Read-only view: Rightsizing scans can only be triggered by FinOps Engineers or Admins.');
      return;
    }
    setAnalyzingRightsizing(true);
    try {
      const generated = await api.analyzeRecommendations(targetAccId);
      await loadAllData();
      alert(`Rightsizing Scan Complete! Generated ${generated?.length || 0} optimization recommendation(s).`);
    } catch (err) {
      alert(err.message || 'Failed to execute rightsizing analysis');
    } finally {
      setAnalyzingRightsizing(false);
    }
  };

  if (authLoading) {
    return (
      <div style={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', background: 'var(--bg-main)' }}>
        <div style={{ textAlign: 'center' }}>
          <div style={{ width: 40, height: 40, borderRadius: '50%', border: '3px solid var(--border-subtle)', borderTopColor: 'var(--primary)', animation: 'spin 1s linear infinite', margin: '0 auto 16px' }}></div>
          <p style={{ color: 'var(--text-secondary)' }}>Loading FinOps Workspace...</p>
        </div>
      </div>
    );
  }

  if (!user) {
    return <LoginModal />;
  }

  const getTabTitle = () => {
    switch (activeTab) {
      case 'overview': return { title: 'Executive Overview', subtitle: 'Global multi-cloud spend, forecast trajectory, and high-impact savings' };
      case 'admin': return { title: 'Admin Console & Users', subtitle: 'Manage organization users and assign multi-tenant security roles' };
      case 'accounts': return { title: 'AWS Cloud Accounts', subtitle: 'Manage encrypted IAM credentials and AWS account linkages' };
      case 'inventory': return { title: 'Cloud Resource Inventory', subtitle: 'Live status and metadata across EC2, RDS, and S3 assets' };
      case 'costs': return { title: 'Cost Analytics & AI Forecasts', subtitle: 'Explore historical billing records and run time-series ML prediction models' };
      case 'telemetry': return { title: 'Telemetry Performance Visualizer', subtitle: 'CloudWatch metrics, percentile CPU utilization, and I/O streams' };
      case 'recommendations': return { title: 'FinOps Rightsizing & Optimization Hub', subtitle: 'Actionable cost-saving recommendations with 1-click execution' };
      case 'alerts': return { title: 'Anomaly & Budget Alert Center', subtitle: 'Real-time detection of billing spikes and threshold breaches' };
      default: return { title: 'FinOps Intelligence Platform' };
    }
  };

  const { title, subtitle } = getTabTitle();

  return (
    <div className="app-container">
      <Sidebar
        activeTab={activeTab}
        setActiveTab={setActiveTab}
        unreadAlertsCount={unreadAlertsCount}
      />

      <div className="main-wrapper">
        <Topbar
          title={title}
          subtitle={subtitle}
          accounts={accounts}
          selectedAccountId={selectedAccountId}
          onSelectAccount={setSelectedAccountId}
          onRefresh={loadAllData}
          refreshing={refreshing}
          onQuickAnomalyScan={handleQuickAnomalyScan}
          scanningAnomalies={scanningAnomalies}
          onQuickRightsizing={handleQuickRightsizing}
          analyzingRightsizing={analyzingRightsizing}
          onOpenAiCopilot={() => setIsAiCopilotOpen(true)}
        />

        <main className="content-body">
          {activeTab === 'overview' && (
            <OverviewView
              accounts={accounts}
              selectedAccountId={selectedAccountId}
              onSelectAccount={setSelectedAccountId}
              costs={filteredCosts}
              allCosts={costs}
              predictions={filteredPredictions}
              allPredictions={predictions}
              recommendations={filteredRecommendations}
              allRecommendations={recommendations}
              alerts={filteredAlerts}
              resources={filteredResources}
              allResources={resources}
              onNavigate={setActiveTab}
            />
          )}

          {activeTab === 'admin' && userRole === 'ADMIN' && (
            <AdminView />
          )}

          {activeTab === 'accounts' && (
            <AccountsView
              accounts={accounts}
              onRefresh={loadAllData}
            />
          )}

          {activeTab === 'inventory' && (
            <InventoryView
              resources={filteredResources}
              accounts={accounts}
              onRefresh={loadAllData}
            />
          )}

          {activeTab === 'costs' && (
            <CostsView
              costs={filteredCosts}
              allCosts={costs}
              predictions={filteredPredictions}
              allPredictions={predictions}
              resources={filteredResources}
              accounts={accounts}
              selectedAccountId={selectedAccountId}
              onSelectAccount={setSelectedAccountId}
              onRefresh={loadAllData}
            />
          )}

          {activeTab === 'telemetry' && (
            <TelemetryView
              metrics={filteredMetrics}
              resources={filteredResources}
              onRefresh={loadAllData}
            />
          )}

          {activeTab === 'recommendations' && (
            <RecommendationsView
              recommendations={filteredRecommendations}
              resources={filteredResources}
              accounts={accounts}
              onRefresh={loadAllData}
              onAnalyze={handleQuickRightsizing}
              analyzing={analyzingRightsizing}
              onOpenAiAudit={(id) => setAiAuditResourceId(id)}
              onOpenAiCopilot={() => setIsAiCopilotOpen(true)}
            />
          )}

          {activeTab === 'alerts' && (
            <AlertsView
              alerts={filteredAlerts}
              onRefresh={loadAllData}
              onDetect={handleQuickAnomalyScan}
              detecting={scanningAnomalies}
            />
          )}
        </main>

        {/* Interactive FinOps AI Copilot Drawer */}
        <AiCopilotDrawer
          isOpen={isAiCopilotOpen}
          onClose={() => setIsAiCopilotOpen(false)}
          selectedAccountId={selectedAccountId}
        />

        {/* AI Deep Optimization Audit Modal */}
        <AiDeepAuditModal
          resourceId={aiAuditResourceId}
          onClose={() => setAiAuditResourceId(null)}
        />
      </div>
    </div>
  );
}
