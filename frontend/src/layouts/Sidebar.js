import React from "react";
import { NavLink } from "react-router-dom";

function Sidebar() {
  return (
    <div className="w-64 h-screen bg-gray-900 text-white flex flex-col p-4">
      <h2 className="text-2xl font-bold mb-6">ShieldAI</h2>
      <nav className="flex flex-col space-y-4">
        <NavLink
          to="/"
          className={({ isActive }) =>
            `p-2 rounded-lg ${isActive ? "bg-gray-700" : "hover:bg-gray-800"}`
          }
        >
          Dashboard
        </NavLink>
        <NavLink
          to="/detections"
          className={({ isActive }) =>
            `p-2 rounded-lg ${isActive ? "bg-gray-700" : "hover:bg-gray-800"}`
          }
        >
          Detections
        </NavLink>
        <NavLink
          to="/settings"
          className={({ isActive }) =>
            `p-2 rounded-lg ${isActive ? "bg-gray-700" : "hover:bg-gray-800"}`
          }
        >
          Settings
        </NavLink>
      </nav>
    </div>
  );
}

export default Sidebar;
