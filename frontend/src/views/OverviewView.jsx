import React, { useMemo } from 'react';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  ArcElement,
  Title,
  Tooltip,
  Legend,
  Filler
} from 'chart.js';
import { Line, Doughnut, Bar } from 'react-chartjs-2';
import { DollarSign, TrendingUp, Zap, Layers, AlertCircle, ArrowUpRight, Cloud, ChevronRight, X } from 'lucide-react';

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  ArcElement,
  Title,
  Tooltip,
  Legend,
  Filler
);

export default function OverviewView({
  accounts = [],
  selectedAccountId = '',
  onSelectAccount,
  costs = [],
  allCosts = [],
  predictions = [],
  allPredictions = [],
  recommendations = [],
  allRecommendations = [],
  alerts = [],
  resources = [],
  allResources = [],
  onNavigate
}) {
  const activeAccount = accounts.find(a => a.id.toString() === selectedAccountId);

  // Compute KPI values
  const totalCost = costs.reduce((sum, c) => sum + (parseFloat(c.costAmount) || 0), 0);
  
  const latestPrediction = predictions.length > 0
    ? predictions[predictions.length - 1]
    : null;
  const projectedSpend = latestPrediction ? parseFloat(latestPrediction.predictedCost) : 0;

  const totalPotentialSavings = recommendations
    .filter(r => r.status === 'NEW' || r.status === 'VIEWED')
    .reduce((sum, r) => sum + (parseFloat(r.estimatedMonthlySaving) || 0), 0);

  const unreadAlerts = alerts.filter(a => a.status === 'UNREAD').length;

  // Prepare Spend Timeline Chart
  const costLabels = costs.map(c => c.usageDate || 'Date');
  const costData = costs.map(c => parseFloat(c.costAmount) || 0);

  if (latestPrediction && latestPrediction.predictionDate) {
    costLabels.push(`Forecast (${latestPrediction.predictionDate})`);
    costData.push(projectedSpend);
  }

  const spendChartData = {
    labels: costLabels.length > 0 ? costLabels : ['Day 1', 'Day 2', 'Day 3', 'Forecast'],
    datasets: [
      {
        label: 'Daily / Projected Spend ($)',
        data: costData.length > 0 ? costData : [25, 45, 60, 180],
        borderColor: '#38BDF8',
        backgroundColor: 'rgba(56, 189, 248, 0.15)',
        fill: true,
        tension: 0.35,
        pointBackgroundColor: '#38BDF8',
        pointHoverRadius: 6,
      }
    ]
  };

  const chartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: false },
      tooltip: {
        backgroundColor: '#1F2937',
        titleColor: '#F9FAFB',
        bodyColor: '#9CA3AF',
        borderColor: 'rgba(255,255,255,0.1)',
        borderWidth: 1,
        padding: 12,
      }
    },
    scales: {
      x: {
        grid: { color: 'rgba(255,255,255,0.05)' },
        ticks: { color: '#9CA3AF', font: { size: 11 } }
      },
      y: {
        grid: { color: 'rgba(255,255,255,0.05)' },
        ticks: { color: '#9CA3AF', font: { size: 11 } }
      }
    }
  };

  // Service distribution
  const serviceDistribution = {
    labels: ['EC2 (Compute)', 'RDS (Databases)', 'S3 (Storage)'],
    datasets: [
      {
        data: [65, 25, 10],
        backgroundColor: ['#38BDF8', '#6366F1', '#10B981'],
        borderWidth: 0,
        hoverOffset: 6
      }
    ]
  };

  // Account-Wise Data Aggregation
  const accountStats = useMemo(() => {
    const dataSourceCosts = allCosts.length > 0 ? allCosts : costs;
    const dataSourceRecs = allRecommendations.length > 0 ? allRecommendations : recommendations;
    const dataSourceRes = allResources.length > 0 ? allResources : resources;
    const dataSourcePred = allPredictions.length > 0 ? allPredictions : predictions;

    return accounts.map(acc => {
      const accCosts = dataSourceCosts.filter(c => c.awsAccountId === acc.id);
      const accSpend = accCosts.reduce((s, c) => s + (parseFloat(c.costAmount) || 0), 0);

      const accRes = dataSourceRes.filter(r => r.awsAccountId === acc.id);
      const resIds = new Set(accRes.map(r => r.id));

      const accRecs = dataSourceRecs.filter(r => 
        r.awsAccountId === acc.id || (r.resourceId && resIds.has(r.resourceId))
      );
      const accSavings = accRecs
        .filter(r => r.status === 'NEW' || r.status === 'VIEWED')
        .reduce((s, r) => s + (parseFloat(r.estimatedMonthlySaving) || 0), 0);

      const accPredictions = dataSourcePred.filter(p =>
        p.awsAccountId === acc.id || (p.resourceId && resIds.has(p.resourceId))
      );
      const latestAccPred = accPredictions.length > 0 ? accPredictions[accPredictions.length - 1] : null;

      return {
        ...acc,
        spend: accSpend,
        savings: accSavings,
        resourceCount: accRes.length,
        forecast: latestAccPred ? parseFloat(latestAccPred.predictedCost) : 0,
        confidence: latestAccPred ? (parseFloat(latestAccPred.confidenceScore) * 100).toFixed(0) : 'N/A'
      };
    });
  }, [accounts, allCosts, costs, allRecommendations, recommendations, allResources, resources, allPredictions, predictions]);

  // Account Comparison Chart Data
  const accountBarData = {
    labels: accountStats.map(a => a.accountName || a.awsAccountNumber),
    datasets: [
      {
        label: 'Recorded Spend ($)',
        data: accountStats.map(a => a.spend),
        backgroundColor: '#38BDF8',
        borderRadius: 4
      },
      {
        label: 'Potential Monthly Savings ($)',
        data: accountStats.map(a => a.savings),
        backgroundColor: '#10B981',
        borderRadius: 4
      }
    ]
  };

  return (
    <div>
      {/* Active Account Filter Banner */}
      {selectedAccountId && (
        <div style={{
          marginBottom: 20,
          padding: '12px 18px',
          borderRadius: 8,
          background: 'rgba(56, 189, 248, 0.1)',
          border: '1px solid rgba(56, 189, 248, 0.3)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between'
        }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
            <Cloud size={18} color="var(--primary)" />
            <span style={{ fontSize: '0.9rem' }}>
              Showing Account-Wise Data for: <strong>{activeAccount?.accountName}</strong> (AWS #{activeAccount?.awsAccountNumber})
            </span>
          </div>
          <button
            onClick={() => onSelectAccount('')}
            className="btn btn-secondary btn-sm"
            style={{ display: 'flex', alignItems: 'center', gap: 4 }}
          >
            <X size={14} /> Clear Filter (View All Accounts)
          </button>
        </div>
      )}

      {/* KPI Cards Grid */}
      <div className="kpi-grid">
        <div className="card kpi-card">
          <div className="kpi-label">
            <span>Historical Recorded Spend</span>
            <DollarSign size={16} color="var(--primary)" />
          </div>
          <div className="kpi-value">${totalCost.toFixed(2)}</div>
          <div className="kpi-footer">
            <span style={{ color: 'var(--text-secondary)' }}>Across {costs.length} billing records</span>
          </div>
        </div>

        <div className="card kpi-card">
          <div className="kpi-label">
            <span>30-Day ML Projected Spend</span>
            <TrendingUp size={16} color="var(--primary)" />
          </div>
          <div className="kpi-value">${projectedSpend.toFixed(2)}</div>
          <div className="kpi-footer">
            <span style={{ color: 'var(--primary)' }}>
              Confidence: {latestPrediction ? `${(parseFloat(latestPrediction.confidenceScore) * 100).toFixed(0)}%` : '65%'}
            </span>
          </div>
        </div>

        <div className="card kpi-card savings">
          <div className="kpi-label">
            <span>Identified Monthly Savings</span>
            <Zap size={16} color="var(--success)" />
          </div>
          <div className="kpi-value" style={{ color: 'var(--success)' }}>
            ${totalPotentialSavings.toFixed(2)}
          </div>
          <div className="kpi-footer">
            <span style={{ color: 'var(--success)' }}>
              {recommendations.filter(r => r.status === 'NEW').length} actionable rightsizing opportunities
            </span>
          </div>
        </div>

        <div className="card kpi-card">
          <div className="kpi-label">
            <span>Active Cloud Assets</span>
            <Layers size={16} color="var(--info)" />
          </div>
          <div className="kpi-value">{resources.length}</div>
          <div className="kpi-footer">
            <span>Tracked EC2, RDS, & S3 instances</span>
          </div>
        </div>

        <div className="card kpi-card alerts">
          <div className="kpi-label">
            <span>Active Anomaly Alerts</span>
            <AlertCircle size={16} color="var(--danger)" />
          </div>
          <div className="kpi-value" style={{ color: unreadAlerts > 0 ? 'var(--danger)' : 'var(--text-primary)' }}>
            {unreadAlerts}
          </div>
          <div className="kpi-footer">
            <span>{unreadAlerts > 0 ? 'Requires attention' : 'All systems normal'}</span>
          </div>
        </div>
      </div>

      {/* Account-Wise Spend & Efficiency Portfolio Matrix */}
      <div className="card" style={{ marginBottom: 24 }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 16 }}>
          <div>
            <h3 style={{ fontSize: '1.1rem', fontWeight: 700 }}>AWS Account Portfolio & Spend Breakdown</h3>
            <p style={{ fontSize: '0.82rem', color: 'var(--text-secondary)' }}>
              Side-by-side cost, forecast trajectory, and potential savings comparison per linked AWS account
            </p>
          </div>
          <span className="pill pill-info">{accounts.length} Linked AWS Accounts</span>
        </div>

        {accountStats.length === 0 ? (
          <p style={{ color: 'var(--text-muted)', fontSize: '0.88rem', padding: '16px 0' }}>
            No AWS accounts linked yet.
          </p>
        ) : (
          <div style={{ overflowX: 'auto' }}>
            <table className="table" style={{ width: '100%' }}>
              <thead>
                <tr>
                  <th>AWS Account</th>
                  <th>AWS Account #</th>
                  <th>Recorded Spend</th>
                  <th>30-Day Forecast</th>
                  <th>Potential Savings</th>
                  <th>Active Assets</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {accountStats.map(acc => {
                  const isSelected = selectedAccountId === acc.id.toString();
                  return (
                    <tr
                      key={acc.id}
                      style={{
                        background: isSelected ? 'rgba(56, 189, 248, 0.08)' : 'transparent',
                        fontWeight: isSelected ? 600 : 'normal'
                      }}
                    >
                      <td>
                        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                          <Cloud size={16} color={isSelected ? 'var(--primary)' : 'var(--text-muted)'} />
                          <strong>{acc.accountName}</strong>
                          {isSelected && <span className="pill pill-primary" style={{ fontSize: '0.7rem' }}>Active</span>}
                        </div>
                      </td>
                      <td style={{ fontFamily: 'var(--font-mono)', fontSize: '0.84rem' }}>{acc.awsAccountNumber}</td>
                      <td style={{ fontFamily: 'var(--font-mono)', color: 'var(--text-primary)', fontWeight: 700 }}>
                        ${acc.spend.toFixed(2)}
                      </td>
                      <td style={{ fontFamily: 'var(--font-mono)', color: 'var(--primary)' }}>
                        {acc.forecast > 0 ? `$${acc.forecast.toFixed(2)}` : '—'}
                      </td>
                      <td>
                        <span className="pill pill-success" style={{ fontFamily: 'var(--font-mono)' }}>
                          +${acc.savings.toFixed(2)}/mo
                        </span>
                      </td>
                      <td>
                        <span className="pill pill-info">{acc.resourceCount} Assets</span>
                      </td>
                      <td>
                        {isSelected ? (
                          <button
                            onClick={() => onSelectAccount('')}
                            className="btn btn-secondary btn-sm"
                            title="Show all accounts"
                          >
                            Show All
                          </button>
                        ) : (
                          <button
                            onClick={() => onSelectAccount(acc.id.toString())}
                            className="btn btn-primary btn-sm"
                            title="Filter dashboard to this account"
                            style={{ display: 'flex', alignItems: 'center', gap: 4 }}
                          >
                            Filter Account <ChevronRight size={14} />
                          </button>
                        )}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Charts Section */}
      <div className="charts-grid" style={{ marginBottom: 24 }}>
        <div className="card">
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 20 }}>
            <div>
              <h3 style={{ fontSize: '1.05rem', fontWeight: 700 }}>Spend Trajectory & ML Forecast</h3>
              <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Daily historical billing vs future forecast projection</p>
            </div>
            <button
              onClick={() => onNavigate('costs')}
              className="btn btn-secondary btn-sm"
            >
              Cost Explorer <ArrowUpRight size={14} />
            </button>
          </div>
          <div style={{ height: 280 }}>
            <Line data={spendChartData} options={chartOptions} />
          </div>
        </div>

        <div className="card">
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 20 }}>
            <div>
              <h3 style={{ fontSize: '1.05rem', fontWeight: 700 }}>Account Cost Comparison</h3>
              <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Recorded Spend vs Potential Savings per Account</p>
            </div>
          </div>
          <div style={{ height: 280 }}>
            <Bar
              data={accountBarData}
              options={{
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                  legend: {
                    position: 'bottom',
                    labels: { color: '#9CA3AF', font: { size: 11 } }
                  }
                },
                scales: {
                  x: {
                    grid: { color: 'rgba(255,255,255,0.05)' },
                    ticks: { color: '#9CA3AF', font: { size: 11 } }
                  },
                  y: {
                    grid: { color: 'rgba(255,255,255,0.05)' },
                    ticks: { color: '#9CA3AF', font: { size: 11 } }
                  }
                }
              }}
            />
          </div>
        </div>
      </div>

      {/* Lower Dual Grid: Top Recommendations & Recent Alerts */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 24 }}>
        {/* Recommendations Quick Panel */}
        <div className="card">
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 16 }}>
            <h3 style={{ fontSize: '1.05rem', fontWeight: 700 }}>Top Rightsizing Recommendations</h3>
            <button onClick={() => onNavigate('recommendations')} className="btn btn-secondary btn-sm">
              View All <ArrowUpRight size={14} />
            </button>
          </div>

          {recommendations.length === 0 ? (
            <p style={{ color: 'var(--text-muted)', fontSize: '0.88rem', padding: '16px 0' }}>
              No recommendations yet. Run an automated rightsizing scan from the top bar!
            </p>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
              {recommendations.slice(0, 3).map((rec) => (
                <div
                  key={rec.id}
                  style={{
                    padding: '12px 14px',
                    borderRadius: 8,
                    background: 'rgba(255,255,255,0.03)',
                    border: '1px solid var(--border-subtle)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between'
                  }}
                >
                  <div>
                    <div style={{ fontWeight: 600, fontSize: '0.88rem' }}>{rec.title}</div>
                    <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)' }}>
                      Target: {rec.resourceName || (rec.awsAccountNumber ? `Account ${rec.awsAccountNumber}` : 'Account-level')}
                    </div>
                  </div>
                  <div style={{ textAlign: 'right' }}>
                    <span className="pill pill-success">+${parseFloat(rec.estimatedMonthlySaving).toFixed(2)}/mo</span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Recent Alerts Quick Panel */}
        <div className="card">
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 16 }}>
            <h3 style={{ fontSize: '1.05rem', fontWeight: 700 }}>Real-time Anomaly Alerts</h3>
            <button onClick={() => onNavigate('alerts')} className="btn btn-secondary btn-sm">
              View All <ArrowUpRight size={14} />
            </button>
          </div>

          {alerts.length === 0 ? (
            <p style={{ color: 'var(--text-muted)', fontSize: '0.88rem', padding: '16px 0' }}>
              No anomalies detected. Cloud infrastructure is healthy.
            </p>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
              {alerts.slice(0, 3).map((alert) => (
                <div
                  key={alert.id}
                  style={{
                    padding: '12px 14px',
                    borderRadius: 8,
                    background: alert.status === 'UNREAD' ? 'var(--danger-bg)' : 'rgba(255,255,255,0.03)',
                    border: `1px solid ${alert.status === 'UNREAD' ? 'rgba(239, 68, 68, 0.3)' : 'var(--border-subtle)'}`,
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between'
                  }}
                >
                  <div>
                    <div style={{ fontWeight: 600, fontSize: '0.88rem' }}>{alert.title}</div>
                    <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)' }}>{alert.message}</div>
                  </div>
                  <span className={`pill ${alert.status === 'UNREAD' ? 'pill-danger' : 'pill-info'}`}>
                    {alert.status}
                  </span>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
