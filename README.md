# Global Credit Scoring Engine

A **Data Hub** demonstration application for Tanzu Experience Day, showcasing the integration of multiple data services on **Tanzu Platform for Cloud Foundry**.

## 🏗️ Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        GLOBAL CREDIT SCORING ENGINE                         │
│                            (Spring Boot 3.x)                                │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│    ┌──────────────┐    ┌───────────────┐    ┌──────────────────────────┐   │
│    │ REST API     │───▶│  RabbitMQ     │───▶│  Credit Score            │   │
│    │ POST /apply  │    │  Queue        │    │  Calculator              │   │
│    └──────────────┘    │  (credit-msg) │    │  (@RabbitListener)       │   │
│                        └───────────────┘    └────────────┬─────────────┘   │
│                                                          │                  │
│                              ┌───────────────────────────┼────────────┐     │
│                              │                           │            │     │
│                              ▼                           ▼            │     │
│                   ┌──────────────────┐       ┌───────────────────┐   │     │
│                   │   PostgreSQL     │       │  VMware Tanzu     │   │     │
│                   │   (credit-db)    │       │  GemFire          │   │     │
│                   │                  │       │  (credit-cache)   │   │     │
│                   │  UserFinancials  │       │                   │   │     │
│                   │  - SSN           │       │  CreditScoreCache │   │     │
│                   │  - creditHistory │       │  - calculatedScore│   │     │
│                   │  - criminalRecord│       │  - riskLevel      │   │     │
│                   │  - riskLevel     │       │  (Sub-second      │   │     │
│                   └──────────────────┘       │   global access)  │   │     │
│                                              └───────────────────┘   │     │
│                                                          │            │     │
│    ┌──────────────┐                                      │            │     │
│    │ REST API     │◀─────────────────────────────────────┘            │     │
│    │ GET /score   │           (Real-time retrieval)                   │     │
│    └──────────────┘                                                   │     │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

## 🎯 The "Data Hub" Concept

This application demonstrates the **Data Hub** pattern on **Tanzu Platform for Cloud Foundry**, leveraging native service tiles for maximum performance and integration:

1. **PostgreSQL (Tile)**: Managed relational database service storing source-of-truth financial records
2. **RabbitMQ (Tile)**: Native message broker decoupling ingestion from processing
3. **Tanzu GemFire (Tile)**: High-speed in-memory data grid caching calculations for sub-second access

### Why This Matters

| Challenge | Solution |
|-----------|----------|
| Complex joins are slow | Offload to async processing layer |
| Real-time score lookups needed | Cache in GemFire for < 10ms response |
| Service Management | All services managed as Tiles within the Platform |
| Scalability | Each component scales properly within the foundation |

## 🚀 Deployment

### Prerequisites

1. **CF CLI** installed and logged in
2. **Java 17+** and **Maven 3.8+** installed
3. **Services available** in your marketplace:
   - PostgreSQL (e.g., `postgres`, `p.mysql`)
   - RabbitMQ (e.g., `p.rabbitmq`, `cloudamqp`)
   - GemFire (e.g., `p-cloudcache`, `tanzu-gemfire`)

### Step 1: Initial Setup

Clone the repository and set up the necessary environment variables for GemFire automation:

```powershell
# Clone the repository
git clone https://github.com/pvladMQ/global-credit-engine.git
cd global-credit-engine

# Define GemFire Management API credentials
$GEMFIRE_API_URL = "https://<your-gemfire-mgmt-endpoint>/management/v1"
$GEMFIRE_USER = "cluster_operator"
$GEMFIRE_PASSWORD = "your-password"
```

### Step 2: GemFire Data Hub Setup

The application uses `ClientRegionShortcut.PROXY`, meaning it expects the region to already exist on the server. The application will fail to start if the region is missing.

Run the following command to create the required **PARTITION** region:

```powershell
# Create the CreditScoreCache region
curl.exe -k -X POST "$GEMFIRE_API_URL/regions" -u "$GEMFIRE_USER:$GEMFIRE_PASSWORD" -H "Content-Type: application/json" -d "{\"name`":`"CreditScoreCache`",`"type`":`"PARTITION`"}"
```

#### Verify Region Creation
Verify the region exists before starting the Spring Boot application:

```powershell
# List regions to verify
curl.exe -k -u "$GEMFIRE_USER:$GEMFIRE_PASSWORD" "$GEMFIRE_API_URL/regions"
```

### Step 3: Create Service Instances (Cloud Foundry)

If deploying to Tanzu Platform for Cloud Foundry, ensure the services are created and bound:

```bash
# Create PostgreSQL instance
cf create-service postgres standard credit-db

