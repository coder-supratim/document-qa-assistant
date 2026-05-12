# Deployment Guide - Document Q&A Assistant

## Local Development

### Quick Start
```bash
# Set API key
export OPENAI_API_KEY="sk-..."

# Build and run
mvn clean install
mvn spring-boot:run

# Application runs on http://localhost:8080
```

### Using IDE
1. Open project in IntelliJ IDEA or VS Code
2. Install Spring Boot extension
3. Set `OPENAI_API_KEY` environment variable
4. Run `DocumentQAApplication.java` directly

---

## Docker Deployment

### Build and Run
```bash
# Build image
docker build -t document-qa-assistant .

# Run container
docker run -p 8080:8080 \
  -e OPENAI_API_KEY="sk-..." \
  document-qa-assistant
```

### Using Docker Compose
```bash
# Set API key
export OPENAI_API_KEY="sk-..."

# Start services
docker-compose up -d

# View logs
docker-compose logs -f document-qa-api

# Stop services
docker-compose down
```

---

## Cloud Deployments

### AWS EC2

1. **Launch Instance**
   - AMI: Ubuntu 22.04 LTS
   - Instance type: t3.medium
   - Storage: 20GB

2. **Install Java & Maven**
   ```bash
   sudo apt update
   sudo apt install openjdk-17-jdk maven -y
   ```

3. **Deploy Application**
   ```bash
   git clone <your-repo>
   cd document-qa-assistant
   export OPENAI_API_KEY="sk-..."
   mvn spring-boot:run &
   ```

4. **Configure Security Group**
   - Allow inbound: Port 8080 (HTTP)
   - Allow inbound: Port 22 (SSH)

### AWS Lambda + API Gateway

```bash
# Requires spring-cloud-function setup
# Build as function
mvn clean package -Pfunctionzip

# Deploy via AWS Console
# Create API Gateway -> Lambda integration
```

### Google Cloud Run

```bash
# Build and push image
docker build -t gcr.io/PROJECT_ID/document-qa .
docker push gcr.io/PROJECT_ID/document-qa

# Deploy to Cloud Run
gcloud run deploy document-qa \
  --image gcr.io/PROJECT_ID/document-qa \
  --platform managed \
  --region us-central1 \
  --set-env-vars OPENAI_API_KEY=sk-...
```

### Azure App Service

```bash
# Create resource group
az group create -n myResourceGroup -l eastus

# Create App Service plan
az appservice plan create \
  -n myAppServicePlan \
  -g myResourceGroup \
  --sku B2

# Deploy JAR
az webapp up \
  --name document-qa-app \
  --resource-group myResourceGroup \
  --runtime java17
```

### Heroku

```bash
# Create Procfile
echo "web: java -Dserver.port=\$PORT -jar target/*.jar" > Procfile

# Deploy
heroku create document-qa-app
heroku config:set OPENAI_API_KEY=sk-...
git push heroku main
```

---

## Production Checklist

- [ ] API key secured (use secrets manager, not env vars)
- [ ] Database setup (PostgreSQL recommended)
- [ ] Logging configured (ELK, Splunk, etc.)
- [ ] Monitoring enabled (CloudWatch, New Relic, etc.)
- [ ] SSL/TLS certificate configured
- [ ] Rate limiting implemented
- [ ] Request logging enabled
- [ ] Error handling robust
- [ ] Backup strategy in place
- [ ] Load testing completed
- [ ] Security audit done
- [ ] Documentation updated
- [ ] CI/CD pipeline setup

---

## Performance Tuning

### JVM Configuration
```bash
java -Xmx2g \
  -Xms512m \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -jar app.jar
```

### Database Optimization
```properties
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.jpa.properties.hibernate.jdbc.batch_size=20
```

### Spring AI Optimization
```properties
# Enable request caching
spring.ai.openai.chat.options.cache=true

# Use streaming for large responses
spring.ai.openai.chat.streaming=true
```

---

## Monitoring & Logging

### ELK Stack Integration
```properties
logging.level.root=INFO
spring.jpa.show-sql=false

# Send logs to Logstash
logging.appender=LOGSTASH
```

### CloudWatch Integration (AWS)
```properties
# Add CloudWatch appender to logback-spring.xml
# Configure via AWS SDK
```

### Health Checks
```bash
# Container health check
curl http://localhost:8080/api/documents/health

# Readiness probe
curl http://localhost:8080/actuator/health/readiness

# Liveness probe
curl http://localhost:8080/actuator/health/liveness
```

---

## Scaling Strategies

### Horizontal Scaling
1. Use load balancer (AWS ALB, NGINX)
2. Container orchestration (Kubernetes)
3. Auto-scaling groups

### Kubernetes Deployment
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: document-qa
spec:
  replicas: 3
  selector:
    matchLabels:
      app: document-qa
  template:
    metadata:
      labels:
        app: document-qa
    spec:
      containers:
      - name: api
        image: document-qa-assistant:latest
        ports:
        - containerPort: 8080
        env:
        - name: OPENAI_API_KEY
          valueFrom:
            secretKeyRef:
              name: openai-key
              key: api-key
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
```

---

## Troubleshooting

### Application won't start
```bash
# Check logs
docker logs container-id

# Verify environment variables
echo $OPENAI_API_KEY

# Check port availability
lsof -i :8080
```

### Performance issues
```bash
# Monitor memory
jps -l
jmap -heap <pid>

# Monitor threads
jstack <pid>

# Monitor GC
java -XX:+PrintGCDetails -jar app.jar
```

### API errors
```bash
# Enable debug logging
export LOGGING_LEVEL_ROOT=DEBUG
mvn spring-boot:run

# Check API connectivity
curl -v http://localhost:8080/api/documents/health
```

---

## Maintenance

### Regular Updates
```bash
# Update Maven dependencies
mvn versions:update-properties

# Update Spring Boot
mvn versions:update-parent
```

### Database Backup
```bash
# PostgreSQL backup
pg_dump document_qa > backup.sql

# Restore
psql document_qa < backup.sql
```

### Log Rotation
```properties
logging.file.name=logs/application.log
logging.file.max-size=10MB
logging.file.max-history=30
```

---

## Support & Debugging

For issues:
1. Check logs: `docker logs container-id`
2. Verify API key: `echo $OPENAI_API_KEY`
3. Test connectivity: `curl -v http://localhost:8080/api/documents`
4. Check Spring docs: https://spring.io/projects/spring-ai
5. OpenAI status: https://status.openai.com

---

**Last Updated**: April 2024
