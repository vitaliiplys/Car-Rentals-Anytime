INSERT INTO users (id, email, password, first_name, last_name) VALUES
    (10, 'admin@example.com', '2a$10$23rpqSNEIYZNXw07qk4KJuN4eUV9jNPiW9fckzgj0adI9RPzav2gG', 'Admin', 'Admin');

INSERT INTO roles (id, name) VALUES (1, 'ROLE_MANAGER');
INSERT INTO user_role (user_id, role_id) VALUES (10, 1);
INSERT INTO cars (id, model, brand, inventory, daily_fee, type) VALUES
    (1, 'Model S', 'Tesla', 1, 1100.99, 'SEDAN');
INSERT INTO cars (id, model, brand, inventory, daily_fee, type) VALUES
    (2, 'Model Y', 'Tesla', 1, 1500.99, 'SUV');
