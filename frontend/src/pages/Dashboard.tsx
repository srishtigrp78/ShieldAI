import React, { useState, useEffect } from 'react';
import DashboardLayout from '../layouts/DashboardLayout';
import { fetchDetections } from '../api/detections';
import DetectionReport from '../types/DetectionReport';

interface DashboardStats {
  totalCandidates: number;
  totalDetections: number;
  highConfidenceDetections: number;
}

const Dashboard: React.FC = () => {
  const [stats, setStats] = useState<DashboardStats>({ totalCandidates: 0, totalDetections: 0, highConfidenceDetections: 0 });
  const [recentAlerts, setRecentAlerts] = useState<DetectionReport[]>([]);
  const [toolStats, setToolStats] = useState<{[key: string]: number}>({});
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadDashboardData();
  }, []);

  const loadDashboardData = async () => {
    try {
      const detections = await fetchDetections('');
      
      // Calculate stats
      const uniqueCandidates = new Set(detections.map(d => d.candidateId)).size;
      const highConfidence = detections.filter(d => d.confidence && d.confidence > 0.9).length;
      
      setStats({
        totalCandidates: uniqueCandidates,
        totalDetections: detections.length,
        highConfidenceDetections: highConfidence
      });

      // Recent alerts (last 10)
      const recent = detections.slice(0, 10);
      setRecentAlerts(recent);

      // Tool statistics
      const tools: {[key: string]: number} = {};
      detections.forEach(d => {
        tools[d.toolName] = (tools[d.toolName] || 0) + 1;
      });
      setToolStats(tools);
      
    } catch (error) {
      console.error('Failed to load dashboard data:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <DashboardLayout><div className="p-6">Loading dashboard...</div></DashboardLayout>;

  return (
    <DashboardLayout>
      <div className="space-y-6">
        <h1 className="text-3xl font-bold">Dashboard</h1>
        
        {/* Summary Metrics */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div className="bg-white p-6 rounded-lg shadow-md">
            <h3 className="text-lg font-semibold text-gray-700">Total Candidates</h3>
            <p className="text-3xl font-bold text-blue-600">{stats.totalCandidates}</p>
          </div>
          <div className="bg-white p-6 rounded-lg shadow-md">
            <h3 className="text-lg font-semibold text-gray-700">Total Detections</h3>
            <p className="text-3xl font-bold text-green-600">{stats.totalDetections}</p>
          </div>
          <div className="bg-white p-6 rounded-lg shadow-md">
            <h3 className="text-lg font-semibold text-gray-700">High Confidence</h3>
            <p className="text-3xl font-bold text-red-600">{stats.highConfidenceDetections}</p>
          </div>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {/* Recent Alerts */}
          <div className="bg-white rounded-lg shadow-md">
            <div className="px-6 py-4 border-b border-gray-200">
              <h3 className="text-lg font-semibold">Recent Alerts</h3>
            </div>
            <div className="px-6 py-4 max-h-96 overflow-y-auto">
              {recentAlerts.map((alert, index) => (
                <div key={index} className="py-2 border-b border-gray-100 last:border-b-0">
                  <div className="flex justify-between items-start">
                    <div>
                      <div className="font-medium">{alert.toolName}</div>
                      <div className="text-sm text-gray-600">{alert.candidateId}</div>
                    </div>
                    <div className="text-right">
                      <div className="text-sm font-medium">{alert.confidence ? (alert.confidence * 100).toFixed(0) : '0'}%</div>
                      <div className="text-xs text-gray-500">{alert.timestamp ? new Date(alert.timestamp).toLocaleDateString() : 'N/A'}</div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Most Common Tools */}
          <div className="bg-white rounded-lg shadow-md">
            <div className="px-6 py-4 border-b border-gray-200">
              <h3 className="text-lg font-semibold">Most Common AI Tools</h3>
            </div>
            <div className="px-6 py-4">
              {Object.entries(toolStats)
                .sort(([,a], [,b]) => b - a)
                .slice(0, 5)
                .map(([tool, count]) => (
                  <div key={tool} className="flex justify-between items-center py-2">
                    <span className="font-medium">{tool}</span>
                    <span className="bg-blue-100 text-blue-800 px-2 py-1 rounded-full text-sm">{count}</span>
                  </div>
                ))}
            </div>
          </div>
        </div>

        {/* Quick Actions */}
        <div className="bg-white rounded-lg shadow-md">
          <div className="px-6 py-4 border-b border-gray-200">
            <h3 className="text-lg font-semibold">Quick Actions</h3>
          </div>
          <div className="px-6 py-4">
            <div className="flex space-x-4">
              <a href="/detections" className="bg-blue-500 text-white px-4 py-2 rounded-lg hover:bg-blue-600">
                View All Detections
              </a>
              <button className="bg-green-500 text-white px-4 py-2 rounded-lg hover:bg-green-600">
                Export Report
              </button>
              <a href="/analytics" className="bg-purple-500 text-white px-4 py-2 rounded-lg hover:bg-purple-600">
                ML Analytics
              </a>
            </div>
          </div>
        </div>
      </div>
    </DashboardLayout>
  );
};

export default Dashboard;