import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { CONFIG } from '../config';
import { AlmacenService } from './almacen.service';
import { AuthService } from './auth.service';

@Injectable({ providedIn: 'root' })
export class FiadoService {
  private almacen = inject(AlmacenService);
  private auth = inject(AuthService);
  private http = inject(HttpClient);

  readonly clientes = computed(() =>
    this.almacen.db().usuarios.filter((u) => u.rol === 'CLIENTE'),
  );

  readonly carteraTotal = computed(() =>
    this.clientes().reduce((s, c) => s + (c.saldoFiado ?? 0), 0),
  );

  readonly miCuenta = computed(() => {
    const id = this.auth.usuario()?.id;
    const u = this.almacen.db().usuarios.find((x) => x.id === id);
    const cupo = u?.cupoFiado ?? 0;
    const saldo = u?.saldoFiado ?? 0;
    return { cupo, saldo, disponible: Math.max(0, cupo - saldo) };
  });

  readonly misMovimientos = computed(() => this.movimientosDe(this.auth.usuario()?.id ?? ''));

  movimientosDe(clienteId: string) {
    return this.almacen
      .db()
      .movimientos.filter((m) => m.clienteId === clienteId)
      .sort((a, b) => b.fecha.localeCompare(a.fecha));
  }

  registrarAbono(clienteId: string, valor: number, descripcion: string): Observable<void> {
    return this.http
      .post(`${CONFIG.apiUrl}/fiado/${clienteId}/abonos`, { valor, descripcion })
      .pipe(map(() => this.almacen.recargar()));
  }

  cambiarCupo(clienteId: string, cupo: number): Observable<void> {
    return this.http
      .put(`${CONFIG.apiUrl}/clientes/${clienteId}/cupo`, { cupo })
      .pipe(map(() => this.almacen.recargar()));
  }
}
