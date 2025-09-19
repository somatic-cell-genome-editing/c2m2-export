# C2M2 Submission Generator

A Java application that extracts data from the SCGE PostgreSQL database and generates a C2M2-compliant submission package for the Common Fund Data Ecosystem (CFDE).

## Prerequisites

- Java 17 or higher
- Gradle 8.0 or higher
- PostgreSQL database access
- Environment variables for database credentials

## Quick Start

1. Clone the repository:
   ```bash
   git clone https://github.com/your-org/c2m2-submission-generator.git
   cd c2m2-submission-generator
   ```

2. Configure database connection:
   - Copy `src/main/resources/application.yml.example` to `application.yml`
   - Copy `.env.example` to `.env`
   - Update with your database credentials

3. Set environment variables:
   ```bash
   export DB_URL=jdbc:postgresql://your-host:5432/your-database
   export DB_USERNAME=your_username
   export DB_PASSWORD=your_password
   ```

4. Build the project:
   ```bash
   ./gradlew build
   ```

5. Run the application:
   ```bash
   ./gradlew bootRun
   ```

   Or run the simple generator:
   ```bash
   ./gradlew run --args="SimpleC2M2Generator"
   ```

## Configuration

The application uses Spring Boot's configuration system with YAML files. See `src/main/resources/application.yml` for configuration options.

## Development

### Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── org/scge/c2m2/
│   └── resources/
│       ├── application.yml
│       └── logback-spring.xml
└── test/
    ├── java/
    └── resources/
```

### Testing

Run tests with:
```bash
./gradlew test
```

## Security Notes

- Never commit database credentials to version control
- Use environment variables or secure credential management systems
- The `application.yml` and `.env` files with actual credentials should never be committed
- Only commit `.example` versions of configuration files

## Output

The application generates:
- C2M2-compliant TSV files for all entities
- Association files linking entities
- A manifest.json with metadata
- A ZIP archive ready for submission to CFDE

## Database Schema

The application maps SCGE database tables to C2M2 entities:
- `study` → `project.tsv`
- `experiment` → `project.tsv` (as sub-projects)
- `model` → `subject.tsv`
- `experiment_record` → `biosample.tsv`

## License

[Add your license information here]