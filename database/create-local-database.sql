-- Safe local bootstrap: creates the database only when it does not exist.
-- It does not drop tables or delete existing records.
CREATE DATABASE IF NOT EXISTS property_registration_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
