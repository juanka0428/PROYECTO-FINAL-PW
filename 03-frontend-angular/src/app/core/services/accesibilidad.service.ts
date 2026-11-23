import { Injectable, effect, signal } from '@angular/core';

const CLAVE = 'tc-accesibilidad';

interface Preferencias {
  escala: number;
  altoContraste: boolean;
  reducirMovimiento: boolean;
}

@Injectable({ providedIn: 'root' })
export class AccesibilidadService {
  static readonly ESCALAS = [0.9, 1, 1.15, 1.3, 1.5];

  readonly escala = signal(1);
  readonly altoContraste = signal(false);
  readonly reducirMovimiento = signal(false);

  constructor() {
    const guardado = this.leer();
    if (guardado) {
      this.escala.set(guardado.escala);
      this.altoContraste.set(guardado.altoContraste);
      this.reducirMovimiento.set(guardado.reducirMovimiento);
    }
    effect(() => {
      const html = document.documentElement;
      html.style.setProperty('--tc-escala', String(this.escala()));
      html.classList.toggle('alto-contraste', this.altoContraste());
      html.classList.toggle('sin-movimiento', this.reducirMovimiento());
      try {
        localStorage.setItem(CLAVE, JSON.stringify({
          escala: this.escala(), altoContraste: this.altoContraste(), reducirMovimiento: this.reducirMovimiento(),
        }));
      } catch {  }
    });
  }

  agrandar(): void {
    const e = AccesibilidadService.ESCALAS;
    const i = e.indexOf(this.escala());
    if (i < e.length - 1) this.escala.set(e[i + 1]);
  }

  achicar(): void {
    const e = AccesibilidadService.ESCALAS;
    const i = e.indexOf(this.escala());
    if (i > 0) this.escala.set(e[i - 1]);
  }

  restablecer(): void {
    this.escala.set(1);
    this.altoContraste.set(false);
    this.reducirMovimiento.set(false);
  }

  private leer(): Preferencias | null {
    try {
      return JSON.parse(localStorage.getItem(CLAVE) ?? 'null');
    } catch {
      return null;
    }
  }
}
