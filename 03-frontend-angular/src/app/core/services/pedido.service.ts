import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject } from '@angular/core';
import { Observable, map, tap } from 'rxjs';
import { CONFIG } from '../config';
import {
  Coordenada, EstadoPedido, ItemPedido, MetodoPago, OrigenPedido, Pedido,
} from '../models/models';
import { AlmacenService, idsATexto } from './almacen.service';
import { AuthService } from './auth.service';

export interface NuevoPedido {
  items: ItemPedido[];
  direccion: string;
  barrio: string;
  telefono: string;
  metodoPago: MetodoPago;
  origen: OrigenPedido;
  notas?: string;
  pago?: { titular: string; numero: string; vence: string; cvc: string };
}

export const FLUJO_ESTADOS: EstadoPedido[] = ['RECIBIDO', 'PREPARANDO', 'EN_CAMINO', 'ENTREGADO'];

@Injectable({ providedIn: 'root' })
export class PedidoService {
  private almacen = inject(AlmacenService);
  private auth = inject(AuthService);
  private http = inject(HttpClient);
  private api = CONFIG.apiUrl + '/pedidos';

  readonly todos = computed(() =>
    [...this.almacen.db().pedidos].sort((a, b) => b.fecha.localeCompare(a.fecha)),
  );

  readonly misPedidos = computed(() => {
    const id = this.auth.usuario()?.id;
    return this.todos().filter((p) => p.clienteId === id);
  });

  readonly misEntregas = computed(() => {
    const id = this.auth.usuario()?.id;
    return this.todos().filter((p) => p.repartidorId === id);
  });

  readonly repartidores = computed(() =>
    this.almacen.db().usuarios.filter((u) => u.rol === 'REPARTIDOR' && u.activo),
  );

  porId(id: string): Pedido | undefined {
    return this.almacen.db().pedidos.find((p) => p.id === id);
  }

  crear(datos: NuevoPedido): Observable<Pedido> {
    const cuerpo = {
      direccion: datos.direccion,
      barrio: datos.barrio,
      telefono: datos.telefono,
      notas: datos.notas,
      metodoPago: datos.metodoPago,
      origen: datos.origen,
      items: datos.items.map((i) => ({ productoId: Number(i.productoId), cantidad: i.cantidad })),
      pago: datos.metodoPago === 'EN_LINEA' ? datos.pago : null,
    };
    return this.http.post<Pedido>(this.api, cuerpo).pipe(
      map((p) => idsATexto(p)),
      tap((p) => this.listo(p)),
    );
  }

  cambiarEstado(id: string, estado: EstadoPedido): Observable<void> {
    return this.http.put<Pedido>(`${this.api}/${id}/estado`, { estado }).pipe(
      map((p) => this.listo(idsATexto(p))),
    );
  }

  asignarRepartidor(id: string, repartidorId: string): Observable<void> {
    return this.http.put<Pedido>(`${this.api}/${id}/repartidor`, { repartidorId: Number(repartidorId) }).pipe(
      map((p) => this.listo(idsATexto(p))),
    );
  }

  actualizarUbicacion(id: string, pos: Coordenada): void {
    const p = this.porId(id);
    if (p) this.almacen.ponerPedido({ ...p, ubicacionRepartidor: { ...pos, actualizado: new Date().toISOString() } });
    this.http.put(`${this.api}/${id}/ubicacion`, pos).subscribe({ error: () => {} });
  }

  siguienteEstado(estado: EstadoPedido): EstadoPedido | null {
    const i = FLUJO_ESTADOS.indexOf(estado);
    return i >= 0 && i < FLUJO_ESTADOS.length - 1 ? FLUJO_ESTADOS[i + 1] : null;
  }

  private listo(p: Pedido): void {
    this.almacen.ponerPedido(p);
    this.almacen.recargar();
  }
}
