# Retail Discount System

A Spring Boot application that calculates and applies various discount rules for customer bills with JWT-based authentication and comprehensive discount logic.

## UML class diagram : 

![alt text](https://github.com/AbdulazizQ3382/retailDiscount/blob/master/src/main/resources/static/retail-class-diagram.png)

## Project Requirements

### System Requirements
- **Java**: 21 or higher
- **Maven**: 3.6+ 
- **Docker**: For running dependencies (MongoDB, SonarQube)
- **Docker Compose**: For orchestrating services

### Key Dependencies
- Spring Boot 3.5.5
- Spring Data MongoDB
- Spring Security with JWT
- MapStruct 1.6.3
- Lombok
- SpringDoc OpenAPI (Swagger)
- JaCoCo (Code Coverage)
- Mockito (Testing)

## Getting Started

### 1. Clone the Repository
```bash
git clone <repository-url>
cd retailDiscount
```

### 2. Start Dependencies with Docker Compose
```bash
# Start MongoDB, Mongo Express, SonarQube, and PostgreSQL
docker-compose -f docker-compose.dev.yaml up -d
```

This will start:
- **MongoDB** on port `27017` (root/password)
- **Mongo Express** on port `8081` (mongoexpressuser/mongoexpresspass)
- **SonarQube** on port `9000` 
- **PostgreSQL** on port `5432` (for SonarQube)

### 3. Build and Run the Application
```bash
# Build the project
mvn clean compile

# Run the application
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## Testing

### Run Tests
```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=BillServiceTest

# Run tests in specific package
mvn test -Dtest="sa.store.retaildiscount.service.*Test"
```

### Code Coverage with JaCoCo
```bash
# Run tests with coverage report
mvn clean test jacoco:report
```

Coverage reports will be generated in `target/site/jacoco/index.html`

**Coverage Exclusions:**
- Configuration classes (`**/*Config.*`)
- Aspect classes (`**/*Aspect.*`)
- Exception handlers (`**/*ExceptionHandler.*`)
- Mappers (`**/*Mapper.*`, `**/*MapperImpl.*`)
- JWT utilities (`**/Jwt**`)

## Code Quality with SonarQube

### Prerequisites
Make sure SonarQube is running via Docker Compose (see step 2 above).

### Run SonarQube Analysis
```bash
# Analyze code quality
mvn verify sonar:sonar
```

Access SonarQube dashboard at `http://localhost:9000`

**SonarQube Configuration:**
- Project Key: `retialDiscount`
- Coverage exclusions: config, aspect, mapper, exception packages

## API Documentation

### Swagger UI
- **URL**: `http://localhost:8080/swagger-ui.html`
- **Root redirect**: `http://localhost:8080/` redirects to Swagger UI

### Main Endpoints

#### Authentication
- `POST /auth/login` - JWT authentication

#### Bill Management
- `POST /api/bills` - Calculate discount and save bill
- `GET /api/bills/{billId}` - Retrieve bill by ID
- `GET /api/bills/customer/{customerId}` - Get customer bills

## Monitoring

### Spring Boot Actuator
- **Base URL**: `http://localhost:8080/actuator`
- **Health**: `/actuator/health` - Application health status
- **Info**: `/actuator/info` - Application information  
- **Metrics**: `/actuator/metrics` - Application metrics

## Discount Business Rules

The system implements the following discount logic:

1. **$5 for every $100**: Applied to bills over $100 (floor division)
2. **Customer Type Discounts**:
   - **Employee**: 30% discount
   - **Affiliate**: 10% discount  
   - **Long-term Customer**: 5% discount (registered 2+ years ago)
3. **Special Rule**: Bills under $100 only receive customer type discounts

## Environment Configuration

Key environment variables with defaults:

```properties
MONGODB_URI=mongodb://root:password@localhost:27017/retailDiscount?authSource=admin
SERVER_PORT=8080
JWT_EXPIRATION=7200000  # 2 hours in milliseconds
MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info,metrics
LOGGING_LEVEL_SA_STORE_RETAILDISCOUNT=INFO
```

## Database Access

### MongoDB
- **Connection**: `mongodb://root:password@localhost:27017/retailDiscount?authSource=admin`
- **Database**: `retailDiscount`

### Mongo Express (Web UI)
- **URL**: `http://localhost:8081`
- **Username**: `mongoexpressuser`
- **Password**: `mongoexpresspass`

## Development Commands

```bash
# Clean and build
mvn clean package

# Skip tests during build
mvn clean package -DskipTests

# Generate sources (MapStruct)
mvn generate-sources
```

## Project Structure

```
src/main/java/sa/store/retaildiscount/
   controller/          # REST API endpoints
   service/            # Business logic
   entity/             # MongoDB entities  
   dto/                # Data transfer objects
   repository/         # Data access layer
   config/             # Configuration classes
   mapper/             # MapStruct mappers
   utils/              # Utility classes
   aspect/             # AOP aspects
   exception/          # Exception handling
```