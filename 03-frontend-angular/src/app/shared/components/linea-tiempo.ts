import { DatePipe } from '@angular/common';
import { Component, computed, input } from '@angular/core';
import { TraducirPipe } from '../../core/i18n/traduccion.service';
import { Pedido } from '../../core/models/models';
import { FLUJO_ESTADOS } from '../../core/services/pedido.service';

@Component({
  selector: 'app-linea-tiempo',
  imports: [TraducirPipe, DatePipe],
  template: `
    <ol class="linea-tiempo list-unstyled mb-0" [attr.aria-label]="'seguimiento.pasos' | translate">
      @for (paso of pasos(); track paso.estado) {
        <li [class.hecho]="paso.fecha" [class.actual]="paso.actual" [attr.aria-current]="paso.actual ? 'step' : null">
          <span class="punto" aria-hidden="true"><i class="bi" [class.bi-check-lg]="paso.fecha" [class.bi-circle]="!paso.fecha"></i></span>
          <div>
            <strong>{{ 'estados.' + paso.estado | translate }}</strong>
            <div class="small text-body-secondary">
              @if (paso.fecha) { {{ paso.fecha | date: 'short' }} } @else { {{ 'seguimiento.pendiente' | translate }} }
            </div>
          </div>
        </li>
      }
    </ol>
  `,
  styles: `
    .linea-tiempo li { display: flex; gap: .75rem; position: relative; padding-bottom: 1rem; }
    .linea-tiempo li:not(:last-child)::before { content: ''; position: absolute; left: .85rem; top: 1.8rem; bottom: 0; border-left: 2px dashed var(--tc-borde); }
    .linea-tiempo li.hecho:not(:last-child)::before { border-left: 2px solid var(--tc-primario); }
    .punto { flex: 0 0 1.75rem; height: 1.75rem; border-radius: 50%; display: grid; place-items: center; background: var(--tc-fondo-suave); border: 2px solid var(--tc-borde); color: var(--tc-texto-suave); }
    .hecho .punto { background: var(--tc-primario); border-color: var(--tc-primario); color: #fff; }
    .actual strong { color: var(--tc-primario); }
  `,
})
export class LineaTiempo {
  readonly pedido = input.required<Pedido>();
  readonly pasos = computed(() => {
    const p = this.pedido();
    const estados = p.estado === 'CANCELADO' ? [...FLUJO_ESTADOS.slice(0, 1), 'CANCELADO' as const] : FLUJO_ESTADOS;
    return estados.map((estado) => {
      const registro = [...p.historial].reverse().find((h) => h.estado === estado);
      return { estado, fecha: registro?.fecha ?? null, actual: p.estado === estado };
    });
  });
}
