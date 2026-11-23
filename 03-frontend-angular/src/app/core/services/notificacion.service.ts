import { HttpErrorResponse } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { TraduccionService } from '../i18n/traduccion.service';

export interface Aviso {
  id: number;
  tipo: 'exito' | 'error' | 'info';
  texto: string;
}

@Injectable({ providedIn: 'root' })
export class NotificacionService {
  private t = inject(TraduccionService);
  private contador = 0;
  readonly avisos = signal<Aviso[]>([]);

  exito(clave: string, params?: object): void {
    this.mostrar('exito', this.t.instant(clave, params));
  }

  info(clave: string, params?: object): void {
    this.mostrar('info', this.t.instant(clave, params));
  }

  error(err: unknown): void {
    if (err instanceof HttpErrorResponse) {
      const mensaje = err.error?.mensaje;
      if (mensaje) this.mostrar('error', mensaje);
      else if (err.status === 0) this.mostrar('error', this.t.instant('comun.sinServidor'));
      else this.mostrar('error', this.t.instant('comun.errorGeneral'));
      return;
    }
    const msg = err instanceof Error ? err.message : String(err);
    const [clave, valor] = msg.split('|');
    const traducido = this.t.instant(clave, { valor });
    this.mostrar('error', traducido === clave ? this.t.instant('comun.errorGeneral') : traducido);
  }

  cerrar(id: number): void {
    this.avisos.update((l) => l.filter((a) => a.id !== id));
  }

  private mostrar(tipo: Aviso['tipo'], texto: string): void {
    const id = ++this.contador;
    this.avisos.update((l) => [...l, { id, tipo, texto }]);
    setTimeout(() => this.cerrar(id), 4500);
  }
}
