# Kubernetes Deployment Guide for KrabotNotif

This guide provides recommended configurations for deploying KrabotNotif in Kubernetes with optimized memory management and garbage collection.

## Memory Configuration

KrabotNotif includes built-in memory monitoring and garbage collection optimizations specifically designed for long-running containerized applications.

### Key Features

1. **Scheduled Garbage Collection**: Runs hourly (configurable) to prevent memory leaks
2. **Memory Health Checks**: Kubernetes liveness probe monitors memory usage
3. **OutOfMemoryError Handling**: Detailed logging when memory issues occur
4. **Memory Threshold Alerts**: Warnings at 80%, critical at 90% usage

## Recommended Kubernetes Deployment

### Basic Deployment with Memory Limits

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: krabotnotif
  labels:
    app: krabotnotif
spec:
  replicas: 1
  selector:
    matchLabels:
      app: krabotnotif
  template:
    metadata:
      labels:
        app: krabotnotif
    spec:
      containers:
      - name: krabotnotif
        image: arnaudroubinet/krabotnotif:latest-jvm
        
        # Resource limits and requests
        resources:
          requests:
            memory: "256Mi"
            cpu: "250m"
          limits:
            memory: "512Mi"
            cpu: "500m"
        
        # JVM options for containerized environment
        env:
        - name: JAVA_OPTS
          value: >-
            -XX:+UseG1GC
            -XX:MaxRAMPercentage=75.0
            -XX:InitialRAMPercentage=50.0
            -XX:+UseContainerSupport
            -XX:+ExitOnOutOfMemoryError
            -XX:+PrintGCDetails
            -XX:+PrintGCDateStamps
            -Xlog:gc*:stdout:time,level,tags
        
        # Required environment variables
        - name: DISCORD_HOOK
          valueFrom:
            secretKeyRef:
              name: krabotnotif-secrets
              key: discord-hook
        - name: KRALAND_USER
          valueFrom:
            secretKeyRef:
              name: krabotnotif-secrets
              key: kraland-user
        - name: KRALAND_PASSWORD
          valueFrom:
            secretKeyRef:
              name: krabotnotif-secrets
              key: kraland-password
        
        # Optional: Customize GC schedule (default: hourly)
        - name: JOB_GC_SCHEDULER_CRON
          value: "0 0 * ? * *"  # Every hour
        
        # Optional: Customize memory thresholds
        - name: MEMORY_WARNING_THRESHOLD
          value: "80"
        - name: MEMORY_CRITICAL_THRESHOLD
          value: "90"
        
        ports:
        - containerPort: 8080
          name: http
          protocol: TCP
        
        # Health checks with memory monitoring
        livenessProbe:
          httpGet:
            path: /q/health/live
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 30
          timeoutSeconds: 5
          failureThreshold: 3
        
        readinessProbe:
          httpGet:
            path: /q/health/ready
            port: 8080
          initialDelaySeconds: 10
          periodSeconds: 10
          timeoutSeconds: 3
          failureThreshold: 3
        
        # Startup probe for slow-starting apps
        startupProbe:
          httpGet:
            path: /q/health/started
            port: 8080
          initialDelaySeconds: 0
          periodSeconds: 5
          timeoutSeconds: 3
          failureThreshold: 30

---
apiVersion: v1
kind: Service
metadata:
  name: krabotnotif
spec:
  selector:
    app: krabotnotif
  ports:
  - port: 8080
    targetPort: 8080
    protocol: TCP
    name: http
  type: ClusterIP

---
# Secret for sensitive credentials
apiVersion: v1
kind: Secret
metadata:
  name: krabotnotif-secrets
type: Opaque
stringData:
  discord-hook: "your-discord-webhook-url"
  kraland-user: "your-kraland-username"
  kraland-password: "your-kraland-password"
```

## Memory Configuration Options

### JVM Heap Size

The application uses percentage-based memory allocation which is ideal for containers:

```yaml
env:
- name: JAVA_OPTS
  value: >-
    -XX:MaxRAMPercentage=75.0
    -XX:InitialRAMPercentage=50.0
```

With a 512Mi memory limit:
- Initial heap: ~256Mi (50%)
- Max heap: ~384Mi (75%)
- Remaining ~128Mi for non-heap memory (thread stacks, native memory, etc.)

### Garbage Collector Options

For small containers (< 1Gi), G1GC is recommended:

```yaml
- name: JAVA_OPTS
  value: "-XX:+UseG1GC -XX:MaxRAMPercentage=75.0"
```

For larger containers (> 1Gi), consider ZGC:

```yaml
- name: JAVA_OPTS
  value: "-XX:+UseZGC -XX:MaxRAMPercentage=75.0"
```

### GC Logging

Enable GC logging for troubleshooting:

```yaml
- name: JAVA_OPTS
  value: >-
    -XX:+UseG1GC
    -Xlog:gc*:stdout:time,level,tags
