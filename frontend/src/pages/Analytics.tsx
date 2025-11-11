import React, { useState, useEffect } from 'react';
import DashboardLayout from '../layouts/DashboardLayout';

interface Alert {
  candidateId: string;
  level: string;
  message: string;
  riskScore: number;
  timestamp: string;
}

interface RiskScore {
  [candidateId: string]: number;
}

const Analytics: React.FC = () => {
  const [alerts, setAlerts] = useState<Alert[]>([]);
  const [riskScores, setRiskScores] = useState<RiskScore>({});
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchAnalyticsData();
    const interval = setInterval(fetchAnalyticsData, 30000); // Refresh every 30 seconds
    return () => clearInterval(interval);
  }, []);

  const fetchAnalyticsData = async () => {
    try {
      const [alertsRes, riskRes] = await Promise.all([
        fetch('/api/analytics/alerts'),
        fetch('/api/analytics/risk-scores')
      ]);
      
      const alertsData = await alertsRes.json();
      const riskData = await riskRes.json();
      
      setAlerts(alertsData);
      setRiskScores(riskData.riskScores);
    } catch (error) {
      console.error('Failed to fetch analytics data:', error);
    } finally {
      setLoading(false);
    }
  };

  const getRiskColor = (score: number) => {
    if (score >= 0.8) return 'text-red-600 bg-red-50';
    if (score >= 0.6) return 'text-orange-600 bg-orange-50';
    if (score >= 0.4) return 'text-yellow-600 bg-yellow-50';
    return 'text-green-600 bg-green-50';
  };

  const getAlertColor = (level: string) => {
    switch (level) {
      case 'CRITICAL': return 'border-red-500 bg-red-50';
      case 'HIGH': return 'border-orange-500 bg-orange-50';
      case 'MEDIUM': return 'border-yellow-500 bg-yellow-50';
      default: return 'border-blue-500 bg-blue-50';
    }
  };

  if (loading) return <div className="p-6">Loading analytics...</div>;

  return (
    <DashboardLayout>
      <div className="space-y-6">
        <h1 className="text-3xl font-bold">ML Analytics Dashboard</h1>
      
      {/* Risk Scores */}
      <div className="bg-white rounded-lg shadow-md">
        <div className="px-6 py-4 border-b border-gray-200">
          <h3 className="text-lg font-semibold text-gray-900">Risk Scores</h3>
        </div>
        <div className="px-6 py-4">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            {Object.entries(riskScores).map(([candidateId, score]) => (
              <div key={candidateId} className={`p-4 rounded-lg ${getRiskColor(score)}`}>
                <div className="font-semibold">{candidateId}</div>
                <div className="text-2xl font-bold">{(score * 100).toFixed(0)}%</div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Active Alerts */}
      <div className="bg-white rounded-lg shadow-md">
        <div className="px-6 py-4 border-b border-gray-200">
          <h3 className="text-lg font-semibold text-gray-900">Active Alerts</h3>
        </div>
        <div className="px-6 py-4">
          <div className="space-y-3">
            {alerts.map((alert, index) => (
              <div key={index} className={`p-4 border-l-4 rounded ${getAlertColor(alert.level)}`}>
                <div className="flex justify-between items-start">
                  <div>
                    <div className="font-semibold">{alert.candidateId}</div>
                    <div className="text-sm text-gray-600">{alert.message}</div>
                  </div>
                  <div className="text-right">
                    <div className="font-bold text-lg">{alert.level}</div>
                    <div className="text-sm">Risk: {(alert.riskScore * 100).toFixed(0)}%</div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
      </div>
    </DashboardLayout>
  );
};

export default Analytics;