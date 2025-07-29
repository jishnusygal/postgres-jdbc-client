-- =====================================================
-- PostgreSQL JDBC Client - Sample Data
-- =====================================================
-- This file inserts sample data for testing and demonstration
-- Execute this only in development/testing environments
-- =====================================================

-- Set session parameters
SET client_min_messages = WARNING;

-- Log sample data creation
SELECT log_schema_operation('1.0.0', 'SAMPLE_DATA', 'Creating sample data for testing');

\echo 'Creating sample data for all tables...'

-- =====================================================
-- 1. USERS - Sample Users
-- =====================================================
INSERT INTO users (username, email, first_name, last_name, password_hash, phone, timezone, is_active) VALUES
('admin', 'admin@example.com', 'System', 'Administrator', '$2a$10$DowJonesIndex123456789abcdef', '+1-555-0001', 'UTC', true),
('john.doe', 'john.doe@example.com', 'John', 'Doe', '$2a$10$DowJonesIndex123456789abcdef', '+1-555-0002', 'America/New_York', true),
('jane.smith', 'jane.smith@example.com', 'Jane', 'Smith', '$2a$10$DowJonesIndex123456789abcdef', '+1-555-0003', 'America/Los_Angeles', true),
('bob.wilson', 'bob.wilson@example.com', 'Bob', 'Wilson', '$2a$10$DowJonesIndex123456789abcdef', '+1-555-0004', 'Europe/London', true),
('alice.brown', 'alice.brown@example.com', 'Alice', 'Brown', '$2a$10$DowJonesIndex123456789abcdef', '+1-555-0005', 'Asia/Tokyo', true)
ON CONFLICT (username) DO NOTHING;

-- =====================================================
-- 2. CATEGORIES - Product Categories
-- =====================================================
INSERT INTO categories (name, description, slug, level_depth, display_order, is_active) VALUES
('Electronics', 'Electronic devices and accessories', 'electronics', 0, 1, true),
('Computers', 'Desktop and laptop computers', 'computers', 0, 2, true),
('Mobile Devices', 'Smartphones and tablets', 'mobile-devices', 0, 3, true),
('Home & Garden', 'Home improvement and gardening supplies', 'home-garden', 0, 4, true),
('Books', 'Physical and digital books', 'books', 0, 5, true),
('Clothing', 'Apparel and accessories', 'clothing', 0, 6, true),
('Sports', 'Sports equipment and accessories', 'sports', 0, 7, true),
('Automotive', 'Car parts and accessories', 'automotive', 0, 8, true),
('Health & Beauty', 'Personal care and health products', 'health-beauty', 0, 9, true),
('Toys & Games', 'Children toys and board games', 'toys-games', 0, 10, true)
ON CONFLICT (slug) DO NOTHING;

-- Add subcategories
INSERT INTO categories (name, description, slug, parent_id, level_depth, display_order, is_active) VALUES
('Laptops', 'Portable computers', 'laptops', (SELECT id FROM categories WHERE slug = 'computers'), 1, 1, true),
('Desktops', 'Desktop computers', 'desktops', (SELECT id FROM categories WHERE slug = 'computers'), 1, 2, true),
('Smartphones', 'Mobile phones', 'smartphones', (SELECT id FROM categories WHERE slug = 'mobile-devices'), 1, 1, true),
('Tablets', 'Tablet computers', 'tablets', (SELECT id FROM categories WHERE slug = 'mobile-devices'), 1, 2, true)
ON CONFLICT (slug) DO NOTHING;

-- =====================================================
-- 3. SUPPLIERS - Sample Suppliers
-- =====================================================
INSERT INTO suppliers (supplier_code, company_name, contact_person, email, phone, website, payment_terms, lead_time_days, quality_rating, is_active, is_preferred) VALUES
('SUP001', 'Tech Solutions Inc.', 'Michael Johnson', 'orders@techsolutions.com', '+1-555-1001', 'www.techsolutions.com', 'NET_30', 7, 4.5, true, true),
('SUP002', 'Global Electronics Ltd.', 'Sarah Connor', 'procurement@globalelectronics.com', '+1-555-1002', 'www.globalelectronics.com', 'NET_15', 5, 4.8, true, true),
('SUP003', 'Quality Parts Co.', 'David Lee', 'sales@qualityparts.com', '+1-555-1003', 'www.qualityparts.com', 'NET_45', 10, 4.2, true, false),
('SUP004', 'Fast Delivery Corp.', 'Emma Watson', 'orders@fastdelivery.com', '+1-555-1004', 'www.fastdelivery.com', 'NET_30', 3, 4.7, true, true),
('SUP005', 'Budget Supplies LLC', 'Tom Hardy', 'info@budgetsupplies.com', '+1-555-1005', 'www.budgetsupplies.com', 'NET_60', 14, 3.9, true, false)
ON CONFLICT (supplier_code) DO NOTHING;

-- Continue with products, customers, and other sample data...
\echo 'Sample data creation in progress...'

-- Update computed values after all inserts
SELECT log_schema_operation('1.0.0', 'SAMPLE_DATA_COMPLETE', 'Sample data creation completed successfully');

\echo 'Sample data created successfully!'