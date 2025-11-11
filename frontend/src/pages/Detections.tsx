import React, { useEffect, useState, useCallback, useMemo } from "react";
import { fetchDetections } from "../api/detections";
import DetectionsTable from "../components/DetectionsTable";
import DetectionReport from "../types/DetectionReport";
import DashboardLayout from "../layouts/DashboardLayout";
import { useAuth } from "../context/AuthContext";
import NotificationPanel from "../components/NotificationPanel";
import ExportButtons from "../components/ExportButtons";

interface Filters {
  candidateId: string;
  startDate: string;
  endDate: string;
  toolName: string;
  search: string;
}

export default function DetectionsPage() {
  const [data, setData] = useState<DetectionReport[] | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [filters, setFilters] = useState<Filters>({
    candidateId: '',
    startDate: '',
    endDate: '',
    toolName: '',
    search: ''
  });
  const [toolNames, setToolNames] = useState<string[]>([]);
  const [isFiltering, setIsFiltering] = useState(false);
  const { logout } = useAuth();

  function LogoutButton() {
    return (
      <button onClick={logout} className="px-3 py-1 bg-red-500 text-white rounded">
        Logout
      </button>
    );
  }

  const load = useCallback(async (showLoading = true) => {
    setError(null);
    if (showLoading) setLoading(true);
    setIsFiltering(true);
    
    try {
      const params = new URLSearchParams();
      if (filters.candidateId.trim()) params.append('candidateId', filters.candidateId.trim());
      if (filters.startDate) params.append('startDate', filters.startDate);
      if (filters.endDate) params.append('endDate', filters.endDate);
      if (filters.toolName) params.append('toolName', filters.toolName);
      if (filters.search.trim()) params.append('search', filters.search.trim());
      
      const rows = await fetchDetections(params.toString());
      
      // Optimize sorting
      const sortedRows = rows.sort((a, b) => {
        const ta = new Date(a.detectedAt || a.timestamp || 0).getTime();
        const tb = new Date(b.detectedAt || b.timestamp || 0).getTime();
        return tb - ta;
      });
      
      setData(sortedRows);
    } catch (err: any) {
      setError(err?.message || "Failed to load detections");
      setData([]);
    } finally {
      if (showLoading) setLoading(false);
      setIsFiltering(false);
    }
  }, [filters]);

  useEffect(() => { 
    load();
    loadToolNames();
  }, []);
  
  // Debounced filter effect
  useEffect(() => {
    const timeoutId = setTimeout(() => {
      if (filters.search || filters.candidateId) {
        load(false);
      }
    }, 500);
    
    return () => clearTimeout(timeoutId);
  }, [filters.search, filters.candidateId]);

  const loadToolNames = useCallback(async () => {
    try {
      const token = localStorage.getItem('token');
      const headers: Record<string, string> = {};
      if (token) {
        headers['Authorization'] = `Bearer ${token}`;
      }
      
      const response = await fetch('/api/detections/tools', {
        headers
      });
      if (response.ok) {
        const names = await response.json();
        setToolNames(Array.isArray(names) ? names : []);
      }
    } catch (err) {
      console.error('Failed to load tool names:', err);
      setToolNames(['ChatGPT', 'Claude', 'Copilot', 'Gemini']); // Fallback
    }
  }, []);

  return (
    <DashboardLayout>
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <h2 className="text-2xl font-semibold">View all detections</h2>
          <div className="flex items-center space-x-2">
            <ExportButtons filters={filters} />
            <NotificationPanel />
            <button onClick={() => load(true)} className="px-3 py-1 bg-gray-200 rounded">Refresh</button>
            <LogoutButton />
          </div>
        </div>

        {/* Filters */}
        <div className="bg-white p-4 rounded shadow space-y-4">
          <h3 className="font-medium">Filters & Search</h3>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <input
              type="text"
              placeholder="Candidate ID"
              value={filters.candidateId}
              onChange={(e) => setFilters({...filters, candidateId: e.target.value})}
              className="border rounded px-3 py-2"
            />
            <input
              type="datetime-local"
              placeholder="Start Date"
              value={filters.startDate}
              onChange={(e) => setFilters({...filters, startDate: e.target.value})}
              className="border rounded px-3 py-2"
            />
            <input
              type="datetime-local"
              placeholder="End Date"
              value={filters.endDate}
              onChange={(e) => setFilters({...filters, endDate: e.target.value})}
              className="border rounded px-3 py-2"
            />
          </div>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <select
              value={filters.toolName}
              onChange={(e) => setFilters({...filters, toolName: e.target.value})}
              className="border rounded px-3 py-2"
            >
              <option value="">All Tools</option>
              {toolNames && toolNames.map(name => (
                <option key={name} value={name}>{name}</option>
              ))}
            </select>
            <input
              type="text"
              placeholder="Search (tool, process, description)"
              value={filters.search}
              onChange={(e) => setFilters({...filters, search: e.target.value})}
              className="border rounded px-3 py-2"
            />
          </div>
          <div className="flex space-x-2">
            <button 
              onClick={() => load(true)} 
              disabled={isFiltering}
              className="px-4 py-2 bg-blue-500 text-white rounded disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {isFiltering ? 'Filtering...' : 'Apply Filters'}
            </button>
            <button 
              onClick={() => {
                setFilters({candidateId: '', startDate: '', endDate: '', toolName: '', search: ''});
                setTimeout(() => load(true), 100);
              }}
              disabled={isFiltering}
              className="px-4 py-2 bg-gray-500 text-white rounded disabled:opacity-50"
            >
              Clear
            </button>
          </div>
        </div>

        {loading && (
          <div className="p-6 bg-white rounded shadow text-gray-700">
            <div className="flex items-center space-x-2">
              <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-blue-500"></div>
              <span>Loading detections...</span>
            </div>
          </div>
        )}

        {error && (
          <div className="p-4 bg-red-50 border border-red-200 rounded text-red-700">
            Failed to load detections: {error}
          </div>
        )}

        {!loading && !error && data && data.length === 0 && (
          <div className="p-6 bg-white rounded shadow text-gray-700">No detections found.</div>
        )}

        {!loading && !error && data && data.length > 0 && (
          <DetectionsTable detections={data} />
        )}
      </div>
    </DashboardLayout>
  );
}