import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject } from '@angular/core';
import { Observable, map, tap } from 'rxjs';
import { CONFIG } from '../config';
import { Producto } from '../models/models';
import { AlmacenService, idsATexto } from './almacen.service';

@Injectable({ providedIn: 'root' })
export class ProductoService {
  private almacen = inject(AlmacenService);
  private http = inject(HttpClient);
  private api = CONFIG.apiUrl + '/productos';

  readonly categorias = computed(() => this.almacen.db().categorias);
  readonly todos = computed(() => this.almacen.db().productos);
  readonly activos = computed(() => this.todos().filter((p) => p.activo));
  readonly bajoStock = computed(() => this.activos().filter((p) => p.stock <= p.stockMinimo));

  porId(id: string): Producto | undefined {
    return this.todos().find((p) => p.id === id);
  }

  nombreCategoria(id: string): string {
    return this.categorias().find((c) => c.id === id)?.nombre ?? '';
  }

  crear(datos: Omit<Producto, 'id'>): Observable<Producto> {
    return this.http.post<Producto>(this.api, this.paraBackend(datos)).pipe(
      map((p) => idsATexto(p)),
      tap(() => this.almacen.recargar()),
    );
  }

  subirFoto(id: string, foto: File): Observable<Producto> {
    const datos = new FormData();
    datos.append('foto', foto);
    return this.http.post<Producto>(`${this.api}/${id}/imagen`, datos).pipe(
      map((p) => idsATexto(p)),
      tap(() => this.almacen.recargar()),
    );
  }

  actualizar(producto: Producto): Observable<Producto> {
    return this.http.put<Producto>(`${this.api}/${producto.id}`, this.paraBackend(producto)).pipe(
      map((p) => idsATexto(p)),
      tap(() => this.almacen.recargar()),
    );
  }

  eliminar(id: string): Observable<void> {
    return this.http.delete<void>(`${this.api}/${id}`).pipe(tap(() => this.almacen.recargar()));
  }

  private paraBackend(p: Omit<Producto, 'id'>) {
    const { id, ...resto } = p as Producto;
    return { ...resto, categoriaId: Number(p.categoriaId) };
  }
}
