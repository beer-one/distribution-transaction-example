CREATE DATABASE IF NOT EXISTS saga_account;
CREATE DATABASE IF NOT EXISTS saga_order;
CREATE DATABASE IF NOT EXISTS saga_product;


CREATE USER 'saga-tester'@'%' IDENTIFIED BY 'sagatest';
GRANT ALL PRIVILEGES ON saga_account.* TO 'saga-tester'@'%';
GRANT ALL PRIVILEGES ON saga_order.* TO 'saga-tester'@'%';
GRANT ALL PRIVILEGES ON saga_product.* TO 'saga-tester'@'%';
FLUSH PRIVILEGES;