# Create RabbitMQ instance
cf create-service p.rabbitmq standard credit-msg

# Create GemFire/Tanzu Data instance
cf create-service p-cloudcache standard credit-cache
```

> **Note**: Service names and plans may vary by foundation. Check `cf marketplace` for available options.

### Step 4: Build and Deploy

```bash
# Build the application
mvn clean package -DskipTests

# Push to Cloud Foundry
cf push
```

The `manifest.yml` automatically binds the three services and configures the cloud profile.

### Step 5: Verify Deployment

```bash
# Check app status
cf app global-credit-engine

# View logs
cf logs global-credit-engine --recent
```


## 📡 API Usage

### Submit a Credit Application

```bash
curl -X POST https://global-credit-engine.<your-cf-domain>/api/apply \
  -H "Content-Type: application/json" \
  -d '{
    "ssn": "123-45-6789",
    "fullName": "John Doe",
    "requestedCreditLimit": 50000,
    "applicationReason": "Home Renovation"
  }'
```

**Response:**
```json
{
  "status": "accepted",
  "message": "Credit application submitted for processing",
  "ssn": "123-45-6789",
  "trackingInfo": "Check /api/score/123-45-6789 for results"
}
```

### Retrieve Credit Score (from GemFire Cache)

```bash
curl https://global-credit-engine.<your-cf-domain>/api/score/123-45-6789
```

**Response:**
```json
{
  "status": "success",
  "ssn": "123-45-6789",
  "fullName": "John Doe",
  "calculatedScore": 78,
  "riskLevel": "MEDIUM_RISK",
  "calculatedAt": "2026-02-03T12:30:45",
  "source": "GemFire Cache (sub-second retrieval)"
}
```

## 🔧 Local Development

### Prerequisites

- Java 17+
- Maven 3.8+
- Docker (for local services)

### Start Local Services

```bash
# PostgreSQL
docker run -d --name credit-postgres \
  -e POSTGRES_DB=creditdb \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:15

# RabbitMQ
docker run -d --name credit-rabbitmq \
  -p 5672:5672 -p 15672:15672 \
  rabbitmq:3-management
```

> **Note**: For local GemFire, you'll need a local cluster or mock the repository.

### Run the Application

```bash
mvn spring-boot:run
```

The application will be available at `http://localhost:8080`

## 📊 Credit Score Algorithm

The scoring algorithm simulates a complex credit assessment:

| Factor | Impact |
|--------|--------|
| Credit History ≥ 750 | +30 points |
| Credit History 650-749 | +20 points |
| Credit History 550-649 | +10 points |
| Credit History < 550 | -10 points |
| Criminal Record | -20 points |
| Market Conditions | -5 to +10 points (random) |

**Base Score**: 50 points  
**Final Range**: 1-100

### Risk Levels

| Score Range | Risk Level |
|-------------|------------|
| 80-100 | LOW_RISK |
| 60-79 | MEDIUM_RISK |
| 40-59 | HIGH_RISK |
| 1-39 | VERY_HIGH_RISK |

## 🏛️ Project Structure

```
global-credit-engine/
├── pom.xml                          # Maven build configuration
├── manifest.yml                     # Cloud Foundry deployment
├── README.md                        # This file
└── src/main/
    ├── java/com/tanzu/creditengine/
    │   ├── GlobalCreditEngineApplication.java
    │   ├── config/
    │   │   ├── GemFireConfig.java   # GemFire region setup
    │   │   └── RabbitMQConfig.java  # Queue configuration
    │   ├── entity/
    │   │   ├── UserFinancials.java  # JPA Entity (PostgreSQL)
    │   │   └── CreditScoreCache.java # GemFire Region model
    │   ├── repository/
    │   │   ├── UserFinancialsRepository.java
    │   │   └── CreditScoreCacheRepository.java
    │   ├── service/
    │   │   ├── CreditApplicationService.java
    │   │   ├── CreditScoreCalculator.java
    │   │   └── MetricsService.java  # In-memory metrics tracking
    │   ├── messaging/
    │   │   ├── CreditApplicationMessage.java
    │   │   └── CreditApplicationListener.java
    │   └── controller/
    │       └── CreditApplicationController.java
    └── resources/
        ├── application.yml          # Application configuration
        └── static/
            └── index.html           # Dashboard UI with AI Assistant
```


## 📝 License

This is a demonstration application for Tanzu Experience Day.

---

**Built with ❤️ for VMware Tanzu Platform**
