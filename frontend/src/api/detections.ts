import DetectionReport from "../types/DetectionReport";

const API_PATH = "/api/detections";

export async function fetchDetections(queryParams?: string): Promise<DetectionReport[]> {
  const url = queryParams ? `${API_PATH}?${queryParams}` : API_PATH;
  console.debug("[frontend] fetchDetections url=", url);

  const token = localStorage.getItem('token');
  const headers: Record<string, string> = {
    "Accept": "application/json",
  };
  
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const resp = await fetch(url, {
    method: "GET",
    headers,
    credentials: "same-origin",
  });

  if (!resp.ok) {
    const text = await resp.text().catch(() => "");
    throw new Error(`API error ${resp.status}${text ? ": " + text : ""}`);
  }

  const data = await resp.json();
  return Array.isArray(data) ? data : [];
}