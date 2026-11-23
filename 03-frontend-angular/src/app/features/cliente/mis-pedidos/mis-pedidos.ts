import { DatePipe } from '@angular/common';
import { FotoProducto } from '../../../shared/components/foto-producto';
import { Component, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { TraducirPipe } from '../../../core/i18n/traduccion.service';
import { Pedido } from '../../../core/models/models';
import { CarritoService } from '../../../core/services/carrito.service';
import { NotificacionService } from '../../../core/services/notificacion.service';
import { PedidoService } from '../../../core/services/pedido.service';
import { EstadoBadge } from '../../../shared/components/estado-badge';
import { CopPipe } from '../../../shared/pipes/cop.pipe';

@Component({
  selector: 'app-mis-pedidos',
  imports: [FotoProducto, RouterLink, DatePipe, TraducirPipe, CopPipe, EstadoBadge],
  templateUrl: './mis-pedidos.html',
})
export class MisPedidos {
  protected pedidos = inject(PedidoService);
  private carrito = inject(CarritoService);
  private notif = inject(NotificacionService);
  private router = inject(Router);

  repetir(p: Pedido): void {
    this.carrito.agregarVarios(p.items, 'CATALOGO');
    this.notif.exito('misPedidos.repetido', { codigo: p.codigo });
    this.router.navigate(['/carrito']);
  }

  activo(p: Pedido): boolean {
    return p.estado !== 'ENTREGADO' && p.estado !== 'CANCELADO';
  }
}
