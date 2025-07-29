# Database Schema SQL Files

This directory contains all SQL files for creating and managing the PostgreSQL JDBC Client database schema.

## 📁 File Structure

- **`00_create_schema.sql`** - Main orchestration script that executes all others
- **`01_create_sequences.sql`** - Creates 20 auto-incrementing sequences
- **`02_create_tables.sql`** - Creates all 20 business tables with relationships
- **`03_create_indexes.sql`** - Creates 100+ performance indexes
- **`04_sample_data.sql`** - Inserts realistic sample data for testing
- **`05_migration_v1_0_1.sql`** - Example migration script
- **`99_drop_schema.sql`** - Complete schema cleanup script

## 🚀 Usage

### Execute Complete Schema
```bash
psql -h localhost -U postgres -d testdb -f 00_create_schema.sql
```

### Execute Individual Files
```bash
psql -h localhost -U postgres -d testdb -f 01_create_sequences.sql
psql -h localhost -U postgres -d testdb -f 02_create_tables.sql
psql -h localhost -U postgres -d testdb -f 03_create_indexes.sql
```

### Using the Application
The Spring Boot application automatically executes `00_create_schema.sql` on startup when:
```properties
app.schema.auto-create=true
```

## 🗄️ Database Objects Created

### 20 Sequences
- Auto-incrementing primary keys for all tables
- Optimized cache sizes for performance
- Starting values tailored to business needs

### 20 Business Tables
1. **users** - User management and authentication
2. **categories** - Product categorization (hierarchical)
3. **suppliers** - Supplier relationship management
4. **products** - Product catalog with rich metadata
5. **customers** - Customer information and segmentation
6. **orders** - Order management with full lifecycle
7. **departments** - Organizational structure
8. **employees** - HR and employee management
9. **projects** - Project management
10. **tasks** - Task tracking and assignment
11. **invoices** - Financial invoice management
12. **payments** - Payment processing
13. **inventory** - Stock management
14. **shipments** - Shipping and logistics
15. **reviews** - Product reviews and ratings
16. **promotions** - Marketing campaigns
17. **addresses** - Generic address management
18. **contacts** - Multi-entity contact information
19. **documents** - Document management
20. **logs** - System and audit logging

### 100+ Indexes
- Primary key indexes (automatic)
- Foreign key indexes for relationships
- Search indexes for text fields
- Composite indexes for complex queries
- Partial indexes for conditional optimization
- GIN indexes for array and JSON fields
- Full-text search indexes

## 🔧 Customization

### Adding New Tables
1. Add sequence in `01_create_sequences.sql`
2. Add table definition in `02_create_tables.sql`
3. Add indexes in `03_create_indexes.sql`
4. Add sample data in `04_sample_data.sql`
5. Update cleanup in `99_drop_schema.sql`

### Modifying Existing Tables
1. Create migration script (e.g., `06_your_migration.sql`)
2. Follow the pattern in `05_migration_v1_0_1.sql`
3. Test thoroughly before production deployment

### Performance Tuning
- Monitor index usage with `pg_stat_user_indexes`
- Add custom indexes based on query patterns
- Adjust sequence cache sizes for high-volume tables

## 📊 Monitoring

### Schema Information
All schema operations are logged in the `schema_info` table:
```sql
SELECT * FROM schema_info ORDER BY executed_at DESC;
```

### Object Counts
```sql
SELECT 
    'Tables' as type, COUNT(*) as count 
FROM information_schema.tables 
WHERE table_schema = 'public'
UNION ALL
SELECT 
    'Sequences', COUNT(*) 
FROM information_schema.sequences 
WHERE sequence_schema = 'public';
```

### Index Usage
```sql
SELECT 
    indexrelname as index_name,
    relname as table_name,
    idx_scan as times_used
FROM pg_stat_user_indexes 
ORDER BY idx_scan DESC;
```

## ⚠️ Important Notes

- Always backup before running schema changes
- Test migrations in development environment first
- Monitor performance after adding new indexes
- Review and update sample data as schema evolves
- Use `99_drop_schema.sql` with extreme caution - it deletes everything!

## 🔗 Related Documentation

See the main README.md and SQL_SCHEMA_GUIDE.md for comprehensive documentation on using and managing the database schema.