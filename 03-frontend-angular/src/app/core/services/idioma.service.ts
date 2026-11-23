import { Injectable, inject, signal } from '@angular/core';
import { TraduccionService } from '../i18n/traduccion.service';

const CLAVE = 'tc-idioma';
export type Idioma = 'es' | 'en' | 'pt';

export const IDIOMAS: { codigo: Idioma; nombre: string }[] = [
  { codigo: 'es', nombre: 'Español' },
  { codigo: 'en', nombre: 'English' },
  { codigo: 'pt', nombre: 'Português' },
];

@Injectable({ providedIn: 'root' })
export class IdiomaService {
  private traduccion = inject(TraduccionService);
  readonly actual = signal<Idioma>(idiomaGuardado());

  iniciar(): Promise<void> {
    document.documentElement.lang = this.actual();
    return this.traduccion.cargar(this.actual());
  }

  cambiar(idioma: Idioma): void {
    if (idioma === this.actual()) return;
    try {
      localStorage.setItem(CLAVE, idioma);
    } catch {  }
    location.reload();
  }
}

export function idiomaGuardado(): Idioma {
  try {
    const v = localStorage.getItem(CLAVE);
    if (v === 'es' || v === 'en' || v === 'pt') return v;
  } catch {  }
  return 'es';
}
