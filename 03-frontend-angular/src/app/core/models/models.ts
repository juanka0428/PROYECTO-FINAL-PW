export type Rol = 'CLIENTE' | 'TENDERO' | 'REPARTIDOR' | 'ASESOR';

export interface Usuario {
  id: string;
  nombre: string;
  correo: string;
  clave: string;
  telefono: string;
  rol: Rol;
  direccion?: string;
  barrio?: string;
  lat?: number;
  lng?: number;
  cupoFiado?: number;
  saldoFiado?: number;
  activo: boolean;
}

export interface Categoria {
  id: string;
  nombre: string;
  icono: string;
}

export interface Producto {
  id: string;
  nombre: string;
  categoriaId: string;
  precio: number;
  stock: number;
  stockMinimo: number;
  unidad: string;
  imagen: string;
  palabrasClave: string[];
  activo: boolean;
}

export type EstadoPedido = 'RECIBIDO' | 'PREPARANDO' | 'EN_CAMINO' | 'ENTREGADO' | 'CANCELADO';
export type MetodoPago = 'EN_LINEA' | 'CONTRA_ENTREGA' | 'FIADO';
export type EstadoPago = 'PENDIENTE' | 'PAGADO' | 'FIADO';
export type OrigenPedido = 'CATALOGO' | 'TEXTO' | 'VOZ' | 'FOTO' | 'RECURRENTE';

export interface ItemPedido {
  productoId: string;
  nombre: string;
  imagen: string;
  precio: number;
  cantidad: number;
}

export interface Coordenada {
  lat: number;
  lng: number;
}

export interface CambioEstado {
  estado: EstadoPedido;
  fecha: string;
  usuario: string;
}

export interface Pedido {
  id: string;
  codigo: string;
  clienteId: string;
  clienteNombre: string;
  telefono: string;
  direccion: string;
  barrio: string;
  destino: Coordenada;
  items: ItemPedido[];
  subtotal: number;
  domicilio: number;
  total: number;
  metodoPago: MetodoPago;
  estadoPago: EstadoPago;
  estado: EstadoPedido;
  origen: OrigenPedido;
  notas?: string;
  referenciaPago?: string;
  tarjetaPago?: string;
  repartidorId?: string;
  repartidorNombre?: string;
  ubicacionRepartidor?: Coordenada & { actualizado: string };
  historial: CambioEstado[];
  fecha: string;
}

export type TipoMovimiento = 'CARGO' | 'ABONO';

export interface MovimientoFiado {
  id: string;
  clienteId: string;
  tipo: TipoMovimiento;
  valor: number;
  descripcion: string;
  pedidoId?: string;
  fecha: string;
}

export type Frecuencia = 'SEMANAL' | 'QUINCENAL' | 'MENSUAL';

export interface PedidoRecurrente {
  id: string;
  clienteId: string;
  nombre: string;
  items: ItemPedido[];
  frecuencia: Frecuencia;
  dia: string;
  activo: boolean;
}

export type AutorMensaje = 'CLIENTE' | 'IA' | 'ASESOR';
export type EstadoConversacion = 'IA' | 'ESPERANDO_ASESOR' | 'CON_ASESOR' | 'CERRADA';

export interface Mensaje {
  id: string;
  autor: AutorMensaje;
  texto: string;
  fecha: string;
}

export interface Conversacion {
  id: string;
  clienteId: string;
  clienteNombre: string;
  estado: EstadoConversacion;
  asesorId?: string;
  asesorNombre?: string;
  mensajes: Mensaje[];
  actualizada: string;
}

export interface BaseDatos {
  version: number;
  usuarios: Usuario[];
  categorias: Categoria[];
  productos: Producto[];
  pedidos: Pedido[];
  movimientos: MovimientoFiado[];
  recurrentes: PedidoRecurrente[];
  conversaciones: Conversacion[];
}

export interface Sesion {
  token: string;
  usuario: Omit<Usuario, 'clave'>;
}
