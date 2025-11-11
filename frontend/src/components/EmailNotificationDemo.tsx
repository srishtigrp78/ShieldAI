import React from 'react';

interface Props {
  userEmail: string;
  enabled: boolean;
}

const EmailNotificationDemo: React.FC<Props> = ({ userEmail, enabled }) => {
  const sendTestEmail = async () => {
    if (!enabled) {
      alert('Email notifications are disabled. Enable them in settings first.');
      return;
    }
    
    try {
      const response = await fetch('/api/settings/test-email', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: userEmail })
      });
      
      const data = await response.json();
      
      if (response.ok) {
        alert(`✅ ${data.message || 'Test email sent successfully!'}`);
      } else {
        alert(`❌ ${data.error || 'Failed to send test email'}`);
      }
    } catch (error) {
      alert('❌ Network error: ' + error);
    }
  };

  return (
    <div className="mt-4 p-3 bg-blue-50 rounded border">
      <div className="text-sm text-blue-700 mb-2">
        Test Email Notifications
      </div>
      <button
        onClick={sendTestEmail}
        disabled={!enabled}
        className="text-sm bg-blue-500 text-white px-3 py-1 rounded hover:bg-blue-600 disabled:opacity-50"
      >
        Send Test Email to {userEmail}
      </button>
    </div>
  );
};

export default EmailNotificationDemo;