# MangaWatch:Backend

Containerized Spring Boot REST API for manga tracking and library management.

## Tech Stack

- **Java 17** (Eclipse Temurin)
- **Spring Boot 3.x** with Spring Data JPA
- **PostgreSQL 16**
- **Docker** (multi-stage build with Maven + JRE)
- **Flyway** for database migrations
- **JWT** authentication

## Prerequisites

- Docker Desktop
- MangaDex API credentials (optional - only needed for importing manga data)

## Quick Start (Docker Desktop recommended for easier access)

### 1. Start PostgreSQL Container
```bash
docker run --name mg-postgres \
  -e POSTGRES_USER=mgusr01 \
  -e POSTGRES_PASSWORD=password \
  -e POSTGRES_DB=mgdbpg \
  -p 5432:5432 \
  -d postgres:16
```

### 2. Build Backend Image
```bash
cd backend
docker build -t mangawatch-backend .
```

### 3. Run Backend Container
```bash
docker run --name manga-backend \
  -p 8080:8080 \
  -e DB_HOST=host.docker.internal \
  -e DB_PORT=5432 \
  -e DB_NAME=mgdbpg \
  -e DB_USER=mgusr01 \
  -e DB_PASS=password \
  -e JWT_SECRET=your-secret-key-minimum-32-characters-long \
  -d mangawatch-backend
```

**Note:** `host.docker.internal` allows the backend container to reach PostgreSQL on your host machine. On Linux, use `--network host` or create a Docker network instead.

### 4. Verify It's Running
```bash
# Check container logs
docker logs manga-backend

# Test the API
curl/postman http://localhost:8080/api/manga/stats
```

---

## Environment Variables

### Required Variables
```bash
DB_HOST=host.docker.internal    # PostgreSQL host
DB_PORT=5432                     # PostgreSQL port
DB_NAME=mangadbpg                # Database name
DB_USER=mangauser01              # Database user
DB_PASS=secretPW                 # Database password
JWT_SECRET=your-secret-here      # JWT signing key (32+ chars)
```

### Optional Variables (for importing manga data)
```bash
MANGADEX_CLIENT_ID=your-client-id
MANGADEX_CLIENT_SECRET=your-client-secret
MANGADEX_USERNAME=your-username
MANGADEX_PASSWORD=your-password
```

**To get MangaDex credentials:** Create a personal client at https://api.mangadex.org/docs/02-authentication/personal-clients/

### Setting Environment Variables

**Option 1: Pass directly to `docker run`** (shown in Quick Start)

**Option 2: IDE Run Configuration** (IntelliJ/Eclipse environment variables)

## Using the Mangadex API

The import system fetches manga from MangaDex in batches, storing them in your local database.

- First, create a Mangadex account and log into it
- Then, go to this url: https://api.mangadex.org/docs/02-authentication/personal-clients/.
- Read it through, it will explain how to create a personal client and use the API. You may or may not need it but its good to have.
- After creating a personal client, note down and add these to the environment variables:-

```
Variables :  Values
MANGADEX_CLIENT_ID : personal-client-8fn736h-6767-931o-g874-kn48ira021s109-xyzxyz
MANGADEX_CLIENT_SECRET : E3knHpCViDJZ84UPXN6IlvsdcRm48x4XF
MANGADEX_PASSWORD : yourpassword
MANGADEX_USERNAME : yourusername
```

(use a unique secret, client id, username and password)

### Prerequisites
- MangaDex API credentials set in environment variables
- Backend and database running
- Stable internet connection

### Import Process

**1. Start import:**
```bash
curl -X POST http://localhost:8080/admin/import/start
```

**2. Check status:**
```bash
curl http://localhost:8080/admin/import/status
```
Response shows:
- Total entries imported
- Last processed timestamp (cursor)
- Import state

**3. Resume import (if needed):**

The import automatically stops after each batch (~9,000-10,000 entries due to MangaDex API limits).
```bash
# Get cursor from status endpoint, then:
curl -X POST "http://localhost:8080/admin/import/resume?cursor=2024-01-15T10:30:00"
```

**4. Repeat until desired count reached**

To reach 87,000+ entries, you'll need to resume ~9 times.

**Note:** The `GET ${API_BASE}/admin/import/status` endpoint will tell you the last import's timestamp and number of entries the db has.

### Import Configuration

Defined in `application.properties`:
```properties
mangadex.import.batch-size=100       # Manga per API request
mangadex.import.rate-limit-ms=250    # Delay between requests
mangadex.import.max-retries=3        # Retry failed requests
```

---
## Deployment

### Testing Dockerfile Locally
```bash
docker build -t mangawatch-backend .
docker run -p 8080:8080 [environment variables] mangawatch-backend
```

### On Render

The dockerfile is used to deploy on Render, link to the deployed version with ~9000 entries on the repo and root readme.

## Architecture & Design Notes

### Multi-Stage Docker Build
- **Stage 1:** Maven build with full JDK (larger image)
- **Stage 2:** Runtime with JRE only (smaller, faster image)
- **Result:** ~60% reduction in final image size

### Database Migrations
- **Flyway** handles schema versioning
- Migrations run automatically on startup
- Baseline migration for existing databases

### Performance Optimizations
- N+1 query prevention with JOIN FETCH
- Database indexes on frequently queried fields
- Connection pooling for database access

### Security
- JWT token-based authentication
- Password hashing with BCrypt
- CORS configuration for frontend
- Input validation and sanitization

---

## Troubleshooting

### Can't connect to database
**If using individual containers:**
- Backend must use `DB_HOST=host.docker.internal`
- Or create Docker network: `docker network create manga-net`

### Import fails after ~9K entries
This is expected! MangaDex API limits batch size. Use the resume endpoint.

### JWT token errors
- Ensure `JWT_SECRET` is at least 32 characters (ALWAYS SIGN JWTS)
- Token expires after 1 hour (configurable in application.properties)

### Port already in use
```bash
# Kill process on port 8080
lsof -ti:8080 | xargs kill -9

# Or use different port
docker run -p 9090:8080 ...
```

## Design/Dev Notes

### Java 17 instead of 21

Switched from Java 21 to Java 17 for deployment compatibility with Render's Docker environment at the time of initial deployment (late 2025). The application can likely be upgraded to Java 21 in the future, but Java 17 provides stable compatibility with all current dependencies and deployment infrastructure.

### Endpoints for future use
- `/api/auth/me` - Planned for profile management
- `/api/manga/dex/{dexId}` - Direct MangaDex ID lookup (internal use)
