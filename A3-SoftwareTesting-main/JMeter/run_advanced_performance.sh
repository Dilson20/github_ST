#!/usr/bin/env bash
# ==============================================================================
# Pet Clinic Advanced Performance & Load Benchmark Runner
# ==============================================================================
# Usage:
#   ./run_advanced_performance.sh [HOST] [PORT] [DURATION_SEC] [RAMP_UP_SEC]
# Example:
#   ./run_advanced_performance.sh localhost 8080 30 10
# ==============================================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "${SCRIPT_DIR}"

HOST="${1:-localhost}"
PORT="${2:-8080}"
DURATION="${3:-20}"
RAMP_UP="${4:-10}"

TIMESTAMP=$(date +"%Y%m%d-%H%M%S")
OUTPUT_DIR="${SCRIPT_DIR}/output_${TIMESTAMP}"
JTL_FILE="${OUTPUT_DIR}/results.jtl"
DASHBOARD_DIR="${OUTPUT_DIR}/html-dashboard"

mkdir -p "${OUTPUT_DIR}"

echo "======================================================================"
echo " Starting Pet Clinic Advanced Performance Benchmark"
echo " Target Host     : ${HOST}:${PORT}"
echo " Duration / Group: ${DURATION}s (Ramp-up: ${RAMP_UP}s)"
echo " Test Plan       : PetClinic_Advanced_Performance_Test.jmx"
echo " Results Dir     : ${OUTPUT_DIR}"
echo "======================================================================"

# 1. Health check target SUT
echo "[1/3] Checking SUT availability on http://${HOST}:${PORT}/ ..."
if ! curl -s -o /dev/null -w "%{http_code}" "http://${HOST}:${PORT}/" | grep -q "200"; then
    echo "ERROR: Target application at http://${HOST}:${PORT}/ is not returning HTTP 200."
    echo "Please ensure 'java -jar A3-Testing-Site.jar' is running before starting the benchmark."
    exit 1
fi
echo "SUT is responsive. Proceeding."

# 2. Execute JMeter in non-GUI mode
echo "[2/3] Executing JMeter performance run..."
jmeter -n \
  -t "PetClinic_Advanced_Performance_Test.jmx" \
  -JHOST="${HOST}" \
  -JPORT="${PORT}" \
  -JDURATION="${DURATION}" \
  -JRAMP_UP="${RAMP_UP}" \
  -l "${JTL_FILE}" \
  -e -o "${DASHBOARD_DIR}"

# 3. Generate summary table
echo "[3/3] Generating summary report..."
python3 - << PYEOF
import csv
from collections import defaultdict

jtl_path = "${JTL_FILE}"
summary_csv = "${OUTPUT_DIR}/load_level_summary.csv"

metrics = defaultdict(lambda: {"count": 0, "errors": 0, "times": [], "start": float('inf'), "end": 0})

with open(jtl_path, "r", encoding="utf-8") as f:
    reader = csv.DictReader(f)
    for row in reader:
        # Group by thread group name
        thread_name = row["threadName"]
        group_label = thread_name.split()[0] if " " in thread_name else thread_name
        elapsed = float(row["elapsed"])
        success = row["success"].lower() == "true"
        ts = float(row["timeStamp"])

        m = metrics[group_label]
        m["count"] += 1
        if not success:
            m["errors"] += 1
        m["times"].append(elapsed)
        m["start"] = min(m["start"], ts)
        m["end"] = max(m["end"], ts + elapsed)

with open(summary_csv, "w", newline="", encoding="utf-8") as out:
    writer = csv.writer(out)
    writer.writerow([
        "thread_group", "samples", "mean_ms", "min_ms", "max_ms",
        "p90_ms", "p95_ms", "error_pct", "duration_s", "throughput_req_s"
    ])
    for group in sorted(metrics.keys()):
        m = metrics[group]
        cnt = m["count"]
        times = sorted(m["times"])
        mean_t = sum(times) / cnt if cnt else 0
        min_t = times[0] if cnt else 0
        max_t = times[-1] if cnt else 0
        p90 = times[int(cnt * 0.90)] if cnt else 0
        p95 = times[int(cnt * 0.95)] if cnt else 0
        err_pct = (m["errors"] / cnt * 100.0) if cnt else 0
        dur_s = (m["end"] - m["start"]) / 1000.0 if m["end"] > m["start"] else 1
        tps = cnt / dur_s if dur_s > 0 else 0

        writer.writerow([
            group, cnt, f"{mean_t:.2f}", f"{min_t:.0f}", f"{max_t:.0f}",
            f"{p90:.0f}", f"{p95:.0f}", f"{err_pct:.2f}%", f"{dur_s:.2f}", f"{tps:.2f}"
        ])

print("\n=== Performance Execution Summary ===")
with open(summary_csv, "r") as f:
    print(f.read())
PYEOF

echo "Benchmark complete."
echo "CSV Summary    : ${OUTPUT_DIR}/load_level_summary.csv"
echo "HTML Dashboard : ${DASHBOARD_DIR}/index.html"
