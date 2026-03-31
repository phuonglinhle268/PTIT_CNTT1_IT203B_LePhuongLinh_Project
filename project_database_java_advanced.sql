create database project_java_advanced;
use project_java_advanced;

create table Categories (
    category_id int auto_increment primary key,
    category_name varchar(100) unique not null,
    created_at datetime default current_timestamp
);

create table Products (
    product_id int auto_increment primary key,
    category_id int not null,
    product_name varchar(300) not null,
    brand varchar(100) not null,
    storage varchar(50) not null,
    color varchar(50) not null,
    price decimal(12,2) not null check(price > 0),
    stock int not null default 0 check (stock >= 0),
    description text,
    created_at datetime default current_timestamp,
    updated_at  datetime default current_timestamp on update current_timestamp,
    foreign key (category_id) references Categories(category_id) 
);

create table Users (
    user_id  int auto_increment primary key,
    username varchar(100) not null unique,
    password varchar(200)  not null,
    full_name varchar(50)  not null ,
    email varchar(150) not null  unique,
    phone varchar(15)  not null ,
    address text,
    role enum('ADMIN','CUSTOMER') not null default 'CUSTOMER',
    created_at datetime default current_timestamp 
);

create table Orders (
    order_id int auto_increment primary key,
    user_id int not null,
    order_date datetime default current_timestamp ,
    total_amount decimal(12,2) not null,
    status enum('PENDING','SHIPPING','DELIVERED','CANCELLED') not null default 'PENDING',
    coupon_code varchar(60) null,
    foreign key (user_id) references Users(user_id) 
);

create table OrderDetails (
    order_detail_id int auto_increment primary key,
    order_id int not null,
    product_id int not null,
    quantity int not null check(quantity > 0),
    price_at_purchase decimal(12,2) not null,
	foreign key (order_id) references Orders(order_id),
    foreign key (product_id) references Products(product_id)
);

create table FlashSales (
    flash_sale_id int auto_increment primary key,
    discount_percent int not null check(discount_percent between 1 and 99),
    start_time datetime not null,
    end_time datetime not null,
    is_active boolean default true,
    created_at  datetime default current_timestamp ,
    updated_at  datetime default current_timestamp on update current_timestamp
);

create table Coupons (
    coupon_id int auto_increment primary key,
    coupon_code varchar(50) not null unique,
    discount_percent int not null check(discount_percent between 1 and 100),
    max_uses int not null default 1,
    used_count int not null default 0,
    start_date date not null,
    end_date date not null,
    is_active boolean default true,
    created_at datetime default current_timestamp 
);


INSERT INTO Categories (category_name) VALUES
('Apple'), ('Samsung'), ('Xiaomi'), ('Oppo'), ('Vivo');

-- Admin & customers (password đều là "123456" — BCrypt hash)
INSERT INTO Users (username, password, full_name, email, phone, address, role) VALUES
('admin',
 '$2a$12$4IH5C64AozGjgTis5Mc8JOWtykL1IoebPnGKofRsc4HJft5kmKriu',
 'Administrator', 'admin@phonestore.vn', '0123456789', 'Hà Nội', 'ADMIN'),
('nguyenvana',
 '$2a$12$4IH5C64AozGjgTis5Mc8JOWtykL1IoebPnGKofRsc4HJft5kmKriu',
 'Nguyễn Văn A', 'nguyenvana@gmail.com', '0912345678',
 '123 Lê Lợi, Q.1, TP.HCM', 'CUSTOMER'),
('tranthib',
 '$2a$12$4IH5C64AozGjgTis5Mc8JOWtykL1IoebPnGKofRsc4HJft5kmKriu',
 'Trần Thị B', 'tranthib@gmail.com', '0987654321',
 '456 Nguyễn Huệ, Q.1, TP.HCM', 'CUSTOMER');

