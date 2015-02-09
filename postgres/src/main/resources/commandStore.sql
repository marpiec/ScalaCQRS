CREATE TABLE IF NOT EXISTS commands (
  id INT NOT NULL PRIMARY KEY,
  command_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  command_time TIMESTAMP NOT NULL,
  command_type VARCHAR(128) NOT NULL,
  command_type_version INT NOT NULL,
  command VARCHAR(10240) NOT NULL);

CREATE SEQUENCE commands_seq;
