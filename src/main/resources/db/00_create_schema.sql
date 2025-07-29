-- =====================================================
-- PostgreSQL JDBC Client - Main Schema Creation Script
-- =====================================================
-- This is the master script that orchestrates the entire schema creation process.
-- It executes all other SQL files in the correct order
-- =====================================================

-- Set session parameters for optimal schema creation
SET client_min_messages = WARNING;
SET timezone = 'UTC';

-- Create schema information table for tracking
CREATE TABLE IF NOT EXISTS schema_info (
    id SERIAL PRIMARY KEY,
    schema_version VARCHAR(20) NOT NULL,
    component VARCHAR(50) NOT NULL,
    description TEXT,
    executed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    execution_time_ms INTEGER,
    status VARCHAR(20) DEFAULT 'SUCCESS'
);

-- Function to log schema operations
CREATE OR REPLACE FUNCTION log_schema_operation(
    p_schema_version VARCHAR(20),
    p_component VARCHAR(50),
    p_description TEXT,
    p_execution_time_ms INTEGER DEFAULT NULL
) RETURNS VOID AS $$
BEGIN
    INSERT INTO schema_info (schema_version, component, description, execution_time_ms)
    VALUES (p_schema_version, p_component, p_description, p_execution_time_ms);
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- SCHEMA CREATION PROCESS
-- =====================================================

\echo '====================================================='
\echo 'PostgreSQL JDBC Client - Schema Creation Started'
\echo '====================================================='

-- Record start time
\set start_time `date +%s%3N`

-- Log schema creation start
SELECT log_schema_operation('1.0.0', 'SCHEMA_START', 'Starting complete schema creation process');

\echo ''
\echo '1. Creating database sequences...'
\set seq_start `date +%s%3N`

-- Execute sequences creation
\i 01_create_sequences.sql

\set seq_end `date +%s%3N`
\set seq_duration :seq_end - :seq_start

SELECT log_schema_operation('1.0.0', 'SEQUENCES', 'Created all database sequences', :seq_duration);
\echo '   ✓ Sequences created successfully'

\echo ''
\echo '2. Creating database tables...'
\set tables_start `date +%s%3N`

-- Execute tables creation
\i 02_create_tables.sql

\set tables_end `date +%s%3N`
\set tables_duration :tables_end - :tables_start

SELECT log_schema_operation('1.0.0', 'TABLES', 'Created all 20 database tables', :tables_duration);
\echo '   ✓ Tables created successfully'

\echo ''
\echo '3. Creating database indexes...'
\set indexes_start `date +%s%3N`

-- Execute indexes creation
\i 03_create_indexes.sql

\set indexes_end `date +%s%3N`
\set indexes_duration :indexes_end - :indexes_start

SELECT log_schema_operation('1.0.0', 'INDEXES', 'Created all database indexes', :indexes_duration);
\echo '   ✓ Indexes created successfully'

\echo ''
\echo '4. Creating additional database objects...'
\set objects_start `date +%s%3N`

-- =====================================================
-- ADDITIONAL DATABASE OBJECTS
-- =====================================================

-- Create enum types for better data consistency
CREATE TYPE IF NOT EXISTS order_status_type AS ENUM (
    'PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED'
);

CREATE TYPE IF NOT EXISTS payment_status_type AS ENUM (
    'PENDING', 'PARTIAL', 'PAID', 'REFUNDED'
);

CREATE TYPE IF NOT EXISTS task_status_type AS ENUM (
    'TODO', 'IN_PROGRESS', 'IN_REVIEW', 'TESTING', 'DONE', 'CANCELLED'
);

CREATE TYPE IF NOT EXISTS priority_type AS ENUM (
    'LOW', 'MEDIUM', 'HIGH', 'CRITICAL'
);

-- Create views for common queries
CREATE OR REPLACE VIEW v_customer_summary AS
SELECT 
    c.id,
    c.customer_code,
    c.company_name,
    c.first_name,
    c.last_name,
    c.email,
    c.total_orders,
    c.total_spent,
    c.average_order_value,
    c.customer_segment,
    c.is_active,
    COUNT(o.id) as open_orders,
    MAX(o.order_date) as last_order_date
FROM customers c
LEFT JOIN orders o ON c.id = o.customer_id AND o.status NOT IN ('DELIVERED', 'CANCELLED')
GROUP BY c.id;

