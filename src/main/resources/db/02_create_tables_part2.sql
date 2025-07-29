-- =====================================================
-- PostgreSQL JDBC Client - Database Tables (Part 2)
-- =====================================================
-- Continuation of table creation
-- =====================================================

-- =====================================================
-- 6. ORDERS TABLE - Order Management
-- =====================================================
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT PRIMARY KEY DEFAULT nextval('orders_seq'),
    order_number VARCHAR(50) UNIQUE NOT NULL,
    customer_id BIGINT NOT NULL REFERENCES customers(id),
    user_id BIGINT REFERENCES users(id), -- Sales rep or user who created order
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    required_date DATE,
    shipped_date DATE,
    delivery_date DATE,
    status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED
    priority VARCHAR(10) DEFAULT 'NORMAL', -- LOW, NORMAL, HIGH, URGENT
    subtotal DECIMAL(12,2) DEFAULT 0.00,
    tax_amount DECIMAL(10,2) DEFAULT 0.00,
    shipping_cost DECIMAL(8,2) DEFAULT 0.00,
    discount_amount DECIMAL(10,2) DEFAULT 0.00,
    total_amount DECIMAL(12,2) DEFAULT 0.00,
    currency_code VARCHAR(3) DEFAULT 'USD',
    payment_status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, PARTIAL, PAID, REFUNDED
    payment_method VARCHAR(50),
    shipping_method VARCHAR(50),
    tracking_number VARCHAR(100),
    billing_address_id BIGINT,
    shipping_address_id BIGINT,
    notes TEXT,
    internal_notes TEXT,
    cancellation_reason TEXT,
    refund_amount DECIMAL(10,2) DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT REFERENCES users(id),
    updated_by BIGINT REFERENCES users(id)
);

CREATE TRIGGER update_orders_updated_at BEFORE UPDATE ON orders
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- =====================================================
-- 7. DEPARTMENTS TABLE - Department Structure
-- =====================================================
CREATE TABLE IF NOT EXISTS departments (
    id BIGINT PRIMARY KEY DEFAULT nextval('departments_seq'),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    department_code VARCHAR(20) UNIQUE,
    parent_id BIGINT REFERENCES departments(id),
    manager_id BIGINT, -- Will reference employees(id) after employees table is created
    location VARCHAR(100),
    cost_center VARCHAR(50),
    budget DECIMAL(15,2),
    phone VARCHAR(20),
    email VARCHAR(100),
    is_billable BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT REFERENCES users(id),
    updated_by BIGINT REFERENCES users(id),
    is_active BOOLEAN DEFAULT TRUE
);

CREATE TRIGGER update_departments_updated_at BEFORE UPDATE ON departments
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- =====================================================
-- 8. EMPLOYEES TABLE - Employee Management
-- =====================================================
CREATE TABLE IF NOT EXISTS employees (
    id BIGINT PRIMARY KEY DEFAULT nextval('employees_seq'),
    employee_code VARCHAR(20) UNIQUE NOT NULL,
    user_id BIGINT REFERENCES users(id), -- Link to users table for login
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(20),
    mobile VARCHAR(20),
    department_id BIGINT REFERENCES departments(id),
    job_title VARCHAR(100),
    employment_type VARCHAR(20) DEFAULT 'FULL_TIME', -- FULL_TIME, PART_TIME, CONTRACT, INTERN
    employment_status VARCHAR(20) DEFAULT 'ACTIVE', -- ACTIVE, INACTIVE, TERMINATED
    hire_date DATE NOT NULL,
    termination_date DATE,
    manager_id BIGINT REFERENCES employees(id),
    salary DECIMAL(10,2),
    hourly_rate DECIMAL(8,2),
    currency_code VARCHAR(3) DEFAULT 'USD',
    work_location VARCHAR(100),
    office_location VARCHAR(100),
    emergency_contact_name VARCHAR(100),
    emergency_contact_phone VARCHAR(20),
    date_of_birth DATE,
    gender VARCHAR(10),
    marital_status VARCHAR(20),
    social_security_number VARCHAR(20), -- Should be encrypted in production
    tax_id VARCHAR(50),
    bank_account_number VARCHAR(50), -- Should be encrypted in production
    vacation_days_total INTEGER DEFAULT 20,
    vacation_days_used INTEGER DEFAULT 0,
    sick_days_total INTEGER DEFAULT 10,
    sick_days_used INTEGER DEFAULT 0,
    performance_rating DECIMAL(3,2),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT REFERENCES users(id),
    updated_by BIGINT REFERENCES users(id),
    is_active BOOLEAN DEFAULT TRUE
);

CREATE TRIGGER update_employees_updated_at BEFORE UPDATE ON employees
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Add foreign key constraint for department manager after employees table exists
ALTER TABLE departments 
ADD CONSTRAINT fk_departments_manager 
FOREIGN KEY (manager_id) REFERENCES employees(id);

\echo 'All 20 tables created successfully!'
\echo 'Proceeding to create indexes...'