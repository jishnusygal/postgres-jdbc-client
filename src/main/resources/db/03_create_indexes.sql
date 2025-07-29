-- =====================================================
-- PostgreSQL JDBC Client - Database Indexes
-- =====================================================
-- This file creates all indexes for optimal query performance
-- Indexes are organized by table and include both single and composite indexes
-- =====================================================

-- =====================================================
-- USERS TABLE INDEXES
-- =====================================================
-- Primary search and authentication indexes
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_phone ON users(phone);

-- Status and activity indexes
CREATE INDEX IF NOT EXISTS idx_users_is_active ON users(is_active);
CREATE INDEX IF NOT EXISTS idx_users_email_verified ON users(email_verified);
CREATE INDEX IF NOT EXISTS idx_users_last_login ON users(last_login_at);

-- Audit and timestamp indexes
CREATE INDEX IF NOT EXISTS idx_users_created_at ON users(created_at);
CREATE INDEX IF NOT EXISTS idx_users_updated_at ON users(updated_at);

-- Security-related indexes
CREATE INDEX IF NOT EXISTS idx_users_failed_login_attempts ON users(failed_login_attempts);
CREATE INDEX IF NOT EXISTS idx_users_locked_until ON users(locked_until) WHERE locked_until IS NOT NULL;

-- Composite indexes for common queries
CREATE INDEX IF NOT EXISTS idx_users_active_verified ON users(is_active, email_verified);
CREATE INDEX IF NOT EXISTS idx_users_timezone_locale ON users(timezone, locale);

-- =====================================================
-- CATEGORIES TABLE INDEXES
-- =====================================================
-- Hierarchy and navigation indexes
CREATE INDEX IF NOT EXISTS idx_categories_parent_id ON categories(parent_id);
CREATE INDEX IF NOT EXISTS idx_categories_slug ON categories(slug);
CREATE INDEX IF NOT EXISTS idx_categories_level_depth ON categories(level_depth);

-- Display and sorting indexes
CREATE INDEX IF NOT EXISTS idx_categories_display_order ON categories(display_order);
CREATE INDEX IF NOT EXISTS idx_categories_is_featured ON categories(is_featured);
CREATE INDEX IF NOT EXISTS idx_categories_is_active ON categories(is_active);

-- Search and SEO indexes
CREATE INDEX IF NOT EXISTS idx_categories_name ON categories(name);
CREATE INDEX IF NOT EXISTS idx_categories_name_lower ON categories(LOWER(name));

-- Composite indexes
CREATE INDEX IF NOT EXISTS idx_categories_active_parent ON categories(is_active, parent_id);
CREATE INDEX IF NOT EXISTS idx_categories_parent_order ON categories(parent_id, display_order);

-- =====================================================
-- SUPPLIERS TABLE INDEXES
-- =====================================================
-- Primary identification indexes
CREATE INDEX IF NOT EXISTS idx_suppliers_supplier_code ON suppliers(supplier_code);
CREATE INDEX IF NOT EXISTS idx_suppliers_company_name ON suppliers(company_name);
CREATE INDEX IF NOT EXISTS idx_suppliers_email ON suppliers(email);

-- Search and filtering indexes
CREATE INDEX IF NOT EXISTS idx_suppliers_is_active ON suppliers(is_active);
CREATE INDEX IF NOT EXISTS idx_suppliers_is_preferred ON suppliers(is_preferred);
CREATE INDEX IF NOT EXISTS idx_suppliers_quality_rating ON suppliers(quality_rating);

-- Performance and business indexes
CREATE INDEX IF NOT EXISTS idx_suppliers_lead_time_days ON suppliers(lead_time_days);
CREATE INDEX IF NOT EXISTS idx_suppliers_credit_limit ON suppliers(credit_limit);

-- Composite indexes
CREATE INDEX IF NOT EXISTS idx_suppliers_active_preferred ON suppliers(is_active, is_preferred);
CREATE INDEX IF NOT EXISTS idx_suppliers_rating_leadtime ON suppliers(quality_rating, lead_time_days);

-- =====================================================
-- PRODUCTS TABLE INDEXES
-- =====================================================
-- Primary identification and search indexes
CREATE INDEX IF NOT EXISTS idx_products_sku ON products(sku);
CREATE INDEX IF NOT EXISTS idx_products_barcode ON products(barcode) WHERE barcode IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_products_name ON products(name);
CREATE INDEX IF NOT EXISTS idx_products_name_lower ON products(LOWER(name));

-- Category and supplier relationships
CREATE INDEX IF NOT EXISTS idx_products_category_id ON products(category_id);
CREATE INDEX IF NOT EXISTS idx_products_supplier_id ON products(supplier_id);

-- Pricing and inventory indexes
CREATE INDEX IF NOT EXISTS idx_products_selling_price ON products(selling_price);
CREATE INDEX IF NOT EXISTS idx_products_cost_price ON products(cost_price);
CREATE INDEX IF NOT EXISTS idx_products_min_stock_level ON products(min_stock_level);

-- Status and feature indexes
CREATE INDEX IF NOT EXISTS idx_products_is_active ON products(is_active);
CREATE INDEX IF NOT EXISTS idx_products_is_featured ON products(is_featured);
CREATE INDEX IF NOT EXISTS idx_products_featured_until ON products(featured_until) WHERE featured_until IS NOT NULL;

-- Search and categorization indexes
CREATE INDEX IF NOT EXISTS idx_products_brand ON products(brand);
CREATE INDEX IF NOT EXISTS idx_products_model ON products(model);
CREATE INDEX IF NOT EXISTS idx_products_tags ON products USING GIN(tags);

-- Composite indexes for common queries
CREATE INDEX IF NOT EXISTS idx_products_category_active ON products(category_id, is_active);
CREATE INDEX IF NOT EXISTS idx_products_supplier_active ON products(supplier_id, is_active);
CREATE INDEX IF NOT EXISTS idx_products_active_featured ON products(is_active, is_featured);
CREATE INDEX IF NOT EXISTS idx_products_price_range ON products(selling_price, is_active);

-- Full-text search index
CREATE INDEX IF NOT EXISTS idx_products_search ON products USING GIN(to_tsvector('english', name || ' ' || COALESCE(description, '') || ' ' || COALESCE(brand, '')));

-- Continue with more indexes...
\echo 'Primary table indexes created successfully!'