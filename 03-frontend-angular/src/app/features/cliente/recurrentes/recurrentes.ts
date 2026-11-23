import { FotoProducto } from '../../../shared/components/foto-producto';
import { Component, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { TraducirPipe } from '../../../core/i18n/traduccion.service';
import { PedidoRecurrente } from '../../../core/models/models';
import { CarritoService } from '../../../core/services/carrito.service';
import { NotificacionService } from '../../../core/services/notificacion.service';
import { RecurrenteService } from '../../../core/services/recurrente.service';
import { CopPipe } from '../../../shared/pipes/cop.pipe';

@Component({
  selector: 'app-recurrentes',
  imports: [FotoProducto, RouterLink, TraducirPipe, CopPipe],
  templateUrl: './recurrentes.html',
})
export class Recurrentes {
  protected recurrentes = inject(RecurrenteService);
  private carrito = inject(CarritoService);
  private notif = inject(NotificacionService);
  private router = inject(Router);

  total(r: PedidoRecurrente): number {
    return r.items.reduce((s, i) => s + i.precio * i.cantidad, 0);
  }

  pedirAhora(r: PedidoRecurrente): void {
    this.carrito.agregarVarios(r.items, 'RECURRENTE');
    this.router.navigate(['/checkout']);
  }

  alternar(r: PedidoRecurrente): void {
    this.recurrentes.alternar(r.id).subscribe(() =>
      this.notif.info(r.activo ? 'recurrentes.pausado' : 'recurrentes.activado'),
    );
  }

  eliminar(r: PedidoRecurrente): void {
    this.recurrentes.eliminar(r.id).subscribe(() => this.notif.info('recurrentes.eliminado'));
  }
}
