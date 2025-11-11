// import React from "react";
// import DashboardLayout from "./layouts/DashboardLayout";
// import { useAuth } from "./context/AuthContext";

// function App() {
//   const { user, login, logout } = useAuth();

//   return (
//     <DashboardLayout>
//       {user ? (
//         <div className="space-y-4">
//           <h1 className="text-3xl font-bold">Welcome, {user}</h1>
//           <button
//             onClick={logout}
//             className="px-4 py-2 bg-red-500 text-white rounded-lg"
//           >
//             Logout
//           </button>
//         </div>
//       ) : (
//         <div className="space-y-4">
//           <h1 className="text-3xl font-bold">Please log in</h1>
//           <button
//             onClick={() => login("Srishti")}
//             className="px-4 py-2 bg-blue-500 text-white rounded-lg"
//           >
//             Login as Srishti
//           </button>
//         </div>
//       )}
//     </DashboardLayout>
//   );
// }

// export default App;

import React from "react";
import { Routes, Route, Navigate } from "react-router-dom";
import { useAuth } from "./context/AuthContext";
import { NotificationProvider } from "./context/NotificationContext";
import DetectionsPage from "./pages/Detections";
import Dashboard from "./pages/Dashboard";
import Analytics from "./pages/Analytics";
import Settings from "./pages/Settings";
import Login from "./pages/Login";
import Signup from "./pages/Signup";

function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const { isAuthenticated } = useAuth();
  return isAuthenticated ? (
    <NotificationProvider>
      {children}
    </NotificationProvider>
  ) : <Navigate to="/login" />;
}

function AppRoutes() {
  const { isAuthenticated } = useAuth();
  
  return (
    <Routes>
      <Route path="/login" element={isAuthenticated ? <Navigate to="/" /> : <Login />} />
      <Route path="/signup" element={isAuthenticated ? <Navigate to="/" /> : <Signup />} />
      <Route path="/" element={
        <ProtectedRoute>
          <Dashboard />
        </ProtectedRoute>
      } />
      <Route path="/dashboard" element={
        <ProtectedRoute>
          <Dashboard />
        </ProtectedRoute>
      } />
      <Route path="/detections" element={
        <ProtectedRoute>
          <DetectionsPage />
        </ProtectedRoute>
      } />
      <Route path="/analytics" element={
        <ProtectedRoute>
          <Analytics />
        </ProtectedRoute>
      } />
      <Route path="/settings" element={
        <ProtectedRoute>
          <Settings />
        </ProtectedRoute>
      } />
    </Routes>
  );
}

export default function App() {
  return <AppRoutes />;
}
