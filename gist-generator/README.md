## Gist Generator

A Spring Boot service that creates and renders lightweight gists, similar to GitHub Gists. Each gist accepts Markdown, converts it to safe HTML, and stores both the source and rendered output as JSON in S3. An S3-compatible endpoint is provided by LocalStack running in a Docker container, so the whole stack runs locally without an AWS account.

### Features
- REST endpoint to create gists from Markdown (`POST /gists`) with ULID-based IDs.
- View a gist as JSON (`/gists/{id}.json`) or as an HTML page (`/g/{id}`) via a Thymeleaf template.
- Persists gist JSON to S3 using the AWS SDK v2 client; the bucket is created automatically when missing.
- LocalStack (via `docker-compose.yml`) simulates AWS S3; `localstack-init-s3.sh` seeds the bucket on startup.

### Markdown & Sanitization dependencies
- `com.vladsch.flexmark:flexmark-all` — parses Markdown (with emoji support enabled) and renders HTML.
- `com.googlecode.owasp-java-html-sanitizer:owasp-java-html-sanitizer` — whitelists safe tags/attributes so untrusted Markdown cannot inject unsafe HTML.

### Getting started
1) Start LocalStack for S3: `docker-compose up -d` (creates bucket `my-bucket`).  
2) Run the app: `./mvnw spring-boot:run` (configured via `src/main/resources/application.yaml` to point the S3 client at LocalStack).  
3) Create a gist:  
   ```bash
   curl -X POST http://localhost:8080/gists \
     -H 'Content-Type: application/json' \
     -d '{"title":"Demo","language":"md","markdown":"# Hello"}'
   ```  
   Or use the provided `create-gist.sh`.
4) View it in the browser at `http://localhost:8080/g/{id}` or fetch JSON at `http://localhost:8080/gists/{id}.json`.

### Configuration
- Default S3 endpoint: `https://localhost.localstack.cloud:4566` (change in `application.yaml` if your LocalStack address differs).
- Requires Docker for LocalStack and Java 25+ to run the Spring Boot app (uses the Maven wrapper included here).
