CREATE TABLE IF NOT EXISTS contactos (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(100) NOT NULL,
  tienda VARCHAR(100) NOT NULL,
  telefono VARCHAR(30) NOT NULL,
  fecha DATETIME NOT NULL
) $$

CREATE TABLE IF NOT EXISTS auditoria_accesos (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  correo VARCHAR(150) NOT NULL,
  exito BOOLEAN NOT NULL,
  ip VARCHAR(60),
  fecha DATETIME NOT NULL
) $$

CREATE TABLE IF NOT EXISTS conversaciones (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  cliente_id BIGINT NOT NULL,
  cliente_nombre VARCHAR(120) NOT NULL,
  estado VARCHAR(20) NOT NULL DEFAULT 'IA',
  asesor_id BIGINT NULL,
  asesor_nombre VARCHAR(120) NULL,
  total_mensajes INT NOT NULL DEFAULT 0,
  actualizada DATETIME NOT NULL,
  INDEX idx_conv_cliente (cliente_id)
) $$

CREATE TABLE IF NOT EXISTS mensajes (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  conversacion_id BIGINT NOT NULL,
  autor VARCHAR(10) NOT NULL,
  texto VARCHAR(1000) NOT NULL,
  fecha DATETIME NOT NULL,
  CONSTRAINT fk_mensaje_conversacion FOREIGN KEY (conversacion_id) REFERENCES conversaciones (id) ON DELETE CASCADE
) $$

DROP TRIGGER IF EXISTS tr_mensaje_validar $$
CREATE TRIGGER tr_mensaje_validar BEFORE INSERT ON mensajes
FOR EACH ROW
BEGIN
  DECLARE v_estado VARCHAR(20);
  SELECT estado INTO v_estado FROM conversaciones WHERE id = NEW.conversacion_id;
  IF v_estado = 'CERRADA' THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La conversación está cerrada';
  END IF;
END $$

DROP TRIGGER IF EXISTS tr_mensaje_nuevo $$
CREATE TRIGGER tr_mensaje_nuevo AFTER INSERT ON mensajes
FOR EACH ROW
BEGIN
  UPDATE conversaciones
  SET actualizada = NEW.fecha, total_mensajes = total_mensajes + 1
  WHERE id = NEW.conversacion_id;
END $$
