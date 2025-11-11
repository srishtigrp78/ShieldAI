import React from "react";
import Sidebar from "./Sidebar";

function DashboardLayout({ children }) {
  return (
    <div className="flex">
      {/* Sidebar */}
      <Sidebar />

      {/* Main Content */}
      <div className="flex-1 bg-gray-100 min-h-screen p-6">
        {children}
      </div>
    </div>
  );
}

export default DashboardLayout;
