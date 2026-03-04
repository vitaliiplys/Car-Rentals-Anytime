INSERT INTO users (id, email, password, first_name, last_name) VALUES
(2, 'customer@example.com', '2a$10$23rpqSNEIYZNXw07qk4KJuN4eUV9jNPiW9fckzgj0adI9RPzav2gG', 'Customer', 'User');
INSERT INTO roles (id, name) VALUES (2, 'ROLE_CUSTOMER');
INSERT INTO user_role (user_id, role_id) VALUES (2, 2);