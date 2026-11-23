import { DatePipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { TraducirPipe, TraduccionService } from '../../../core/i18n/traduccion.service';
import { CONFIG } from '../../../core/config';
import { EstadoPedido, Pedido } from '../../../core/models/models';
import { NotificacionService } from '../../../core/services/notificacion.service';
import { PedidoService } from '../../../core/services/pedido.service';
import { EstadoBadge } from '../../../shared/components/estado-badge';
import { Mapa, PuntoMapa } from '../../../shared/components/mapa';
import { CopPipe } from '../../../shared/pipes/cop.pipe';

type Filtro = 'ACTIVOS' | 'TODOS' | EstadoPedido;

@Component({
  selector: 'app-pedidos',
  imports: [DatePipe, TraducirPipe, CopPipe, EstadoBadge, Mapa],
  templateUrl: './pedidos.html',
})
export class Pedidos {
  protected pedidos = inject(PedidoService);
  private notif = inject(NotificacionService);
  private t = inject(TraduccionService);

  protected readonly filtros: Filtro[] = ['ACTIVOS', 'RECIBIDO', 'PREPARANDO', 'EN_CAMINO', 'ENTREGADO', 'CANCELADO', 'TODOS'];
  protected readonly filtro = signal<Filtro>('ACTIVOS');
  protected readonly vista = signal<'lista' | 'zonas'>('lista');
  protected readonly busqueda = signal('');

  protected readonly filtrados = computed(() => {
    const f = this.filtro();
    const q = this.busqueda().trim().toLowerCase();
    return this.pedidos.todos().filter((p) => {
      const pasaFiltro =
        f === 'TODOS' ? true : f === 'ACTIVOS' ? !['ENTREGADO', 'CANCELADO'].includes(p.estado) : p.estado === f;
      const pasaBusqueda = !q || p.codigo.toLowerCase().includes(q) || p.clienteNombre.toLowerCase().includes(q) || p.barrio.toLowerCase().includes(q);
      return pasaFiltro && pasaBusqueda;
    });
  });

  protected readonly zonas = computed(() => {
    const activos = this.pedidos.todos().filter((p) => p.estado === 'RECIBIDO' || p.estado === 'PREPARANDO');
    const grupos = new Map<string, Pedido[]>();
    for (const p of activos) grupos.set(p.barrio, [...(grupos.get(p.barrio) ?? []), p]);
    return [...grupos.entries()].map(([barrio, lista]) => ({ barrio, lista })).sort((a, b) => b.lista.length - a.lista.length);
  });

  protected readonly puntosZonas = computed<PuntoMapa[]>(() => [
    { id: 'tienda', icono: '🏪', etiqueta: CONFIG.nombreTienda, ...CONFIG.ubicacionTienda },
    ...this.zonas().flatMap((z) => z.lista.map((p) => ({ id: p.id, icono: '📦', etiqueta: `${p.codigo} · ${p.barrio}`, ...p.destino }))),
  ]);

  contar(f: Filtro): number {
    if (f === 'TODOS') return this.pedidos.todos().length;
    if (f === 'ACTIVOS') return this.pedidos.todos().filter((p) => !['ENTREGADO', 'CANCELADO'].includes(p.estado)).length;
    return this.pedidos.todos().filter((p) => p.estado === f).length;
  }

  avanzar(p: Pedido): void {
    const siguiente = this.pedidos.siguienteEstado(p.estado);
    if (!siguiente) return;
    if (siguiente === 'EN_CAMINO' && !p.repartidorId) {
      this.notif.error(new Error('tendero.asignaPrimero'));
      return;
    }
    this.pedidos.cambiarEstado(p.id, siguiente).subscribe({
      next: () => this.notif.exito('tendero.estadoCambiado', { codigo: p.codigo }),
      error: (e) => this.notif.error(e),
    });
  }

  cancelar(p: Pedido): void {
    if (!confirm(this.t.instant('tendero.confirmarCancelar', { codigo: p.codigo }))) return;
    this.pedidos.cambiarEstado(p.id, 'CANCELADO').subscribe({
      next: () => this.notif.info('tendero.cancelado', { codigo: p.codigo }),
      error: (e) => this.notif.error(e),
    });
  }

  asignar(p: Pedido, e: Event): void {
    const id = (e.target as HTMLSelectElement).value;
    this.pedidos.asignarRepartidor(p.id, id).subscribe(() => this.notif.exito('tendero.asignado'));
  }

  asignarZona(lista: Pedido[], e: Event): void {
    const id = (e.target as HTMLSelectElement).value;
    if (!id) return;
    for (const p of lista) this.pedidos.asignarRepartidor(p.id, id).subscribe();
    this.notif.exito('tendero.zonaAsignada', { n: lista.length });
  }

  buscar(e: Event): void {
    this.busqueda.set((e.target as HTMLInputElement).value);
  }
}
