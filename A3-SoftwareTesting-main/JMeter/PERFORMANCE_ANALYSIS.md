# Pet Clinic JMeter Performance Analysis

> [NEW] Rubric evidence for the JMeter performance-testing criterion.

## Objective and scope

Measure the Pet Clinic web application's response time, error rate and throughput at four separately executed load levels. The test covers five representative read paths, including the required billing page:

1. `/` — Welcome page
2. `/vets.html` — Veterinarians list
3. `/owners?lastName=` — Owner search
4. `/owners/6` — Owner details
5. `/owners/6/pets/7/visits/1/services` — Visit billing calculation

Each sampler has a Response Assertion that checks expected page content. A successful HTTP status alone is therefore not treated as proof that the correct page was returned.

## Test-plan configuration

| Setting | Value |
|---|---|
| Test plan | `PetClinic_Performance_Test.jmx` |
| Execution model | Thread groups run consecutively (`TestPlan.serialize_threadgroups=true`) so results are attributable to one load level at a time. |
| Load levels | 1, 10, 25 and 50 virtual users |
| Ramp-up | 10 seconds for every thread group |
| Scheduler duration | 20 seconds for every thread group |
| Loop controller | Forever, bounded by the 20-second scheduler |
| Think time | Constant Timer: 300 ms before every request |
| Error handling | Continue on error |
| Assertions | One Response Assertion on each of the five samplers |
| Required listeners | View Results Tree, Aggregate Report, Summary Report, Graph Results |
| Raw evidence | `results.jtl` in CSV format |

## Execution environment

| Item | Value |
|---|---|
| Execution time | 2026-09-01 18:17:18 CST |
| SUT | `A3-Testing-Site.jar` (SHA-256 `7246e232292a4a24e72d9cb5b0b08d29b212951e8099106c540b405dd0a9f9eb`) |
| Target | `http://localhost:8080` |
| JMeter | Apache JMeter 5.6.3 |
| Java | OpenJDK 21.0.12.1 |
| Load generator / OS | Apple Silicon macOS, `aarch64`, 8 available processors, JMeter max heap 1 GB |
| Network topology | JMeter and the SUT ran on the same machine over loopback (`localhost`). |

## Aggregate results

Source: `results.jtl`; totals were grouped by the JMeter thread-group name. The reproducible CSV form is `load_level_summary.csv`.

| Users | Samples | Mean response (ms) | Min–max (ms) | Error rate | Measured window (s) | Throughput (req/s) |
|---:|---:|---:|---:|---:|---:|---:|
| 1 | 59 | 29.37 | 6–332 | 0.00% | 19.444 | 3.03 |
| 10 | 487 | 10.99 | 1–89 | 0.00% | 19.682 | 24.74 |
| 25 | 1,200 | 8.63 | 0–81 | 0.00% | 19.522 | 61.47 |
| 50 | 2,427 | 4.79 | 0–46 | 0.00% | 19.704 | 123.17 |

## Interpretation and release recommendation

- All 4,173 samples passed their response assertions; no HTTP or content-validation failure was recorded.
- Throughput rose from 3.03 req/s at one user to 123.17 req/s at 50 users, with no observed error-rate increase.
- Mean response time decreased as the consecutive test groups progressed. This does **not** demonstrate that the application becomes faster under higher load: the 1-user group ran first, while later groups likely benefited from JVM warm-up, connection reuse and local caching.
- This is a useful local functional-performance baseline. It supports a conditional recommendation to proceed with the assignment release at the tested 50-user localhost workload, subject to the listed limitations.

## Residual risk and next action

The result must not be presented as production capacity evidence. The load generator and SUT share CPU, memory and loopback networking; the 20-second windows are also too short to assess sustained load, memory leaks or database contention.

Before a production-like claim, repeat the same plan with the JMeter generator and SUT on separate machines, a controlled warm-up phase, at least three repeated runs per load level, and a longer steady-state duration. Compare median and 95th-percentile response times as well as error rate and throughput.

## Reproduction procedure

Start the SUT from the assignment root in one terminal:

```sh
java -jar A3-Testing-Site.jar
```

In a second terminal, run JMeter from the assignment root. Use fresh timestamped paths so existing evidence is not overwritten:

```sh
jmeter -n -t JMeter/PetClinic_Performance_Test.jmx \
  -JHOST=localhost -JPORT=8080 \
  -l JMeter/results-YYYYMMDD-HHMMSS.jtl \
  -e -o JMeter/html-dashboard-YYYYMMDD-HHMMSS
```

The run produces the required CSV/JTL evidence and an HTML dashboard. Update `load_level_summary.csv` and the Aggregate Results table only from that run's JTL file; retain the run date, JAR hash and environment with the submitted evidence.
