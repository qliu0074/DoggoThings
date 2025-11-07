## Performance Testing

1. Ensure the API is running locally (`mvn spring-boot:run`).
2. Install [k6](https://k6.io).
3. Execute `k6 run perf/k6/orders-load.js`.
4. Observe latency/error metrics to catch regressions on critical order flow.

Adjust VUs/duration to mimic your production traffic profiles.
