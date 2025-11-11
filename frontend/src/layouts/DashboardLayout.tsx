import React from "react";

type Props = {
  children: React.ReactNode;
};

const DashboardLayout = ({ children }: Props) => {
  return (
    <div className="flex h-screen bg-gray-100">
      {/* Sidebar */}
      <aside className="w-64 bg-gray-900 text-white flex flex-col">
        <div className="p-4 text-xl font-bold border-b border-gray-700">
          ShieldAI
        </div>
        <nav className="flex-1 p-4 space-y-2">
          <a href="/dashboard" className="block px-2 py-1 rounded hover:bg-gray-700">
            Dashboard
          </a>
          <a href="/detections" className="block px-2 py-1 rounded hover:bg-gray-700">
            Detections
          </a>
          <a href="/analytics" className="block px-2 py-1 rounded hover:bg-gray-700">
            ML Analytics
          </a>
          <a href="/settings" className="block px-2 py-1 rounded hover:bg-gray-700">
            Settings
          </a>
        </nav>
        <div className="p-4 border-t border-gray-700 text-sm">
          <div className="text-gray-300">Logged in as:</div>
          <div className="font-medium" id="user-profile">HR Admin</div>
          <div className="text-gray-400 text-xs" id="user-email">hr@company.com</div>
        </div>
      </aside>

      {/* Main content */}
      <main className="flex-1 p-6 overflow-y-auto">{children}</main>
    </div>
  );
};

export default DashboardLayout;