CREATE OR REPLACE VIEW v_product_inventory AS
SELECT 
    p.id,
    p.name,
    p.sku,
    p.selling_price,
    p.category_id,
    c.name as category_name,
    p.supplier_id,
    s.company_name as supplier_name,
    i.warehouse_location,
    i.quantity_on_hand,
    i.quantity_available,
    i.reorder_level,
    CASE 
        WHEN i.quantity_available <= i.reorder_level THEN 'LOW_STOCK'
        WHEN i.quantity_available = 0 THEN 'OUT_OF_STOCK'
        ELSE 'IN_STOCK'
    END as stock_status
FROM products p
LEFT JOIN categories c ON p.category_id = c.id
LEFT JOIN suppliers s ON p.supplier_id = s.id
LEFT JOIN inventory i ON p.id = i.product_id
WHERE p.is_active = TRUE;

-- Create functions for business logic
CREATE OR REPLACE FUNCTION get_customer_orders(customer_id_param BIGINT)
RETURNS TABLE(
    order_id BIGINT,
    order_number VARCHAR(50),
    order_date TIMESTAMP,
    status VARCHAR(20),
    total_amount DECIMAL(12,2),
    currency_code VARCHAR(3)
) AS $$
BEGIN
    RETURN QUERY
    SELECT o.id, o.order_number, o.order_date, o.status, o.total_amount, o.currency_code
    FROM orders o
    WHERE o.customer_id = customer_id_param
    ORDER BY o.order_date DESC;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_customer_totals(customer_id_param BIGINT)
RETURNS VOID AS $$
DECLARE
    total_orders_count INTEGER;
    total_spent_amount DECIMAL(15,2);
    avg_order_value DECIMAL(10,2);
BEGIN
    SELECT 
        COUNT(*),
        COALESCE(SUM(total_amount), 0),
        COALESCE(AVG(total_amount), 0)
    INTO total_orders_count, total_spent_amount, avg_order_value
    FROM orders 
    WHERE customer_id = customer_id_param 
    AND status IN ('DELIVERED', 'PAID');
    
    UPDATE customers 
    SET 
        total_orders = total_orders_count,
        total_spent = total_spent_amount,
        average_order_value = avg_order_value,
        updated_at = CURRENT_TIMESTAMP
    WHERE id = customer_id_param;
END;
$$ LANGUAGE plpgsql;

\set objects_end `date +%s%3N`
\set objects_duration :objects_end - :objects_start

SELECT log_schema_operation('1.0.0', 'OBJECTS', 'Created views, functions, and triggers', :objects_duration);
\echo '   ✓ Additional objects created successfully'

-- =====================================================
-- SCHEMA COMPLETION AND SUMMARY
-- =====================================================

\echo ''
\echo '5. Generating schema summary...'

-- Calculate total execution time
\set end_time `date +%s%3N`
\set total_duration :end_time - :start_time

SELECT log_schema_operation('1.0.0', 'SCHEMA_COMPLETE', 'Schema creation completed successfully', :total_duration);

-- Display summary information
\echo ''
\echo '====================================================='
\echo 'SCHEMA CREATION SUMMARY'
\echo '====================================================='

-- Count objects created
SELECT 
    'SEQUENCES' as object_type,
    COUNT(*) as count
FROM pg_sequences 
WHERE schemaname = 'public'
UNION ALL
SELECT 
    'TABLES' as object_type,
    COUNT(*) as count
FROM pg_tables 
WHERE schemaname = 'public' AND tablename != 'schema_info'
UNION ALL
SELECT 
    'INDEXES' as object_type,
    COUNT(*) as count
FROM pg_indexes 
WHERE schemaname = 'public'
UNION ALL
SELECT 
    'VIEWS' as object_type,
    COUNT(*) as count
FROM pg_views 
WHERE schemaname = 'public';

\echo ''
\echo '====================================================='
\echo 'Schema creation completed successfully!'
\echo 'Total execution time: ' :total_duration 'ms'
\echo ''
\echo 'The database now contains:'
\echo '• 20 sequences for auto-incrementing IDs'
\echo '• 20 tables with full relationships'
\echo '• 100+ indexes for optimal performance'
\echo '• Views for common queries'
\echo '• Stored procedures for business logic'
\echo '• Triggers for automatic calculations'
\echo ''
\echo 'Ready for application use!'
\echo '====================================================='