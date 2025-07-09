# CloudDrive Microservices Architecture

## Architecture Overview

CloudDrive is a comprehensive cloud storage platform built with Spring Cloud Alibaba microservices architecture. The system consists of 9 core services that handle different aspects of file storage, user management, and system administration.

## Service Architecture

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Frontend      │    │   API Gateway    │    │   Nacos         │
│   (Vue 3)       │────│   (8080)         │────│   (8848)        │
│   Port: 3000    │    │                  │    │   Service       │
└─────────────────┘    └──────────────────┘    │   Registry      │
                                               └─────────────────┘
                              │
              ┌───────────────┼───────────────┐
              │               │               │
    ┌─────────▼──────┐ ┌─────▼─────┐ ┌──────▼──────┐
    │ Auth Service   │ │File Upload│ │Admin Service│
    │ (8081)         │ │Down Service│ │ (8083)      │
    │ JWT/Login      │ │ (8082)     │ │ Management  │
    └────────────────┘ └───────────┘ └─────────────┘
              │               │               │
    ┌─────────▼──────┐ ┌─────▼─────┐ ┌──────▼──────┐
    │Membership Svc  │ │File Manage│ │File Recycle │
    │ (8084)         │ │Service     │ │Server       │
    │ Subscriptions  │ │ (8085)     │ │ (8086)      │
    └────────────────┘ └───────────┘ └─────────────┘
              │
    ┌─────────▼──────┐
    │ Mail Service   │
    │ (8087)         │
    │ Email/Notify   │
    └────────────────┘
              │
    ┌─────────▼──────┐    ┌──────────────┐    ┌─────────────┐
    │    MySQL       │    │    Redis     │    │  RabbitMQ   │
    │   (3307)       │    │   (6379)     │    │  (Message   │
    │   Database     │    │   Cache      │    │   Queue)    │
    └────────────────┘    └──────────────┘    └─────────────┘
