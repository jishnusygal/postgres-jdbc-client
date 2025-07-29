-- =====================================================
-- PostgreSQL JDBC Client - Schema Drop Script
-- =====================================================
-- This script completely removes all database objects
-- USE WITH CAUTION - This will delete all data!
-- =====================================================

-- Set session parameters
SET client_min_messages = WARNING;

\echo '====================================================='
\echo 'WARNING: This will DROP ALL database objects!'
\echo 'This action is IRREVERSIBLE and will delete all data!'
\echo '====================================================='

-- Log schema drop start
INSERT INTO schema_info (schema_version, component, description)
VALUES ('1.0.0', 'SCHEMA_DROP', 'Starting complete schema drop process')
ON CONFLICT DO NOTHING;

\echo ''
\echo '1. Dropping triggers...'

-- Drop triggers first to avoid issues
DROP TRIGGER IF EXISTS update_users_updated_at ON users;
DROP TRIGGER IF EXISTS update_categories_updated_at ON categories;
DROP TRIGGER IF EXISTS update_suppliers_updated_at ON suppliers;
DROP TRIGGER IF EXISTS update_products_updated_at ON products;
DROP TRIGGER IF EXISTS update_customers_updated_at ON customers;
DROP TRIGGER IF EXISTS update_orders_updated_at ON orders;
DROP TRIGGER IF EXISTS update_departments_updated_at ON departments;
DROP TRIGGER IF EXISTS update_employees_updated_at ON employees;

\echo '   ✓ Triggers dropped'

\echo ''
\echo '2. Dropping functions...'

-- Drop functions
DROP FUNCTION IF EXISTS update_updated_at_column() CASCADE;
DROP FUNCTION IF EXISTS log_schema_operation(VARCHAR, VARCHAR, TEXT, INTEGER) CASCADE;

\echo '   ✓ Functions dropped'

\echo ''
\echo '3. Dropping views...'

-- Drop views
DROP VIEW IF EXISTS v_customer_summary CASCADE;
DROP VIEW IF EXISTS v_product_inventory CASCADE;

\echo '   ✓ Views dropped'

\echo ''
\echo '4. Dropping tables in dependency order...'

-- Drop tables in reverse dependency order to handle foreign keys
DROP TABLE IF EXISTS logs CASCADE;
DROP TABLE IF EXISTS documents CASCADE;
DROP TABLE IF EXISTS contacts CASCADE;
DROP TABLE IF EXISTS addresses CASCADE;
DROP TABLE IF EXISTS reviews CASCADE;
DROP TABLE IF EXISTS promotions CASCADE;
DROP TABLE IF EXISTS shipments CASCADE;
DROP TABLE IF EXISTS inventory CASCADE;
DROP TABLE IF EXISTS payments CASCADE;
DROP TABLE IF EXISTS invoices CASCADE;
DROP TABLE IF EXISTS tasks CASCADE;
DROP TABLE IF EXISTS projects CASCADE;
DROP TABLE IF EXISTS orders CASCADE;
DROP TABLE IF EXISTS employees CASCADE;
DROP TABLE IF EXISTS departments CASCADE;
DROP TABLE IF EXISTS products CASCADE;
DROP TABLE IF EXISTS suppliers CASCADE;
DROP TABLE IF EXISTS categories CASCADE;
DROP TABLE IF EXISTS customers CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS schema_info CASCADE;

\echo '   ✓ All tables dropped'

\echo ''
\echo '5. Dropping sequences...'

-- Drop all sequences
DROP SEQUENCE IF EXISTS users_seq CASCADE;
DROP SEQUENCE IF EXISTS categories_seq CASCADE;
DROP SEQUENCE IF EXISTS products_seq CASCADE;
DROP SEQUENCE IF EXISTS customers_seq CASCADE;
DROP SEQUENCE IF EXISTS orders_seq CASCADE;
DROP SEQUENCE IF EXISTS suppliers_seq CASCADE;
DROP SEQUENCE IF EXISTS employees_seq CASCADE;
DROP SEQUENCE IF EXISTS departments_seq CASCADE;
DROP SEQUENCE IF EXISTS projects_seq CASCADE;
DROP SEQUENCE IF EXISTS tasks_seq CASCADE;
DROP SEQUENCE IF EXISTS invoices_seq CASCADE;
DROP SEQUENCE IF EXISTS payments_seq CASCADE;
DROP SEQUENCE IF EXISTS inventory_seq CASCADE;
DROP SEQUENCE IF EXISTS shipments_seq CASCADE;
DROP SEQUENCE IF EXISTS reviews_seq CASCADE;
DROP SEQUENCE IF EXISTS promotions_seq CASCADE;
DROP SEQUENCE IF EXISTS addresses_seq CASCADE;
DROP SEQUENCE IF EXISTS contacts_seq CASCADE;
DROP SEQUENCE IF EXISTS documents_seq CASCADE;
DROP SEQUENCE IF EXISTS logs_seq CASCADE;

\echo '   ✓ All sequences dropped'

\echo ''
\echo '6. Dropping custom types...'

-- Drop custom enum types
DROP TYPE IF EXISTS order_status_type CASCADE;
DROP TYPE IF EXISTS payment_status_type CASCADE;
DROP TYPE IF EXISTS task_status_type CASCADE;
DROP TYPE IF EXISTS priority_type CASCADE;

\echo '   ✓ Custom types dropped'

\echo ''
\echo '====================================================='
\echo 'Schema drop completed!'
\echo ''
\echo 'All database objects have been removed.'
\echo 'The database is now clean and ready for fresh schema creation.'
\echo '====='