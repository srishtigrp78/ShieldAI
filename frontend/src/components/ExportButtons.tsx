import React, { useState } from 'react';

interface ExportButtonsProps {
  filters: {
    candidateId: string;
    startDate: string;
    endDate: string;
    toolName: string;
    search: string;
  };
}

export default function ExportButtons({ filters }: ExportButtonsProps) {
  const [loading, setLoading] = useState({ csv: false, pdf: false });

  const buildQueryParams = () => {
    const params = new URLSearchParams();
    if (filters.candidateId) params.append('candidateId', filters.candidateId);
    if (filters.startDate) params.append('startDate', filters.startDate);
    if (filters.endDate) params.append('endDate', filters.endDate);
    if (filters.toolName) params.append('toolName', filters.toolName);
    if (filters.search) params.append('search', filters.search);
    return params.toString();
  };

  const downloadFile = async (endpoint: string, filename: string, type: 'csv' | 'pdf') => {
    setLoading(prev => ({ ...prev, [type]: true }));
    
    try {
      const token = localStorage.getItem('token');
      const queryParams = buildQueryParams();
      const url = `/api/export/${endpoint}${queryParams ? `?${queryParams}` : ''}`;
      
      const response = await fetch(url, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });

      if (!response.ok) {
        throw new Error(`Export failed: ${response.status}`);
      }

      const blob = await response.blob();
      const downloadUrl = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = downloadUrl;
      link.download = filename;
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(downloadUrl);
      
    } catch (error) {
      console.error(`${type.toUpperCase()} export failed:`, error);
      alert(`Failed to export ${type.toUpperCase()}. Please try again.`);
    } finally {
      setLoading(prev => ({ ...prev, [type]: false }));
    }
  };

  const exportCSV = () => {
    const timestamp = new Date().toISOString().slice(0, 19).replace(/:/g, '-');
    downloadFile('csv', `detections_${timestamp}.csv`, 'csv');
  };

  const exportPDF = () => {
    const timestamp = new Date().toISOString().slice(0, 19).replace(/:/g, '-');
    downloadFile('pdf', `detections_${timestamp}.pdf`, 'pdf');
  };

  return (
    <div className="flex space-x-2">
      <button
        onClick={exportCSV}
        disabled={loading.csv}
        className="px-3 py-1 bg-green-500 text-white rounded text-sm hover:bg-green-600 disabled:opacity-50 flex items-center"
      >
        {loading.csv ? (
          <>
            <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
              <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
              <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
            Exporting...
          </>
        ) : (
          'Export CSV'
        )}
      </button>
      
      <button
        onClick={exportPDF}
        disabled={loading.pdf}
        className="px-3 py-1 bg-red-500 text-white rounded text-sm hover:bg-red-600 disabled:opacity-50 flex items-center"
      >
        {loading.pdf ? (
          <>
            <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
              <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
              <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
            Generating...
          </>
        ) : (
          'Export PDF'
        )}
      </button>
    </div>
  );
}