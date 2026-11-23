import { HttpClient } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { Observable, defer, delay, forkJoin, map, of, switchMap } from 'rxjs';
import { CONFIG } from '../config';
import { BaseDatos, Conversacion, Pedido, PedidoRecurrente, Rol } from '../models/models';

function vacio(): BaseDatos {
  return { version: 1, usuarios: [], categorias: [], productos: [], pedidos: [], movimientos: [], recurrentes: [], conversaciones: [] };
}

@Injectable({ providedIn: 'root' })
export class AlmacenService {
  private http = inject(HttpClient);

  readonly db = signal<BaseDatos>(vacio());

  private rol: Rol | null = null;
  private usuarioId: string | null = null;
  private reloj: ReturnType<typeof setInterval> | null = null;
  private espera: ReturnType<typeof setTimeout> | null = null;

  readonly conectado = signal(false);

  constructor() {
    setTimeout(() => this.recargar());
    this.conectarWebSocket();
  }

  private conectarWebSocket(): void {
    const url = CONFIG.apiUrl.replace(/^http/, 'ws').replace(/\/api$/, '/ws');
    const ws = new WebSocket(url);
    ws.onopen = () => {
      this.conectado.set(true);
      console.log('WebSocket conectado');
    };
    ws.onmessage = (evento) => {
      const aviso = JSON.parse(evento.data) as { tipo: string };
      console.log('Aviso del servidor:', aviso.tipo);
      if (this.espera) clearTimeout(this.espera);
      this.espera = setTimeout(() => this.recargar(), 200);
    };
    ws.onclose = () => {
      this.conectado.set(false);
      setTimeout(() => this.conectarWebSocket(), 5000);
    };
  }

  iniciarSesion(rol: Rol, usuarioId: string): void {
    this.rol = rol;
    this.usuarioId = usuarioId;
    this.recargar();
    if (this.reloj) clearInterval(this.reloj);
    this.reloj = setInterval(() => this.recargar(), 20000);
  }

  cerrarSesion(): void {
    this.rol = null;
    this.usuarioId = null;
    if (this.reloj) clearInterval(this.reloj);
    this.reloj = null;
    this.db.update((db) => ({ ...db, pedidos: [], usuarios: [], movimientos: [], recurrentes: [], conversaciones: [] }));
  }

  recargar(): void {
    this.traerDatos().subscribe({ error: () => {} });
  }

  ponerPedido(p: Pedido): void {
    this.db.update((db) => ({ ...db, pedidos: [p, ...db.pedidos.filter((x) => x.id !== p.id)] }));
  }

  ponerConversacion(c: Conversacion): void {
    const conv = idsATexto(c);
    this.db.update((db) => ({ ...db, conversaciones: [conv, ...db.conversaciones.filter((x) => x.id !== conv.id)] }));
  }

  ponerRecurrente(r: PedidoRecurrente | null, eliminadoId?: string): void {
    this.db.update((db) => {
      let lista = db.recurrentes.filter((x) => x.id !== (r ? String(r.id) : eliminadoId));
      if (r) lista = [...lista, idsATexto(r)].sort((a, b) => Number(a.id) - Number(b.id));
      return { ...db, recurrentes: lista };
    });
  }

  simularHttp<T>(operacion: () => T, ms = 250): Observable<T> {
    return defer(() => of(operacion())).pipe(delay(ms));
  }

  private traerDatos(): Observable<void> {
    const api = CONFIG.apiUrl;
    const peticiones: Record<string, Observable<any>> = {
      categorias: this.http.get(api + '/categorias'),
      productos: this.http.get(api + '/productos'),
    };
    if (this.rol === 'CLIENTE') {
      peticiones['pedidos'] = this.http.get(api + '/pedidos/mios');
      peticiones['yo'] = this.http.get(api + '/auth/yo');
      peticiones['fiado'] = this.http.get(api + '/fiado/' + this.usuarioId);
      peticiones['recurrentes'] = this.http.get(api + '/recurrentes/mios');
      peticiones['chat'] = this.http.get(api + '/chat/mia').pipe(map((c) => (c ? [c] : [])));
    }
    if (this.rol === 'TENDERO') {
      peticiones['pedidos'] = this.http.get(api + '/pedidos');
      peticiones['clientes'] = this.http.get(api + '/clientes');
      peticiones['repartidores'] = this.http.get(api + '/repartidores');
    }
    if (this.rol === 'REPARTIDOR') {
      peticiones['pedidos'] = this.http.get(api + '/pedidos/entregas');
      peticiones['yo'] = this.http.get(api + '/auth/yo');
    }
    if (this.rol === 'ASESOR') {
      peticiones['yo'] = this.http.get(api + '/auth/yo');
      peticiones['chat'] = this.http.get(api + '/chat');
      peticiones['clientes'] = this.http.get(api + '/clientes');
      peticiones['pedidos'] = this.http.get(api + '/pedidos');
    }

    return forkJoin(peticiones).pipe(
      switchMap((r) => {
        if (this.rol !== 'TENDERO' || r['clientes'].length === 0) return of({ r, fiados: [] as any[] });
        const cuentas = r['clientes'].map((c: any) => this.http.get(api + '/fiado/' + c.id));
        return forkJoin(cuentas).pipe(map((fiados) => ({ r, fiados: fiados as any[] })));
      }),
      map(({ r, fiados }) => {
        let usuarios: any[] = [];
        if (r['clientes']) usuarios = usuarios.concat(r['clientes']);
        if (r['repartidores']) usuarios = usuarios.concat(r['repartidores']);
        if (r['yo']) usuarios.push(r['yo']);

        let movimientos: any[] = [];
        if (r['fiado']) movimientos = r['fiado'].movimientos;
        for (const f of fiados) movimientos = movimientos.concat(f.movimientos);

        this.db.update((db) => ({
          ...db,
          categorias: idsATexto(r['categorias']),
          productos: idsATexto(r['productos']),
          pedidos: idsATexto(r['pedidos'] ?? []),
          usuarios: idsATexto(usuarios),
          movimientos: idsATexto(movimientos),
          recurrentes: idsATexto(r['recurrentes'] ?? []),
          conversaciones: idsATexto(r['chat'] ?? []),
        }));
      }),
    );
  }
}

const CAMPOS_ID = ['id', 'clienteId', 'productoId', 'categoriaId', 'repartidorId', 'pedidoId', 'asesorId'];

export function idsATexto<T>(dato: T): T {
  if (Array.isArray(dato)) return dato.map((d) => idsATexto(d)) as T;
  if (dato && typeof dato === 'object') {
    const copia: any = {};
    for (const [k, v] of Object.entries(dato)) {
      if (CAMPOS_ID.includes(k) && v !== null && v !== undefined) copia[k] = String(v);
      else copia[k] = idsATexto(v);
    }
    return copia;
  }
  return dato;
}
