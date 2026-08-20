const BASE_URL = ''; // Relative path, proxy in vite.config.js maps /api to http://localhost:8080

function getAuthHeaders() {
  const token = localStorage.getItem('finops_token');
  return {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {})
  };
}

async function request(endpoint, options = {}) {
  const url = `${BASE_URL}${endpoint}`;
  const response = await fetch(url, {
    ...options,
    headers: {
      ...getAuthHeaders(),
      ...(options.headers || {})
    }
  });

  if (response.status === 401) {
    // If unauthorized, clear token
    if (localStorage.getItem('finops_token') && !endpoint.includes('/api/auth/login')) {
      localStorage.removeItem('finops_token');
      window.location.reload();
    }
  }

  const text = await response.text();
  let data = null;
  try {
    data = text ? JSON.parse(text) : null;
  } catch (err) {
    data = text;
  }

  if (!response.ok) {
    const errorMsg = data?.message || (typeof data === 'string' ? data : `Request failed with status ${response.status}`);
    throw new Error(errorMsg);
  }

  return data;
}

export const api = {
  // Auth
  login: (email, password) => request('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify({ email, password })
  }),
  register: (name, email, password, roleName = 'ADMIN') => request('/api/auth/register', {
    method: 'POST',
    body: JSON.stringify({ name, email, password, roleName })
  }),
  getMe: () => request('/api/auth/me'),

  // Admin Management
  getUsers: () => request('/api/admin/users'),
  updateUserRole: (id, roleName) => request(`/api/admin/users/${id}/role`, {
    method: 'PUT',
    body: JSON.stringify({ roleName })
  }),

  // Accounts
  getAccounts: () => request('/api/accounts'),
  linkAccount: (awsAccountNumber, accountName, accessKey, secretKey) => request('/api/accounts', {
    method: 'POST',
    body: JSON.stringify({ awsAccountNumber, accountName, accessKey, secretKey })
  }),

  // Catalog
  getServices: () => request('/api/catalog/services'),
  getRegions: () => request('/api/catalog/regions'),

  // Resources
  getResources: (accountId, regionCode, serviceName) => {
    const params = new URLSearchParams();
    if (accountId) params.append('accountId', accountId);
    if (regionCode) params.append('regionCode', regionCode);
    if (serviceName) params.append('serviceName', serviceName);
    const qs = params.toString();
    return request(`/api/resources${qs ? '?' + qs : ''}`);
  },
  createResource: (payload) => request('/api/resources', {
    method: 'POST',
    body: JSON.stringify(payload)
  }),

  // Metrics
  getMetrics: (resourceId, metricName, startDate, endDate) => {
    const params = new URLSearchParams();
    if (resourceId) params.append('resourceId', resourceId);
    if (metricName) params.append('metricName', metricName);
    if (startDate) params.append('startDate', startDate);
    if (endDate) params.append('endDate', endDate);
    const qs = params.toString();
    return request(`/api/metrics${qs ? '?' + qs : ''}`);
  },
  ingestMetric: (payload) => request('/api/metrics', {
    method: 'POST',
    body: JSON.stringify(payload)
  }),

  // Costs
  getCosts: (accountId, resourceId, startDate, endDate) => {
    const params = new URLSearchParams();
    if (accountId) params.append('accountId', accountId);
    if (resourceId) params.append('resourceId', resourceId);
    if (startDate) params.append('startDate', startDate);
    if (endDate) params.append('endDate', endDate);
    const qs = params.toString();
    return request(`/api/costs${qs ? '?' + qs : ''}`);
  },
  ingestCost: (payload) => request('/api/costs', {
    method: 'POST',
    body: JSON.stringify(payload)
  }),

  // Predictions
  getPredictions: (accountId, resourceId, startDate, endDate) => {
    const params = new URLSearchParams();
    if (accountId) params.append('accountId', accountId);
    if (resourceId) params.append('resourceId', resourceId);
    if (startDate) params.append('startDate', startDate);
    if (endDate) params.append('endDate', endDate);
    const qs = params.toString();
    return request(`/api/predictions${qs ? '?' + qs : ''}`);
  },
  generatePrediction: (payload) => request('/api/predictions/generate', {
    method: 'POST',
    body: JSON.stringify(payload)
  }),

  // Recommendations
  getRecommendations: (status, accountId, resourceId) => {
    const params = new URLSearchParams();
    if (status) params.append('status', status);
    if (accountId) params.append('accountId', accountId);
    if (resourceId) params.append('resourceId', resourceId);
    const qs = params.toString();
    return request(`/api/recommendations${qs ? '?' + qs : ''}`);
  },
  analyzeRecommendations: (accountId) => {
    const qs = accountId ? `?accountId=${accountId}` : '';
    return request(`/api/recommendations/analyze${qs}`, { method: 'POST' });
  },
  updateRecommendationStatus: (id, status) => request(`/api/recommendations/${id}/status`, {
    method: 'PATCH',
    body: JSON.stringify({ status })
  }),

  // Alerts
  getAlerts: (status, accountId, resourceId) => {
    const params = new URLSearchParams();
    if (status) params.append('status', status);
    if (accountId) params.append('accountId', accountId);
    if (resourceId) params.append('resourceId', resourceId);
    const qs = params.toString();
    return request(`/api/alerts${qs ? '?' + qs : ''}`);
  },
  getUnreadAlertsCount: () => request('/api/alerts/unread-count'),
  detectAnomalies: (accountId) => {
    const qs = accountId ? `?accountId=${accountId}` : '';
    return request(`/api/alerts/detect${qs}`, { method: 'POST' });
  },
  updateAlertStatus: (id, status) => request(`/api/alerts/${id}/status`, {
    method: 'PATCH',
    body: JSON.stringify({ status })
  }),

  // AI FinOps Copilot & Deep Audit
  aiAnalyzeResource: (resourceId) => request(`/api/ai/analyze-resource/${resourceId}`, {
    method: 'POST'
  }),
  aiChat: (message, accountId, resourceId) => request('/api/ai/copilot/chat', {
    method: 'POST',
    body: JSON.stringify({ message, accountId, resourceId })
  })
};

