INSERT INTO payments (id, rental_id, session_url, session_id, amount_to_pay, status, type, is_deleted) VALUES
(1, 2, 'https://stripe.com/pay/sess_pending123', 'sess_pending123', 1100.99, 'PENDING', 'PAYMENT', FALSE);
INSERT INTO payments (id, rental_id, session_url, session_id, amount_to_pay, status, type, is_deleted) VALUES
(2, 3, 'https://stripe.com/pay/sess_paid456', 'sess_paid456', 1500.99, 'PAID', 'PAYMENT', FALSE);