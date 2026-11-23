import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject } from '@angular/core';
import { Observable, map, tap } from 'rxjs';
import { CONFIG } from '../config';
import { Frecuencia, ItemPedido, Pedido, PedidoRecurrente } from '../models/models';
import { AlmacenService, idsATexto } from './almacen.service';
import { AuthService } from './auth.service';

@Injectable({ providedIn: 'root' })
export class RecurrenteService {
  private http = inject(HttpClient);
  private almacen = inject(AlmacenService);
  private auth = inject(AuthService);
  private api = CONFIG.apiUrl + '/recurrentes';

  readonly misRecurrentes = computed(() => {
    const id = this.auth.usuario()?.id;
    return this.almacen.db().recurrentes.filter((r) => r.clienteId === id);
  });

  crear(nombre: string, items: ItemPedido[], frecuencia: Frecuencia, dia: string): Observable<PedidoRecurrente> {
    const cuerpo = { nombre, frecuencia, dia, items: items.map((i) => ({ productoId: Number(i.productoId), cantidad: i.cantidad })) };
    return this.http.post<PedidoRecurrente>(this.api, cuerpo).pipe(tap((r) => this.almacen.ponerRecurrente(r)));
  }

  alternar(id: string): Observable<PedidoRecurrente> {
    return this.http.put<PedidoRecurrente>(`${this.api}/${id}/activo`, {}).pipe(tap((r) => this.almacen.ponerRecurrente(r)));
  }

  pedirAhora(id: string): Observable<Pedido> {
    return this.http.post<Pedido>(`${this.api}/${id}/pedir`, {}).pipe(
      map((p) => idsATexto(p)),
      tap((p) => this.almacen.ponerPedido(p)),
    );
  }

  eliminar(id: string): Observable<void> {
    return this.http.delete<void>(`${this.api}/${id}`).pipe(tap(() => this.almacen.ponerRecurrente(null, id)));
  }
}
