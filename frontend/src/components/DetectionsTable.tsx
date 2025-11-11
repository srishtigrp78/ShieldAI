import React, { useMemo } from "react";
import DetectionReport from "../types/DetectionReport";

type Props = {
  detections: DetectionReport[];
};

function fmtDate(iso?: string) {
  if (!iso) return "—";
  try {
    const d = new Date(iso);
    return d.toLocaleString();
  } catch {
    return iso;
  }
}

function fmtConfidence(n?: number) {
  if (n == null || Number.isNaN(n)) return "—";
  const v = Number(n);
  const pct = v <= 1 ? v * 100 : v;
  return `${pct.toFixed(1)}%`;
}

export default function DetectionsTable({ detections }: Props) {
  // Limit initial render to prevent hanging
  const displayedDetections = useMemo(() => {
    return detections.slice(0, 100); // Show first 100 items
  }, [detections]);
  
  const DetectionRow = React.memo(({ detection, index }: { detection: DetectionReport, index: number }) => {
    const detectedAt = detection.detectedAt || detection.timestamp || detection["detectedAtUtc"];
    const process = detection.processName || detection.processDetails || "—";
    
    return (
      <tr key={detection.candidateId ?? index} className="hover:bg-gray-50">
        <td className="px-4 py-3 text-sm text-gray-800">{detection.toolName ?? "—"}</td>
        <td className="px-4 py-3 text-sm text-gray-700">
          <span className={`px-2 py-1 text-xs rounded-full ${
            detection.toolType === 'browser' ? 'bg-blue-100 text-blue-800' : 'bg-green-100 text-green-800'
          }`}>
            {detection.toolType ?? "—"}
          </span>
        </td>
        <td className="px-4 py-3 text-sm text-gray-700">{fmtDate(detectedAt)}</td>
        <td className="px-4 py-3 text-sm text-gray-700">{process}</td>
        <td className="px-4 py-3 text-sm text-gray-700">{fmtConfidence(detection.confidence)}</td>
        <td className="px-4 py-3 text-sm text-gray-600 truncate max-w-xl">{detection.description ?? "—"}</td>
      </tr>
    );
  });
  
  return (
    <div className="bg-white shadow rounded-md">
      {detections.length > 100 && (
        <div className="p-3 bg-yellow-50 border-b border-yellow-200 text-sm text-yellow-800">
          Showing first 100 of {detections.length} detections. Use filters to narrow results.
        </div>
      )}
      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-4 py-3 text-left text-sm font-medium text-gray-600">Tool</th>
              <th className="px-4 py-3 text-left text-sm font-medium text-gray-600">Type</th>
              <th className="px-4 py-3 text-left text-sm font-medium text-gray-600">Detected At</th>
              <th className="px-4 py-3 text-left text-sm font-medium text-gray-600">Process</th>
              <th className="px-4 py-3 text-left text-sm font-medium text-gray-600">Confidence</th>
              <th className="px-4 py-3 text-left text-sm font-medium text-gray-600">Description</th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-100">
            {displayedDetections.map((detection, idx) => (
              <DetectionRow key={detection.candidateId ?? idx} detection={detection} index={idx} />
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}