```

View GC logs with:
```bash
kubectl logs -f deployment/krabotnotif | grep "GC"
```

## Application-Level Memory Configuration

### GC Schedule

Customize the garbage collection schedule:

```yaml
env:
- name: JOB_GC_SCHEDULER_CRON
  value: "0 0 */2 ? * *"  # Every 2 hours
```

Default: `0 0 * ? * *` (every hour)

### Memory Thresholds

Configure when memory warnings and alerts are triggered:

```yaml
env:
- name: MEMORY_WARNING_THRESHOLD
  value: "70"   # Warning at 70% usage
- name: MEMORY_CRITICAL_THRESHOLD
  value: "85"   # Critical at 85% usage
```

When critical threshold is reached, the liveness probe will fail, triggering a pod restart.

## Monitoring Memory Usage

### Health Check Endpoint

Check memory status via the health endpoint:

```bash
kubectl port-forward deployment/krabotnotif 8080:8080
curl http://localhost:8080/q/health/live
```

Response includes:
```json
{
  "status": "UP",
  "checks": [
    {
      "name": "Memory Usage Health Check",
      "status": "UP",
      "data": {
        "max_memory_mb": 384,
        "used_memory_mb": 128,
        "free_memory_mb": 256,
        "usage_percent": 33,
        "status": "OK - Memory usage normal"
      }
    }
  ]
}
```

### Application Logs

Monitor memory statistics in logs:

```bash
kubectl logs -f deployment/krabotnotif | grep -E "Memory|GC"
```

You'll see:
- Initial memory configuration at startup
- Hourly GC execution logs with before/after memory stats
- Memory threshold warnings
- OutOfMemoryError details if they occur

### Prometheus Metrics (Optional)

If using Prometheus, you can scrape JVM metrics from the `/q/metrics` endpoint by adding the `quarkus-micrometer-registry-prometheus` dependency.

## Troubleshooting

### Pod Keeps Restarting Due to OOM

1. Check the last logs before restart:
   ```bash
   kubectl logs deployment/krabotnotif --previous | tail -100
   ```

2. Look for OutOfMemoryError or memory threshold messages

3. Increase memory limits:
   ```yaml
   resources:
     limits:
       memory: "768Mi"  # Increase from 512Mi
   ```

4. Adjust heap percentage:
   ```yaml
   - name: JAVA_OPTS
     value: "-XX:MaxRAMPercentage=70.0"  # Reduce from 75%
   ```

### High Memory Usage

1. Check current memory via health endpoint
2. Review GC logs for frequency and effectiveness
3. Consider running GC more frequently:
   ```yaml
   - name: JOB_GC_SCHEDULER_CRON
     value: "0 0/30 * ? * *"  # Every 30 minutes
   ```

### Memory Growing Over Time

1. Enable verbose GC logging
2. Monitor for memory leaks in application logs
3. Check if kramails or notifications are accumulating
4. Review the GC job execution logs for freed memory amounts

## Best Practices

1. **Always set resource limits**: Prevents pod from consuming all node memory
2. **Use percentage-based heap sizing**: Works better than fixed sizes in containers
3. **Monitor health checks**: Set up alerts on liveness probe failures
4. **Enable GC logging**: Essential for troubleshooting memory issues
5. **Start conservative**: Begin with lower memory limits and increase if needed
6. **Use native image for production**: Consider `arnaudroubinet/krabotnotif:latest-native` for lower memory footprint

## Native Image Deployment

For even better memory efficiency, use the native image:

```yaml
spec:
  containers:
  - name: krabotnotif
    image: arnaudroubinet/krabotnotif:latest-native
    
    resources:
      requests:
        memory: "128Mi"  # Native uses less memory
        cpu: "100m"
      limits:
        memory: "256Mi"
        cpu: "200m"
```

Native images have:
- Faster startup time
- Lower memory footprint
- No JVM GC overhead (uses different memory model)
- Better suited for memory-constrained environments

## Example: Resource-Constrained Environment

For very small clusters or limited resources:

```yaml
resources:
  requests:
    memory: "128Mi"
    cpu: "100m"
  limits:
    memory: "256Mi"
    cpu: "200m"

env:
- name: JAVA_OPTS
  value: >-
    -XX:+UseSerialGC
    -XX:MaxRAMPercentage=70.0
- name: JOB_GC_SCHEDULER_CRON
  value: "0 0/15 * ? * *"  # Every 15 minutes
```

## Additional Resources

- [Quarkus on Kubernetes](https://quarkus.io/guides/deploying-to-kubernetes)
- [Java Container Memory](https://developers.redhat.com/blog/2017/03/14/java-inside-docker)
- [G1GC Tuning](https://www.oracle.com/technical-resources/articles/java/g1gc.html)
