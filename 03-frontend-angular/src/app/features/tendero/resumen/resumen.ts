import { DatePipe } from '@angular/common';
import { FotoProducto } from '../../../shared/components/foto-producto';
import { Component, computed, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TraducirPipe } from '../../../core/i18n/traduccion.service';
import { AuthService } from '../../../core/services/auth.service';
import { FiadoService } from '../../../core/services/fiado.service';
import { PedidoService } from '../../../core/services/pedido.service';
import { ProductoService } from '../../../core/services/producto.service';
import { EstadoBadge } from '../../../shared/components/estado-badge';
import { CopPipe } from '../../../shared/pipes/cop.pipe';

@Component({
  selector: 'app-resumen',
  imports: [FotoProducto, RouterLink, DatePipe, TraducirPipe, CopPipe, EstadoBadge],
  templateUrl: './resumen.html',
})
export class Resumen {
  protected auth = inject(AuthService);
  protected pedidos = inject(PedidoService);
  protected productos = inject(ProductoService);
  protected fiado = inject(FiadoService);

  private deHoy = computed(() => {
    const hoy = new Date().toDateString();
    return this.pedidos.todos().filter((p) => new Date(p.fecha).toDateString() === hoy && p.estado !== 'CANCELADO');
  });

  protected readonly pedidosHoy = computed(() => this.deHoy().length);
  protected readonly ventasHoy = computed(() => this.deHoy().reduce((s, p) => s + p.total, 0));
  protected readonly pendientes = computed(() =>
    this.pedidos.todos().filter((p) => ['RECIBIDO', 'PREPARANDO', 'EN_CAMINO'].includes(p.estado)),
  );
  protected readonly clientesCerca = computed(() =>
    this.fiado.clientes().filter((c) => (c.cupoFiado ?? 0) > 0 && (c.saldoFiado ?? 0) / (c.cupoFiado ?? 1) >= 0.8),
  );
}
