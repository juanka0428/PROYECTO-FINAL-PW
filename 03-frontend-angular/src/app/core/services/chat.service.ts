import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject } from '@angular/core';
import { Observable, map, tap } from 'rxjs';
import { CONFIG } from '../config';
import { Conversacion } from '../models/models';
import { AlmacenService } from './almacen.service';
import { AuthService } from './auth.service';
import { NotificacionService } from './notificacion.service';

@Injectable({ providedIn: 'root' })
export class ChatService {
  private http = inject(HttpClient);
  private almacen = inject(AlmacenService);
  private auth = inject(AuthService);
  private notif = inject(NotificacionService);
  private api = CONFIG.apiUrl + '/chat';

  readonly todas = computed(() =>
    [...this.almacen.db().conversaciones].sort((a, b) => b.actualizada.localeCompare(a.actualizada)),
  );

  readonly miConversacion = computed(() => {
    const id = this.auth.usuario()?.id;
    return this.todas().find((c) => c.clienteId === id && c.estado !== 'CERRADA') ?? null;
  });

  readonly pendientes = computed(() => this.todas().filter((c) => c.estado === 'ESPERANDO_ASESOR').length);

  porId(id: string): Conversacion | undefined {
    return this.todas().find((c) => c.id === id);
  }

  enviarComoCliente(texto: string): Observable<void> {
    return this.http.post<Conversacion>(this.api + '/mensajes', { texto }).pipe(
      tap((c) => this.almacen.ponerConversacion(c)),
      map(() => undefined),
    );
  }

  pedirAsesor(): void {
    this.http.post<Conversacion>(`${this.api}/asesor`, {}).subscribe(this.guardar());
  }

  tomar(id: string): void {
    this.http.put<Conversacion>(`${this.api}/${id}/tomar`, {}).subscribe(this.guardar());
  }

  enviarComoAsesor(id: string, texto: string): void {
    this.http.post<Conversacion>(`${this.api}/${id}/mensajes`, { texto }).subscribe(this.guardar());
  }

  cerrar(id: string): void {
    this.http.put<Conversacion>(`${this.api}/${id}/cerrar`, {}).subscribe(this.guardar());
  }

  private guardar() {
    return {
      next: (c: Conversacion) => this.almacen.ponerConversacion(c),
      error: (e: unknown) => this.notif.error(e),
    };
  }
}
