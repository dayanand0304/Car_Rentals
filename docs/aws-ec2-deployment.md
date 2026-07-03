# AWS EC2 Deployment Guide

This guide deploys the Car-Rentals Spring Boot app on **AWS EC2 free tier** alongside your existing Render deployment. Render stays useful for fast iteration; EC2 gives hands-on experience with security groups, networking, and manual scaling.

## Architecture

```text
Internet -> Security Group (443/8080) -> EC2 (Docker) -> RDS PostgreSQL
                                                      -> ElastiCache Redis (optional)
```

## Prerequisites

- AWS account
- Domain or public IP for testing
- Local Docker image build (`docker build -t car-rentals .`)

## 1. Create RDS PostgreSQL (optional stretch)

1. Open **RDS** -> **Create database**
2. Engine: PostgreSQL 16
3. Template: **Free tier**
4. DB identifier: `car-rentals-db`
5. Master username/password: store in AWS Secrets Manager or SSM Parameter Store
6. Public access: **No**
7. VPC: same VPC you will use for EC2
8. Initial database name: `car_rentals`

Note the endpoint: `car-rentals-db.xxxxx.region.rds.amazonaws.com:5432`

## 2. Launch EC2 instance

1. AMI: **Ubuntu 24.04 LTS**
2. Instance type: `t2.micro` or `t3.micro` (free tier eligible)
3. Key pair: create/download a `.pem` file
4. Security group inbound rules:
   - SSH (22) from your IP
   - HTTP (8080) from `0.0.0.0/0` for testing, or restrict to your IP
   - HTTPS (443) if terminating TLS on the instance
5. Storage: 20–30 GB gp3

## 3. Configure networking access to RDS

If using RDS:

- Place EC2 and RDS in the same VPC
- RDS security group: allow inbound **5432** from the EC2 security group
- EC2 security group: allow outbound to RDS on 5432

## 4. Install Docker on EC2

```bash
ssh -i your-key.pem ubuntu@YOUR_EC2_PUBLIC_IP

sudo apt-get update
sudo apt-get install -y docker.io docker-compose-plugin
sudo usermod -aG docker ubuntu
newgrp docker
```

## 5. Deploy the application

Copy project files or pull from GitHub:

```bash
git clone https://github.com/YOUR_USER/Car-Rentals.git
cd Car-Rentals
```

Create an environment file:

```bash
cat > .env <<'EOF'
DB_URL=jdbc:postgresql://YOUR_RDS_ENDPOINT:5432/car_rentals
DB_USERNAME=postgres
DB_PASSWORD=YOUR_RDS_PASSWORD
DDL_AUTO=update
CACHE_TYPE=redis
REDIS_HOST=127.0.0.1
REDIS_PORT=6379
JWT_SECRET=replace-with-a-long-random-secret
AVAILABLE_CARS_CACHE_TTL=90
EOF
```

### Option A: Docker Compose on EC2 (Postgres + Redis on same host)

For a simpler free-tier setup without RDS:

```bash
docker compose up -d --build
```

### Option B: App container only (RDS backend)

```bash
docker build -t car-rentals .
docker run -d --name car-rentals \
  --restart unless-stopped \
  -p 8080:8080 \
  --env-file .env \
  car-rentals
```

Install Redis on EC2 for caching:

```bash
sudo apt-get install -y redis-server
sudo systemctl enable redis-server
```

Set `CACHE_TYPE=redis` and `REDIS_HOST=127.0.0.1`.

## 6. Verify deployment

```bash
curl http://YOUR_EC2_PUBLIC_IP:8080/api/health
curl http://YOUR_EC2_PUBLIC_IP:8080/
```

Swagger UI: `http://YOUR_EC2_PUBLIC_IP:8080/swagger-ui/index.html`

## 7. Production hardening checklist

- [ ] Put Nginx or ALB in front of the app
- [ ] Terminate TLS with ACM + ALB, or Let's Encrypt on Nginx
- [ ] Restrict security group ingress to ALB only
- [ ] Store secrets in AWS Secrets Manager, not `.env` files
- [ ] Enable CloudWatch agent for logs and CPU/memory alarms
- [ ] Use RDS automated backups and multi-AZ only when needed (not free tier)

## Interview talking points

- **Render vs EC2**: Render for speed; EC2 for infrastructure control (SGs, VPC, manual scaling).
- **Cache invalidation**: available-car listings cached for 90s; cache cleared on booking, return, cancel, repair, and car status changes.
- **Event-driven side effects**: booking completion, damage, and overdue rentals publish Spring events; listeners handle notifications/logging independently of core booking logic.
- **Testing**: JUnit 5 + Mockito for pricing/auth; Testcontainers spins up real PostgreSQL for soft-delete query integration tests.
- **CI/CD**: GitHub Actions runs tests, builds the JAR, and builds the Docker image on every push.

## Useful commands

```bash
# View app logs
docker logs -f car-rentals

# Restart after env change
docker restart car-rentals

# Run CI locally
./mvnw clean test
docker build -t car-rentals .
```
