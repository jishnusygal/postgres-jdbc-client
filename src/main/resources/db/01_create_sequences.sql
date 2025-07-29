-- =====================================================
-- PostgreSQL JDBC Client - Database Sequences
-- =====================================================
-- This file creates all sequences used by the tables
-- Sequences provide auto-incrementing primary keys
-- =====================================================

-- Drop existing sequences if they exist (for re-creation)
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

-- Create sequences with proper configuration
-- Each sequence starts at 1 and increments by 1
-- Cache size is set to 20 for better performance

-- User management sequences
CREATE SEQUENCE users_seq 
    START WITH 1 
    INCREMENT BY 1 
    CACHE 20 
    NO CYCLE;

-- Product management sequences  
CREATE SEQUENCE categories_seq 
    START WITH 1 
    INCREMENT BY 1 
    CACHE 20 
    NO CYCLE;

CREATE SEQUENCE products_seq 
    START WITH 1000 
    INCREMENT BY 1 
    CACHE 20 
    NO CYCLE;

-- Customer and order sequences
CREATE SEQUENCE customers_seq 
    START WITH 1000 
    INCREMENT BY 1 
    CACHE 20 
    NO CYCLE;

CREATE SEQUENCE orders_seq 
    START WITH 10000 
    INCREMENT BY 1 
    CACHE 20 
    NO CYCLE;

-- Supplier management
CREATE SEQUENCE suppliers_seq 
    START WITH 1000 
    INCREMENT BY 1 
    CACHE 20 
    NO CYCLE;

-- HR management sequences
CREATE SEQUENCE employees_seq 
    START WITH 1000 
    INCREMENT BY 1 
    CACHE 20 
    NO CYCLE;

CREATE SEQUENCE departments_seq 
    START WITH 1 
    INCREMENT BY 1 
    CACHE 10 
    NO CYCLE;

-- Project management sequences
CREATE SEQUENCE projects_seq 
    START WITH 1000 
    INCREMENT BY 1 
    CACHE 20 
    NO CYCLE;

CREATE SEQUENCE tasks_seq 
    START WITH 1 
    INCREMENT BY 1 
    CACHE 50 
    NO CYCLE;

-- Financial sequences
CREATE SEQUENCE invoices_seq 
    START WITH 100000 
    INCREMENT BY 1 
    CACHE 20 
    NO CYCLE;

CREATE SEQUENCE payments_seq 
    START WITH 1 
    INCREMENT BY 1 
    CACHE 20 
    NO CYCLE;

-- Operations sequences
CREATE SEQUENCE inventory_seq 
    START WITH 1 
    INCREMENT BY 1 
    CACHE 20 
    NO CYCLE;

CREATE SEQUENCE shipments_seq 
    START WITH 10000 
    INCREMENT BY 1 
    CACHE 20 
    NO CYCLE;

-- Marketing sequences
CREATE SEQUENCE reviews_seq 
    START WITH 1 
    INCREMENT BY 1 
    CACHE 50 
    NO CYCLE;

CREATE SEQUENCE promotions_seq 
    START WITH 1 
    INCREMENT BY 1 
    CACHE 10 
    NO CYCLE;

-- Supporting data sequences
CREATE SEQUENCE addresses_seq 
    START WITH 1 
    INCREMENT BY 1 
    CACHE 50 
    NO CYCLE;

CREATE SEQUENCE contacts_seq 
    START WITH 1 
    INCREMENT BY 1 
    CACHE 50 
    NO CYCLE;

CREATE SEQUENCE documents_seq 
    START WITH 1 
    INCREMENT BY 1 
    CACHE 20 
    NO CYCLE;

CREATE SEQUENCE logs_seq 
    START WITH 1 
    INCREMENT BY 1 
    CACHE 100 
    NO CYCLE;

-- Grant usage permissions (adjust as needed for your security model)
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO PUBLIC;

-- Display created sequences
SELECT schemaname, sequencename, start_value, increment_by, max_value, cache_size
FROM pg_sequences 
WHERE schemaname = 'public' 
ORDER BY sequencename;