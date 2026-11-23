import { Component, computed, input } from '@angular/core';
import { TraducirPipe } from '../../core/i18n/traduccion.service';
import { EstadoPedido } from '../../core/models/models';

const CLASES: Record<EstadoPedido, string> = {
  RECIBIDO: 'text-bg-secondary',
  PREPARANDO: 'text-bg-warning',
  EN_CAMINO: 'text-bg-info',
  ENTREGADO: 'text-bg-success',
  CANCELADO: 'text-bg-danger',
};

const ICONOS: Record<EstadoPedido, string> = {
  RECIBIDO: 'bi-inbox',
  PREPARANDO: 'bi-basket',
  EN_CAMINO: 'bi-scooter',
  ENTREGADO: 'bi-check2-circle',
  CANCELADO: 'bi-x-circle',
};

@Component({
  selector: 'app-estado-badge',
  imports: [TraducirPipe],
  template: `<span class="badge rounded-pill {{ clase() }}"><i class="bi {{ icono() }} me-1" aria-hidden="true"></i>{{ 'estados.' + estado() | translate }}</span>`,
})
export class EstadoBadge {
  readonly estado = input.required<EstadoPedido>();
  readonly clase = computed(() => CLASES[this.estado()]);
  readonly icono = computed(() => ICONOS[this.estado()]);
}
