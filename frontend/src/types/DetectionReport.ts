export default interface DetectionReport {
  candidateId?: string | null;
  toolName: string;
  toolType?: string;
  // some backends use 'timestamp', others 'detectedAt'
  timestamp?: string;
  detectedAt?: string;
  // processDetails / processName
  processDetails?: string;
  processName?: string;
  confidence?: number; // expected 0..1 or 0..100
  description?: string;
  [k: string]: any;
}