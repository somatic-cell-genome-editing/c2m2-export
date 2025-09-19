# Running C2M2 Generator with Your PostgreSQL Database

This guide shows you how to connect the C2M2 Submission Generator to your PostgreSQL database and generate submission packages.

## Quick Start

### Option 1: Using the Setup Script (Recommended)

```bash
cd /Users/jdepons/claude/c2m2-submission-generator
./run-c2m2-generator.sh
```

The script will prompt you for your database connection details and run the generator.

### Option 2: Using Environment Variables

```bash
# Set your database credentials
export DB_USERNAME="your_username"
export DB_PASSWORD="your_password"

# Update the database configuration in src/main/resources/application.yml
# Then run:
./gradlew bootRun
```

### Option 3: Direct Configuration

1. Edit `src/main/resources/application.yml`
2. Update the database section with your connection details:

```yaml
database:
  host: your_database_host      # e.g., localhost
  port: 5432                    # your PostgreSQL port
  name: your_database_name      # e.g., scge_production
  username: your_username
  password: your_password
```

3. Run the application:
```bash
./gradlew bootRun
```

## Database Requirements

### Required Tables

The generator expects these tables in your PostgreSQL database:

- **`study`** - Research studies/projects
  - `study_id` (BIGINT, Primary Key)
  - `name` (VARCHAR)
  - `description` (TEXT)
  - `study_type` (VARCHAR)

- **`person`** - People involved in research
  - `person_id` (BIGINT, Primary Key)
  - `first_name` (VARCHAR)
  - `last_name` (VARCHAR)
  - `email` (VARCHAR)
  - `organization` (VARCHAR)

- **`model`** - Research models/organisms
  - `model_id` (BIGINT, Primary Key)
  - `name` (VARCHAR)
  - `description` (TEXT)
  - `model_type` (VARCHAR)
  - `species` (VARCHAR)

- **`experiment`** - Experimental records
  - `experiment_id` (BIGINT, Primary Key)
  - `name` (VARCHAR)
  - `description` (TEXT)
  - `experiment_type` (VARCHAR)

### Table Customization

If your tables have different names or columns, you can modify the SQL queries in:
`src/main/java/org/scge/c2m2/C2M2SubmissionGeneratorApplication.java`

Look for the `extractAndMapData()` method and update the queries:

```java
// Example: If your table is named 'studies' instead of 'study'
List<Study> studies = databaseService.executeQuery(
    "SELECT * FROM studies ORDER BY id",  // Changed table name and column
    rs -> Study.builder()
        .studyId(rs.getLong("id"))        // Changed column name
        .name(rs.getString("title"))      // Changed column name
        .description(rs.getString("description"))
        .studyType(rs.getString("type"))  // Changed column name
        .build()
);
```

## What the Generator Does

1. **Connects to Database** - Establishes connection using HikariCP pooling
2. **Extracts Data** - Runs SQL queries to get SCGE entities
3. **Maps to C2M2** - Converts SCGE data to C2M2-compliant format
4. **Generates TSV Files** - Creates tab-separated files for each C2M2 table
5. **Creates Package** - Builds complete submission with manifest and README
6. **Validation** - Checks data quality and package completeness
7. **Archives** - Produces a ZIP file ready for submission

## Output Structure

After running, check the `c2m2-output` directory:

```
c2m2-output/
├── packages/
│   ├── scge-submission-YYYYMMDD-HHMMSS/
│   │   ├── manifest.json           # Package metadata
│   │   ├── README.md              # Package documentation
│   │   ├── data/
│   │   │   ├── project.tsv        # C2M2 projects
│   │   │   ├── subject.tsv        # C2M2 subjects
│   │   │   └── biosample.tsv      # C2M2 biosamples
│   │   ├── associations/
│   │   │   └── subject_in_project.tsv
│   │   └── validation/
│   └── scge-submission-YYYYMMDD-HHMMSS.zip  # Ready for submission
└── submission-YYYYMMDD-HHMMSS/
    └── tsv-files/                 # Individual TSV files
```

## Database Connection Testing

To test your database connection without running the full generator:

```bash
# Test basic connectivity
psql -h your_host -p 5432 -U your_username -d your_database -c "SELECT 1;"

# Check if required tables exist
psql -h your_host -p 5432 -U your_username -d your_database -c "\\dt"

# Count records in key tables
psql -h your_host -p 5432 -U your_username -d your_database -c "
SELECT 
  'study' as table_name, COUNT(*) as record_count FROM study
UNION ALL SELECT 
  'person', COUNT(*) FROM person
UNION ALL SELECT 
  'model', COUNT(*) FROM model
UNION ALL SELECT 
  'experiment', COUNT(*) FROM experiment;"
```

## Troubleshooting

### Connection Issues

1. **"Connection refused"**
   - Check if PostgreSQL is running
   - Verify host and port settings
   - Check firewall rules

2. **"Authentication failed"**
   - Verify username and password
   - Check PostgreSQL user permissions
   - Ensure user has SELECT access to required tables

3. **"Database does not exist"**
   - Verify database name
   - Check if you have access to the database

### Missing Tables

If tables don't exist, the application will show specific error messages. Update the SQL queries in the application code to match your schema.

### Performance Tuning

For large databases, you can:

1. **Adjust batch sizes** in `application.yml`:
```yaml
c2m2:
  generation:
    batch-size: 5000
    max-entities-per-table: 100000
```

2. **Add LIMIT clauses** to queries for testing:
```java
"SELECT * FROM study ORDER BY study_id LIMIT 100"
```

3. **Increase connection pool size**:
```yaml
database:
  pool:
    maximum-pool-size: 20
```

## Support

The generator includes comprehensive logging. Check the console output for detailed information about:
- Database connection status
- Number of records extracted from each table
- Mapping results and any issues
- Package creation progress
- Final output locations

For issues, examine the logs and verify your database schema matches the expected structure.