INSERT INTO Products (category_id, product_name, brand, storage, color, price, stock, description) VALUES
(1, 'iPhone 15 Pro Max', 'Apple',   '256GB', 'Titan', 34990000, 20, 'Chip A17 Pro, camera 48MP, USB-C'),
(1, 'iPhone 15 Pro',     'Apple',   '128GB', 'Titan Đen',      28990000, 15, 'Chip A17 Pro, Dynamic Island'),
(1, 'iPhone 15',         'Apple',   '128GB', 'Hồng',           22990000, 30, 'Chip A16 Bionic, USB-C'),
(1, 'iPhone 14',         'Apple',   '128GB', 'Midnight',       18990000, 10, 'Chip A15 Bionic'),
(1, 'iPhone 13',         'Apple',   '128GB', 'Starlight',      15990000,  0, 'Chip A15 — HẾT HÀNG'),
(2, 'Galaxy S24 Ultra',  'Samsung', '512GB', 'Titan Gray',  32990000, 12, 'S Pen, camera 200MP'),
(2, 'Galaxy S24+',       'Samsung', '256GB', 'Violet',  26990000, 18, 'Snapdragon 8 Gen 3'),
(2, 'Galaxy A55',        'Samsung', '256GB', 'Xanh Nhạt',      10990000, 40, 'AMOLED 120Hz, pin 5000mAh'),
(2, 'Galaxy A35',        'Samsung', '128GB', 'Xanh Lam',        8490000, 35, 'AMOLED 120Hz, IP67'),
(3, 'Xiaomi 14 Ultra',   'Xiaomi',  '512GB', 'Titan Xám',      28990000, 10, 'Camera Leica, Snapdragon 8 Gen 3'),
(3, 'Redmi Note 13 Pro', 'Xiaomi',  '256GB', 'Midnight',   8990000, 50, 'AMOLED 120Hz, 200MP'),
(3, 'Redmi 13C',         'Xiaomi',  '128GB', 'Đen',              3490000, 60, 'Pin 5000mAh, sạc 18W'),
(4, 'OPPO Reno 11',      'Oppo',    '256GB', 'Xanh Pastel',    13990000, 25, 'Camera portrait AI, sạc 67W'),
(4, 'OPPO A98',          'Oppo',    '256GB', 'Xanh Biển',       8990000, 30, 'Snapdragon 695, sạc 67W'),
(5, 'Vivo V30',          'Vivo',    '256GB', 'Xanh Lá',        11990000, 20, 'Aura Light Portrait, sạc 80W'),
(5, 'Vivo Y36',          'Vivo',    '128GB', 'Vàng Nhạt',       5990000, 45, 'Pin 5000mAh, sạc 44W');

-- Coupons (đủ các trạng thái để test)
INSERT INTO Coupons (coupon_code, discount_percent, max_uses, used_count, start_date, end_date, is_active) VALUES
('WELCOME10', 10, 100,  0, '2026-01-01', '2026-12-31', TRUE),  -- Còn hạn, còn lượt
('SALE20',    20,  50,  5, '2026-03-01', '2026-04-30', TRUE),  -- Còn hạn, còn lượt
('VIP30',     30,  10, 10, '2026-01-01', '2026-06-30', TRUE),  -- Hết lượt dùng
('EXPIRED15', 15,  20,  0, '2025-01-01', '2025-12-31', TRUE);  -- Hết hạn

-- FlashSales (đủ các trạng thái để test)
INSERT INTO FlashSales (discount_percent, start_time, end_time, is_active) VALUES
(20, NOW() - INTERVAL 1 HOUR, NOW() + INTERVAL 5 HOUR, TRUE),   -- Đang diễn ra
(30, NOW() + INTERVAL 1 DAY,  NOW() + INTERVAL 2 DAY,  TRUE),   -- Sắp diễn ra
(15, NOW() - INTERVAL 2 DAY,  NOW() - INTERVAL 1 DAY,  FALSE);  -- Đã kết thúc

-- Đơn hàng mẫu (user nguyenvana = id 2, tranthib = id 3)
INSERT INTO Orders (user_id, order_date, total_amount, status, coupon_code) VALUES
(2, NOW() - INTERVAL 5 DAY,  51980000, 'DELIVERED', NULL),
(2, NOW() - INTERVAL 2 DAY,  22990000, 'SHIPPING',  'WELCOME10'),
(2, NOW() - INTERVAL 1 HOUR,  8990000, 'PENDING',   NULL),
(3, NOW() - INTERVAL 3 DAY,  32990000, 'DELIVERED', 'SALE20');

INSERT INTO OrderDetails (order_id, product_id, quantity, price_at_purchase) VALUES
(1, 1, 1, 34990000), (1, 3, 1, 17990000),
(2, 3, 1, 22990000),
(3, 11, 1, 8990000),
(4, 6, 1, 32990000);

DELETE FROM Users WHERE username = 'admin';

-- SELECT '=== USERS ===' AS info;
-- SELECT user_id, username, full_name, role FROM Users;

-- SELECT '=== PRODUCTS ===' AS info;
-- SELECT p.product_id, c.category_name, p.product_name,
--        FORMAT(p.price,0) AS gia, p.stock
-- FROM Products p JOIN Categories c ON p.category_id = c.category_id
-- ORDER BY c.category_name, p.product_name;

-- SELECT '=== FLASH SALES ===' AS info;
-- SELECT flash_sale_id, discount_percent, start_time, end_time, is_active FROM FlashSales;

-- SELECT '=== COUPONS ===' AS info;
-- SELECT coupon_id, coupon_code, discount_percent, max_uses, used_count,
--        start_date, end_date, is_active FROM Coupons;