import React, { useState, useEffect } from 'react';
import DashboardLayout from '../layouts/DashboardLayout';
import EmailNotificationDemo from '../components/EmailNotificationDemo';

interface Settings {
  confidenceThreshold: number;
  monitoredTools: string[];
  dashboardAlerts: boolean;
  emailAlerts: boolean;
}

interface UserProfile {
  name: string;
  email: string;
}

const Settings: React.FC = () => {
  const [settings, setSettings] = useState<Settings>({
    confidenceThreshold: 0.8,
    monitoredTools: ['ChatGPT', 'Claude', 'Copilot', 'Gemini'],
    dashboardAlerts: true,
    emailAlerts: false
  });
  const [profile, setProfile] = useState<UserProfile>({ name: '', email: '' });
  const [loading, setLoading] = useState(false);

  const availableTools = ['ChatGPT', 'Claude', 'GitHub Copilot', 'Gemini', 'Perplexity', 'Cursor', 'Tabnine'];

  useEffect(() => {
    loadSettings();
  }, []);

  const loadSettings = async () => {
    try {
      const response = await fetch('/api/settings');
      if (response.ok) {
        const data = await response.json();
        setSettings(data.settings);
        setProfile(data.profile);
      }
    } catch (error) {
      console.error('Failed to load settings:', error);
    }
  };

  const saveSettings = async () => {
    setLoading(true);
    try {
      const response = await fetch('/api/settings', {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ settings, profile })
      });
      if (response.ok) {
        alert('Settings saved successfully!');
        // Update sidebar display
        const nameEl = document.getElementById('user-profile');
        const emailEl = document.getElementById('user-email');
        if (nameEl) nameEl.textContent = profile.name || 'HR Admin';
        if (emailEl) emailEl.textContent = profile.email || 'hr@company.com';
      }
    } catch (error) {
      alert('Failed to save settings');
    } finally {
      setLoading(false);
    }
  };

  const toggleTool = (tool: string) => {
    setSettings(prev => ({
      ...prev,
      monitoredTools: prev.monitoredTools.includes(tool)
        ? prev.monitoredTools.filter(t => t !== tool)
        : [...prev.monitoredTools, tool]
    }));
  };

  return (
    <DashboardLayout>
      <div className="space-y-6">
        <h1 className="text-3xl font-bold">Settings</h1>

        {/* Account Management */}
        <div className="bg-white rounded-lg shadow-md">
          <div className="px-6 py-4 border-b border-gray-200">
            <h3 className="text-lg font-semibold">Account Management</h3>
          </div>
          <div className="px-6 py-4 space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700">Name</label>
              <input
                type="text"
                value={profile.name}
                onChange={(e) => setProfile(prev => ({ ...prev, name: e.target.value }))}
                className="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Email</label>
              <input
                type="email"
                value={profile.email}
                onChange={(e) => setProfile(prev => ({ ...prev, email: e.target.value }))}
                className="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2"
              />
            </div>
          </div>
        </div>

        {/* Detection Sensitivity */}
        <div className="bg-white rounded-lg shadow-md">
          <div className="px-6 py-4 border-b border-gray-200">
            <h3 className="text-lg font-semibold">Detection Sensitivity</h3>
          </div>
          <div className="px-6 py-4 space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700">
                Confidence Threshold: {(settings.confidenceThreshold * 100).toFixed(0)}%
              </label>
              <input
                type="range"
                min="0.5"
                max="1"
                step="0.1"
                value={settings.confidenceThreshold}
                onChange={(e) => setSettings(prev => ({ ...prev, confidenceThreshold: parseFloat(e.target.value) }))}
                className="mt-1 block w-full"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">Monitored AI Tools</label>
              <div className="grid grid-cols-2 md:grid-cols-3 gap-2">
                {availableTools.map(tool => (
                  <label key={tool} className="flex items-center">
                    <input
                      type="checkbox"
                      checked={settings.monitoredTools.includes(tool)}
                      onChange={() => toggleTool(tool)}
                      className="mr-2"
                    />
                    {tool}
                  </label>
                ))}
              </div>
            </div>
          </div>
        </div>

        {/* Notification Settings */}
        <div className="bg-white rounded-lg shadow-md">
          <div className="px-6 py-4 border-b border-gray-200">
            <h3 className="text-lg font-semibold">Notification Settings</h3>
          </div>
          <div className="px-6 py-4 space-y-4">
            <label className="flex items-center">
              <input
                type="checkbox"
                checked={settings.dashboardAlerts}
                onChange={(e) => setSettings(prev => ({ ...prev, dashboardAlerts: e.target.checked }))}
                className="mr-2"
              />
              Enable dashboard alerts
            </label>
            <label className="flex items-center">
              <input
                type="checkbox"
                checked={settings.emailAlerts}
                onChange={(e) => setSettings(prev => ({ ...prev, emailAlerts: e.target.checked }))}
                className="mr-2"
              />
              Enable email alerts
            </label>
            <EmailNotificationDemo userEmail={profile.email} enabled={settings.emailAlerts} />
          </div>
        </div>

        {/* Save Button */}
        <div className="flex justify-end">
          <button
            onClick={saveSettings}
            disabled={loading}
            className="bg-blue-500 text-white px-6 py-2 rounded-lg hover:bg-blue-600 disabled:opacity-50"
          >
            {loading ? 'Saving...' : 'Save Settings'}
          </button>
        </div>
      </div>
    </DashboardLayout>
  );
};

export default Settings;