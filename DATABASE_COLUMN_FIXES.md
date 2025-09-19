# Database Column Fixes for C2M2 Generator

## Summary
The SCGE C2M2 generator was failing due to incorrect column names being used in SQL queries. This document details the issues found and fixes applied.

## Issues Found and Fixed

### 1. Tissue Table Issue
**Problem:** Query was trying to access a non-existent 'description' column in the tissue table.

**Original Query:**
```sql
SELECT tissue_id, tissue, description FROM tissue ORDER BY tissue_id
```

**Actual Tissue Table Structure:**
```
tissue_id            (int4)    - Primary key
tissue_system        (varchar) - Body system (e.g., "Reproductive System")
parent_tissue_term   (varchar) - Parent tissue (e.g., "Ovary", "Kidney")
tissue_term          (varchar) - Specific tissue (e.g., "general", "Oocyte")
```

**Fixed Query:**
```sql
SELECT tissue_id, tissue_system, parent_tissue_term, tissue_term FROM tissue ORDER BY tissue_id
```

**Code Changes:**
- Updated query in `generateBiosampleFile()` method
- Added `buildTissueName()` helper method to create meaningful names
- Added `buildTissueDescription()` helper method to create descriptions from available columns

### 2. Images Table Issue
**Problem:** Query was trying to access non-existent 'image_id' and 'filename' columns in the images table.

**Original Query:**
```sql
SELECT image_id, filename, mime_type, file_size FROM images ORDER BY image_id
```

**Actual Images Table Structure:**
```
scge_id              (int8)    - Primary key
file_name            (varchar) - Filename
file_type            (varchar) - File type/MIME type
title                (varchar) - Image title
image                (bytea)   - Binary image data
bucket               (varchar) - Storage bucket
legend               (varchar) - Image legend/caption
pos_index            (int8)    - Position index
thumbnail            (bytea)   - Thumbnail image data
image_700_wide       (bytea)   - Resized image data
```

**Fixed Query:**
```sql
SELECT scge_id, file_name, file_type, title FROM images ORDER BY scge_id
```

**Code Changes:**
- Updated query in `generateFileFile()` method
- Updated references in `generateFileAssociations()` method
- Added `buildImageName()` helper method to create names from title or filename
- Added `determineFileTypeFromName()` helper method to determine MIME types from extensions

### 3. Delivery System Table Issue
**Problem:** Query was trying to access non-existent 'delivery_system_id', 'name', 'type', 'description' columns.

**Original Query:**
```sql
SELECT delivery_system_id, name, type, description FROM delivery_system ORDER BY delivery_system_id
```

**Actual Delivery System Table Structure:**
```
ds_id                (int8)    - Primary key
ds_name              (varchar) - Delivery system name
ds_type              (varchar) - Delivery system type
ds_description       (varchar) - Description
ds_subtype           (varchar) - Subtype
ds_source            (varchar) - Source
(+ additional ds_* columns)
```

**Fixed Query:**
```sql
SELECT ds_id, ds_name, ds_type, ds_description FROM delivery_system ORDER BY ds_id
```

## Final Results
After applying all fixes, the generator successfully completed with:
- 95 project records
- 82 subject records
- 3,797 biosample records (including 50 tissue records)
- 1,102 file records (image files)
- 436 collection records
- All association files generated successfully

## Sample Tissue Records Generated
```tsv
scge.org	tissue-41			Skin	Tissue from Integumentary system, specifically from Skin	
scge.org	tissue-42			Skin - Dermis	Tissue from Integumentary system, specifically from Skin (Dermis)	
scge.org	tissue-43			Skin - Epidermis	Tissue from Integumentary system, specifically from Skin (Epidermis)	
scge.org	tissue-44			Skin - Hair follicle	Tissue from Integumentary system, specifically from Skin (Hair follicle)	
scge.org	tissue-48			Brain	Tissue from Nervous system, specifically from Brain	
```

## Sample Image Records Generated
```tsv
scge.org	image-1025	Characterization of NP formulations	null	image				Saltzman NP characterizations 2.PNG
scge.org	image-1033	Conkin_Fast-Seq_AAV_Page_01.png	null	image				Conkin_Fast-Seq_AAV_Page_01.png
```

## Files Modified
- `/src/main/java/org/scge/c2m2/SimpleC2M2Generator.java`
  - Fixed tissue table query and added helper methods
  - Fixed images table query and added helper methods
  - Fixed delivery_system table query

## Helper Methods Added
1. `buildTissueName()` - Creates meaningful tissue names from tissue_term and parent_tissue_term
2. `buildTissueDescription()` - Creates descriptions from tissue_system, parent_tissue_term, and tissue_term
3. `buildImageName()` - Creates image names preferring title over filename
4. `determineFileTypeFromName()` - Determines MIME types from file extensions

The generator now correctly maps the actual SCGE database schema to C2M2 format without column access errors.