# Lootsy - E-commerce Application

Lootsy is a full-featured e-commerce web application built with Spring Boot, featuring user authentication, product catalog, shopping cart, and order management functionality.

## Features

- 🔐 User Authentication & Authorization (JWT-based)
- 🛍️ Product Catalog with Categories
- 🛒 Shopping Cart Management
- 📦 Order Processing & Management
- 🚚 Shipping Information
- 👤 User Account Management
- 🖼️ Product Image Management
- 📱 Responsive Web Interface

## Tech Stack

- **Backend**: Java 21, Spring Boot, Spring Security
- **Database**: PostgreSQL
- **Frontend**: Thymeleaf, HTML, CSS, JavaScript
- **Authentication**: JWT (JSON Web Tokens)
- **Containerization**: Docker & Docker Compose

## Prerequisites

Before running the application, make sure you have the following installed:

- [Docker](https://www.docker.com/get-started)
- [Docker Compose](https://docs.docker.com/compose/install/)

## Quick Start with Docker

### 1. Clone the Repository

```bash
By SSh... git clone git@github.com:ghhamo/lootsy.git
By HTTPS... git clone https://github.com/ghhamo/lootsy.git
cd lootsy
```

### 2. Build and Run with Docker Compose

Navigate to the `api` directory and run the following commands:

```bash
cd api
docker compose build
docker compose up -d
```

This will:
- Build the Spring Boot application
- Start a PostgreSQL database container
- Start the application container
- Set up all necessary networking between containers

### 3. Access the Application

Once the containers are running, open your web browser and navigate to:

**🌐 Application URL: [http://localhost:8080](http://localhost:8080)**

### 4. Login Information

The application will be seeded with initial data. You can:

1. **Register a new account** by clicking on the "Sign Up" link
2. **Login with existing credentials** (if any test users are seeded)

Navigate to the login page at: **[http://localhost:8080/login](http://localhost:8080/login)**

## Docker Services

The application consists of two main services:

### Database Service (`db`)
- **Image**: PostgreSQL 16
- **Container Name**: `lootsy-db`
- **Port**: 5433 (mapped from container port 5432)
- **Database**: `lootsy`
- **Username**: `name`
- **Password**: `password`

### Application Service (`app`)
- **Container Name**: `lootsy-app`
- **Port**: 8080
- **Depends on**: Database service (with health check)

## Useful Docker Commands

### View Running Containers
```bash
docker compose ps
```

### View Application Logs
```bash
docker compose logs app
```

### View Database Logs
```bash
docker compose logs db
```

### Stop the Application
```bash
docker compose down
```

### Stop and Remove All Data
```bash
docker compose down -v
```

### Rebuild and Restart
```bash
docker compose down
docker compose build --no-cache
docker compose up -d
```

## Application Structure

```
lootsy/
├── api/                          # Spring Boot application
│   ├── src/main/java/hamo/job/
│   │   ├── controller/           # REST controllers
│   │   ├── service/             # Business logic
│   │   ├── repository/          # Data access layer
│   │   ├── entity/              # JPA entities
│   │   ├── dto/                 # Data transfer objects
│   │   └── config/              # Configuration classes
│   ├── src/main/resources/
│   │   ├── templates/           # Thymeleaf templates
│   │   ├── static/              # CSS, JS, images
│   │   └── application.properties
│   ├── Dockerfile
│   └── docker-compose.yml
└── uploads/                     # Product images storage
```

## Development

### Running Locally (without Docker)

If you prefer to run the application locally for development:

1. Start PostgreSQL database
2. Update `application.properties` with your local database settings
3. Run the Spring Boot application:

```bash
cd api
./mvnw spring-boot:run
```

### Environment Variables

The Docker setup uses the following environment variables:

- `SPRING_DATASOURCE_URL`: Database connection URL
- `SPRING_DATASOURCE_USERNAME`: Database username
- `SPRING_DATASOURCE_PASSWORD`: Database password
- `IMAGE_FOLDER`: Directory for storing product images

## Troubleshooting

### Port Already in Use
If port 8080 or 5433 is already in use, you can modify the ports in `docker-compose.yml`:

```yaml
ports:
  - "8081:8080"  # Change 8080 to 8081 for the app
  - "5434:5432"  # Change 5433 to 5434 for the database
```

### Database Connection Issues
If the application can't connect to the database:

1. Check if both containers are running: `docker compose ps`
2. Check the database health: `docker compose logs db`
3. Restart the services: `docker compose restart`

### Application Not Loading
If the application doesn't load in the browser:

1. Check application logs: `docker compose logs app`
2. Verify the container is running: `docker compose ps`
3. Try accessing directly: `curl http://localhost:8080`

## Support

If you encounter any issues or have questions, please check the application logs using the Docker commands provided above.

---

**Happy Shopping with Lootsy! 🛍️**