```

## Service Details

### 1. Frontend (Port 3000)
- **Technology**: Vue 3 + TypeScript + Vite
- **Purpose**: User interface for file management and system interaction
- **Features**: 
  - File upload/download interface
  - User authentication and registration
  - Admin dashboard
  - Membership management
  - File sharing and recycling
- **Dependencies**: Communicates with all backend services through Gateway

### 2. API Gateway (Port 8080)
- **Technology**: Spring Cloud Gateway
- **Purpose**: Single entry point for all API requests, routing and load balancing
- **Features**:
  - Request routing to microservices
  - CORS handling
  - Authentication filtering
- **Dependencies**: Nacos for service discovery

### 3. Auth Service (Port 8081)
- **Technology**: Spring Boot + Spring Security + JWT
- **Purpose**: User authentication and authorization
- **Features**:
  - User login/logout
  - JWT token generation and validation
  - User registration
  - Token verification endpoints
- **Dependencies**: MySQL, Redis, Nacos
- **Key Classes**:
  - `AuthController` - Login/logout endpoints
  - `RegisterController` - User registration
  - `TokenVerifyController` - Token validation
  - `JwtUtil` - JWT token utilities

### 4. File Upload/Download Service (Port 8082)
- **Technology**: Spring Boot + MyBatis Plus
- **Purpose**: Core file operations and storage management
- **Features**:
  - File upload with chunked support
  - File download and streaming
  - Image processing and thumbnails
  - File validation and security
  - User space quota management
- **Dependencies**: MySQL, Redis, Nacos
- **Key Classes**:
  - `FileInfoController` - File operations API
  - `CommonFileController` - Common file utilities
  - `FileInfoService` - File business logic
  - `FileCleanTask` - Automated file cleanup

### 5. Admin Service (Port 8083)
- **Technology**: Spring Boot + Spring Security
- **Purpose**: System administration and management
- **Features**:
  - User management for administrators
  - System monitoring and statistics
  - Cross-service data management
- **Dependencies**: MySQL, Redis, Nacos
- **Key Classes**:
  - `UserController` - Admin user management
  - `SecurityConfig` - Admin security configuration

### 6. Membership Service (Port 8084)
- **Technology**: Spring Boot + MyBatis Plus
- **Purpose**: Subscription and membership management
- **Features**:
  - Membership level management
  - Payment processing
  - Subscription lifecycle management
  - Storage quota upgrades
- **Dependencies**: MySQL, Redis, Nacos
- **Key Classes**:
  - `MembershipController` - Membership plans
  - `PaymentController` - Payment processing
  - `SubscriptionController` - User subscriptions

### 7. File Management Service (Port 8085)
- **Technology**: Spring Boot
- **Purpose**: File metadata and organization management
- **Features**:
  - File information management
  - Directory structure handling
  - File categorization
- **Dependencies**: MySQL, Nacos
- **Key Classes**:
  - `FileInfo` - File metadata entity
  - `DateUtil` - Date utilities

### 8. File Recycle Service (Port 8086)
- **Technology**: Spring Boot
- **Purpose**: File deletion and recovery management
- **Features**:
  - Soft delete functionality
  - File recovery from recycle bin
  - Automated cleanup of expired files
- **Dependencies**: MySQL, Nacos

### 9. Mail Service (Port 8087)
- **Technology**: Spring Boot + RabbitMQ
- **Purpose**: Email notifications and messaging
- **Features**:
  - Email verification codes
  - System notifications
  - Asynchronous message processing
  - Template-based emails
- **Dependencies**: RabbitMQ, Redis, Nacos
- **Key Classes**:
  - `MailController` - Email API endpoints
  - `MailProducerService` - Message publishing
  - `MailConsumerService` - Message consumption
  - `RabbitMQConfig` - Message queue configuration

## Infrastructure Services

### Nacos (Port 8848)
- **Purpose**: Service discovery, configuration management
- **Features**: Service registration, health checks, dynamic configuration

### MySQL (Port 3307)
- **Purpose**: Primary data storage
- **Database**: `cloud_drive`
- **Tables**: users, files, file_shares, membership_levels, user_subscriptions

### Redis (Port 6379)
- **Purpose**: Caching and session storage
- **Usage**: JWT tokens, session data, file upload progress

### RabbitMQ
- **Purpose**: Asynchronous message processing
- **Usage**: Email notifications, system events

## Service Communication Patterns

### Synchronous Communication
- **Frontend ↔ Gateway**: HTTP/HTTPS requests
- **Gateway ↔ Services**: HTTP with load balancing
- **Service ↔ Nacos**: Service registration and discovery

### Asynchronous Communication
- **Mail Service**: RabbitMQ message queues for email processing
- **File Operations**: Event-driven file processing

### Data Access Patterns
- **Direct Database Access**: Each service manages its own data domain
- **Shared Cache**: Redis for cross-service session and temporary data
- **Service Discovery**: Nacos for dynamic service location

## Port Assignments

| Service | Port | Protocol | Purpose |
|---------|------|----------|---------|
| Frontend | 3000 | HTTP | User Interface |
| Gateway | 8080 | HTTP | API Gateway |
| Auth Service | 8081 | HTTP | Authentication |
| File Upload/Download | 8090 | HTTP | File Operations |
| Admin Service | 8083 | HTTP | Administration |
| Membership Service | 8084 | HTTP | Subscriptions |
| File Management | 8099 | HTTP | File Metadata |
| File Recycle | 8086 | HTTP | File Recovery (Port 8080→8086) |
| Mail Service | 8087 | HTTP | Email Service (Port 8084→8087) |
| Nacos | 8848 | HTTP | Service Registry |
| MySQL | 3307 | TCP | Database |
| Redis | 6379 | TCP | Cache |

## Security Architecture

### Authentication Flow
1. User logs in through Frontend
2. Auth Service validates credentials
3. JWT token generated and stored in Redis
4. Frontend includes token in subsequent requests
5. Gateway validates token with Auth Service

### Authorization
- **JWT-based**: Stateless authentication using JSON Web Tokens
- **Role-based**: Different access levels for users and administrators
- **Service-to-service**: Internal authentication between microservices

## Data Architecture

### Database Design
- **User Management**: Users, authentication data
- **File System**: File metadata, sharing, recycling
- **Membership**: Subscription plans, payment records
- **System**: Audit logs, configuration data

### File Storage Strategy
- **Metadata**: Stored in MySQL database
- **File Content**: Local file system with chunked upload support
- **Thumbnails**: Generated and cached for images
- **Backup**: Configurable cleanup and archival

## Deployment Architecture

### Development Environment
- **Local Services**: Individual Spring Boot applications
- **Infrastructure**: Docker containers for MySQL, Redis, Nacos
- **Frontend**: Vite dev server with proxy to Gateway

### Production Environment
- **Containerization**: All services deployed as Docker containers
- **Service Mesh**: Docker Compose networking
- **Load Balancing**: Built into Spring Cloud Gateway
- **Health Monitoring**: Nacos health checks

## Development Workflow

### Service Dependencies
1. **Infrastructure First**: MySQL, Redis, Nacos
2. **Core Services**: Gateway, Auth Service
3. **Business Services**: File services, Membership, Admin
4. **Supporting Services**: Mail Service
5. **Frontend**: Vue 3 application

### API Standards
- **RESTful APIs**: Standard HTTP methods and status codes
- **JSON Communication**: Request/response format
- **Error Handling**: Consistent error response structure
- **Documentation**: OpenAPI/Swagger for each service

## Scalability Considerations

### Horizontal Scaling
- **Stateless Services**: All services designed for multiple instances
- **Load Balancing**: Gateway distributes requests
- **Database**: Read replicas for query scaling

### Performance Optimization
- **Caching**: Redis for frequently accessed data
- **Async Processing**: RabbitMQ for non-blocking operations
- **File Chunking**: Large file upload optimization

## Monitoring and Observability

### Health Checks
- **Service Health**: Nacos monitors service availability
- **Database Health**: Connection pool monitoring
- **Cache Health**: Redis connectivity checks

### Logging
- **Centralized Logging**: Logback configuration
- **Service Correlation**: Request tracing across services
- **Error Tracking**: Exception logging and alerting

This architecture provides a robust, scalable foundation for CloudDrive's file storage and management capabilities while maintaining clear separation of concerns across services.