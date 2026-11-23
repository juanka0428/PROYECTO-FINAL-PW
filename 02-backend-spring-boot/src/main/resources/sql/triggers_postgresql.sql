CREATE OR REPLACE FUNCTION fn_pedido_nuevo() RETURNS TRIGGER AS $$
BEGIN
  INSERT INTO historial_pedido (pedido_id, estado, fecha, usuario)
  VALUES (NEW.id, NEW.estado, NOW(), NEW.ultimo_usuario);
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;;

DROP TRIGGER IF EXISTS trg_pedido_nuevo ON pedidos;;
CREATE TRIGGER trg_pedido_nuevo AFTER INSERT ON pedidos
FOR EACH ROW EXECUTE FUNCTION fn_pedido_nuevo();;

CREATE OR REPLACE FUNCTION fn_pedido_validar_estado() RETURNS TRIGGER AS $$
BEGIN
  IF NEW.estado <> OLD.estado AND OLD.estado IN ('ENTREGADO', 'CANCELADO') THEN
    RAISE EXCEPTION 'El pedido ya terminó y no se puede cambiar';
  END IF;
  IF NEW.estado = 'CANCELADO' AND OLD.estado <> 'CANCELADO' AND OLD.estado_pago = 'PAGADO' THEN
    RAISE EXCEPTION 'No se puede cancelar un pedido que ya fue pagado';
  END IF;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;;

DROP TRIGGER IF EXISTS trg_pedido_validar_estado ON pedidos;;
CREATE TRIGGER trg_pedido_validar_estado BEFORE UPDATE ON pedidos
FOR EACH ROW EXECUTE FUNCTION fn_pedido_validar_estado();;

CREATE OR REPLACE FUNCTION fn_pedido_cambio_estado() RETURNS TRIGGER AS $$
BEGIN
  IF NEW.estado <> OLD.estado THEN
    INSERT INTO historial_pedido (pedido_id, estado, fecha, usuario)
    VALUES (NEW.id, NEW.estado, NOW(), NEW.ultimo_usuario);

    IF NEW.estado = 'CANCELADO' THEN
      UPDATE productos p SET stock = p.stock + i.cantidad
      FROM items_pedido i
      WHERE i.producto_id = p.id AND i.pedido_id = NEW.id;
    END IF;
  END IF;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;;

DROP TRIGGER IF EXISTS trg_pedido_cambio_estado ON pedidos;;
CREATE TRIGGER trg_pedido_cambio_estado AFTER UPDATE ON pedidos
FOR EACH ROW EXECUTE FUNCTION fn_pedido_cambio_estado();;

CREATE OR REPLACE FUNCTION fn_item_descontar_stock() RETURNS TRIGGER AS $$
DECLARE
  disponible INTEGER;
  nombre_producto VARCHAR;
BEGIN
  SELECT stock, nombre INTO disponible, nombre_producto FROM productos WHERE id = NEW.producto_id;
  IF disponible IS NULL OR disponible < NEW.cantidad THEN
    RAISE EXCEPTION 'No hay suficientes unidades de %', COALESCE(nombre_producto, 'un producto del pedido');
  END IF;
  UPDATE productos SET stock = stock - NEW.cantidad WHERE id = NEW.producto_id;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;;

DROP TRIGGER IF EXISTS trg_item_descontar_stock ON items_pedido;;
CREATE TRIGGER trg_item_descontar_stock BEFORE INSERT ON items_pedido
FOR EACH ROW EXECUTE FUNCTION fn_item_descontar_stock();;

CREATE OR REPLACE FUNCTION fn_fiado_validar() RETURNS TRIGGER AS $$
DECLARE
  v_cupo INTEGER;
  v_saldo INTEGER;
BEGIN
  SELECT cupo_fiado, saldo_fiado INTO v_cupo, v_saldo FROM usuarios WHERE id = NEW.cliente_id;
  IF NEW.valor <= 0 THEN
    RAISE EXCEPTION 'El valor debe ser mayor que cero';
  END IF;
  IF NEW.tipo = 'CARGO' AND v_saldo + NEW.valor > v_cupo THEN
    RAISE EXCEPTION 'El pedido supera el cupo de fiado disponible';
  END IF;
  IF NEW.tipo = 'ABONO' AND NEW.valor > v_saldo THEN
    RAISE EXCEPTION 'El abono es mayor que lo que debe el cliente';
  END IF;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;;

DROP TRIGGER IF EXISTS trg_fiado_validar ON movimientos_fiado;;
CREATE TRIGGER trg_fiado_validar BEFORE INSERT ON movimientos_fiado
FOR EACH ROW EXECUTE FUNCTION fn_fiado_validar();;

CREATE OR REPLACE FUNCTION fn_fiado_saldo() RETURNS TRIGGER AS $$
BEGIN
  IF NEW.tipo = 'CARGO' THEN
    UPDATE usuarios SET saldo_fiado = saldo_fiado + NEW.valor WHERE id = NEW.cliente_id;
  ELSE
    UPDATE usuarios SET saldo_fiado = saldo_fiado - NEW.valor WHERE id = NEW.cliente_id;
  END IF;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;;

DROP TRIGGER IF EXISTS trg_fiado_saldo ON movimientos_fiado;;
CREATE TRIGGER trg_fiado_saldo AFTER INSERT ON movimientos_fiado
FOR EACH ROW EXECUTE FUNCTION fn_fiado_saldo();;
