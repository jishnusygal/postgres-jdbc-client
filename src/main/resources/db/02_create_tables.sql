-- =====================================================
-- PostgreSQL JDBC Client - Database Tables
-- =====================================================
-- This file creates all 20 tables for the application
-- Tables are created in dependency order (referenced tables first)
-- =====================================================

-- Enable required extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_stat_statements";

-- =====================================================
-- 1. USERS TABLE - User Management
-- =====================================================
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY DEFAULT nextval('users_seq'),
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    date_of_birth DATE,
    avatar_url VARCHAR(500),
    timezone VARCHAR(50) DEFAULT 'UTC',
    locale VARCHAR(10) DEFAULT 'en_US',
    last_login_at TIMESTAMP,
    email_verified BOOLEAN DEFAULT FALSE,
    phone_verified BOOLEAN DEFAULT FALSE,
    two_factor_enabled BOOLEAN DEFAULT FALSE,
    failed_login_attempts INTEGER DEFAULT 0,
    locked_until TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    is_active BOOLEAN DEFAULT TRUE,
    version INTEGER DEFAULT 1
);

-- Add user audit trigger
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    NEW.version = OLD.version + 1;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- =====================================================
-- 2. CATEGORIES TABLE - Product Categories
-- =====================================================
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT PRIMARY KEY DEFAULT nextval('categories_seq'),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    slug VARCHAR(100) UNIQUE,
    parent_id BIGINT REFERENCES categories(id),
    category_path TEXT, -- Full hierarchical path
    level_depth INTEGER DEFAULT 0,
    display_order INTEGER DEFAULT 0,
    image_url VARCHAR(500),
    meta_title VARCHAR(200),
    meta_description TEXT,
    is_featured BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT REFERENCES users(id),
    updated_by BIGINT REFERENCES users(id),
    is_active BOOLEAN DEFAULT TRUE
);

CREATE TRIGGER update_categories_updated_at BEFORE UPDATE ON categories
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- =====================================================
-- 3. SUPPLIERS TABLE - Supplier Management
-- =====================================================
CREATE TABLE IF NOT EXISTS suppliers (
    id BIGINT PRIMARY KEY DEFAULT nextval('suppliers_seq'),
    supplier_code VARCHAR(20) UNIQUE NOT NULL,
    company_name VARCHAR(200) NOT NULL,
    contact_person VARCHAR(100),
    email VARCHAR(100),
    phone VARCHAR(20),
    website VARCHAR(200),
    tax_id VARCHAR(50),
    payment_terms VARCHAR(50),
    credit_limit DECIMAL(15,2),
    discount_percentage DECIMAL(5,2) DEFAULT 0.00,
    lead_time_days INTEGER DEFAULT 7,
    quality_rating DECIMAL(3,2) CHECK (quality_rating >= 0 AND quality_rating <= 5),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT REFERENCES users(id),
    updated_by BIGINT REFERENCES users(id),
    is_active BOOLEAN DEFAULT TRUE,
    is_preferred BOOLEAN DEFAULT FALSE
);

CREATE TRIGGER update_suppliers_updated_at BEFORE UPDATE ON suppliers
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- =====================================================
-- 4. PRODUCTS TABLE - Product Catalog
-- =====================================================
CREATE TABLE IF NOT EXISTS products (
    id BIGINT PRIMARY KEY DEFAULT nextval('products_seq'),
    name VARCHAR(200) NOT NULL,
    description TEXT,
    short_description VARCHAR(500),
    sku VARCHAR(50) UNIQUE NOT NULL,
    barcode VARCHAR(100),
    category_id BIGINT REFERENCES categories(id),
    supplier_id BIGINT REFERENCES suppliers(id),
    brand VARCHAR(100),
    model VARCHAR(100),
    weight_kg DECIMAL(8,3),
    dimensions_cm VARCHAR(50), -- Format: "L x W x H"
    color VARCHAR(50),
    size VARCHAR(50),
    material VARCHAR(100),
    cost_price DECIMAL(10,2),
    selling_price DECIMAL(10,2),
    msrp DECIMAL(10,2), -- Manufacturer suggested retail price
    currency_code VARCHAR(3) DEFAULT 'USD',
    tax_rate DECIMAL(5,2) DEFAULT 0.00,
    min_stock_level INTEGER DEFAULT 0,
    max_stock_level INTEGER DEFAULT 1000,
    reorder_point INTEGER DEFAULT 10,
    reorder_quantity INTEGER DEFAULT 50,
    is_serialized BOOLEAN DEFAULT FALSE,
    is_digital BOOLEAN DEFAULT FALSE,
    requires_shipping BOOLEAN DEFAULT TRUE,
    warranty_months INTEGER DEFAULT 12,
    image_urls TEXT[], -- Array of image URLs
    tags TEXT[], -- Array of search tags
    meta_title VARCHAR(200),
    meta_description TEXT,
    seo_keywords TEXT,
    featured_until DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT REFERENCES users(id),
    updated_by BIGINT REFERENCES users(id),
    is_active BOOLEAN DEFAULT TRUE,
    is_featured BOOLEAN DEFAULT FALSE
);

CREATE TRIGGER update_products_updated_at BEFORE UPDATE ON products
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- =====================================================
-- 5. CUSTOMERS TABLE - Customer Management
-- =====================================================
CREATE TABLE IF NOT EXISTS customers (
    id BIGINT PRIMARY KEY DEFAULT nextval('customers_seq'),
    customer_code VARCHAR(20) UNIQUE NOT NULL,
    customer_type VARCHAR(20) DEFAULT 'INDIVIDUAL', -- INDIVIDUAL, BUSINESS
    company_name VARCHAR(200),
    contact_person VARCHAR(100),
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    email VARCHAR(100),
    phone VARCHAR(20),
    mobile VARCHAR(20),
    date_of_birth DATE,
    gender VARCHAR(10),
    tax_id VARCHAR(50),
    credit_limit DECIMAL(12,2) DEFAULT 0.00,
    payment_terms VARCHAR(50) DEFAULT 'NET_30',
    discount_percentage DECIMAL(5,2) DEFAULT 0.00,
    total_orders INTEGER DEFAULT 0,
    total_spent DECIMAL(15,2) DEFAULT 0.00,
    average_order_value DECIMAL(10,2) DEFAULT 0.00,
    last_order_date DATE,
    customer_since DATE,
    referral_source VARCHAR(100),
    loyalty_points INTEGER DEFAULT 0,
    customer_segment VARCHAR(50), -- VIP, REGULAR, NEW, etc.
    preferred_language VARCHAR(10) DEFAULT 'en',
    timezone VARCHAR(50) DEFAULT 'UTC',
    newsletter_subscribed BOOLEAN DEFAULT FALSE,
    marketing_consent BOOLEAN DEFAULT FALSE,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT REFERENCES users(id),
    updated_by BIGINT REFERENCES users(id),
    is_active BOOLEAN DEFAULT TRUE
);

CREATE TRIGGER update_customers_updated_at BEFORE UPDATE ON customers
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Continue with remaining tables in next file due to length...