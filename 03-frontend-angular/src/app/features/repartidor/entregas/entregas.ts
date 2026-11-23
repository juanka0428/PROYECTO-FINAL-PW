import { Component, OnDestroy, computed, inject, signal } from '@angular/core';
import { TraducirPipe, TraduccionService } from '../../../core/i18n/traduccion.service';
import { CONFIG } from '../../../core/config';
import { Pedido } from '../../../core/models/models';
import { NotificacionService } from '../../../core/services/notificacion.service';
import { PedidoService } from '../../../core/services/pedido.service';
import { EstadoBadge } from '../../../shared/components/estado-badge';
import { Mapa, PuntoMapa } from '../../../shared/components/mapa';
import { CopPipe } from '../../../shared/pipes/cop.pipe';

@Component({
  selector: 'app-entregas',
  imports: [TraducirPipe, CopPipe, EstadoBadge, Mapa],
  templateUrl: './entregas.html',
})
export class Entregas implements OnDestroy {
  protected pedidos = inject(PedidoService);
  private notif = inject(NotificacionService);
  private t = inject(TraduccionService);

  protected readonly activas = computed(() => this.pedidos.misEntregas().filter((p) => p.estado === 'PREPARANDO' || p.estado === 'EN_CAMINO' || p.estado === 'RECIBIDO'));
  protected readonly entregadasHoy = computed(() => {
    const hoy = new Date().toDateString();
    return this.pedidos.misEntregas().filter((p) => p.estado === 'ENTREGADO' && new Date(p.historial.at(-1)!.fecha).toDateString() === hoy);
  });

  protected readonly seleccionadaId = signal<string | null>(null);
  protected readonly seleccionada = computed(() => {
    const id = this.seleccionadaId() ?? this.activas()[0]?.id;
    return this.activas().find((p) => p.id === id) ?? null;
  });

  protected readonly compartiendo = signal<'no' | 'gps' | 'simulado'>('no');
  private watchId: number | null = null;
  private intervalo: ReturnType<typeof setInterval> | null = null;

  protected readonly puntos = computed<PuntoMapa[]>(() => {
    const p = this.seleccionada();
    if (!p) return [];
    const lista: PuntoMapa[] = [
      { id: 'tienda', icono: '🏪', etiqueta: CONFIG.nombreTienda, ...CONFIG.ubicacionTienda },
      { id: 'casa', icono: '🏠', etiqueta: `${p.clienteNombre} · ${p.direccion}`, ...p.destino },
    ];
    if (p.ubicacionRepartidor) lista.push({ id: 'yo', icono: '🛵', etiqueta: 'Yo', lat: p.ubicacionRepartidor.lat, lng: p.ubicacionRepartidor.lng });
    return lista;
  });

  protected readonly ruta = computed(() => {
    const p = this.seleccionada();
    return p ? [p.ubicacionRepartidor ?? CONFIG.ubicacionTienda, p.destino] : [];
  });

  ngOnDestroy(): void {
    this.detener();
  }

  enlaceNavegacion(p: Pedido): string {
    return `https://www.google.com/maps/dir/?api=1&destination=${p.destino.lat},${p.destino.lng}`;
  }

  iniciar(p: Pedido): void {
    this.pedidos.cambiarEstado(p.id, 'EN_CAMINO').subscribe({
      next: () => {
        this.seleccionadaId.set(p.id);
        this.notif.exito('repartidor.iniciada');
        this.compartirGps(p);
      },
      error: (e) => this.notif.error(e),
    });
  }

  compartirGps(p: Pedido): void {
    this.detener();
    if (!('geolocation' in navigator)) {
      this.notif.info('repartidor.sinGps');
      return;
    }
    this.watchId = navigator.geolocation.watchPosition(
      (pos) => {
        this.compartiendo.set('gps');
        this.pedidos.actualizarUbicacion(p.id, { lat: pos.coords.latitude, lng: pos.coords.longitude });
      },
      () => {
        this.detener();
        this.notif.info('repartidor.gpsDenegado');
      },
      { enableHighAccuracy: true, maximumAge: 5000 },
    );
  }

  simular(p: Pedido): void {
    this.detener();
    this.compartiendo.set('simulado');
    const inicio = CONFIG.ubicacionTienda;
    const fin = p.destino;
    let paso = 0;
    const pasos = 20;
    this.pedidos.actualizarUbicacion(p.id, inicio);
    this.intervalo = setInterval(() => {
      paso++;
      const t = paso / pasos;
      const desvio = Math.sin(t * Math.PI) * 0.0015;
      this.pedidos.actualizarUbicacion(p.id, {
        lat: inicio.lat + (fin.lat - inicio.lat) * t + desvio,
        lng: inicio.lng + (fin.lng - inicio.lng) * t,
      });
      if (paso >= pasos) this.detener();
    }, 2000);
  }

  detener(): void {
    if (this.watchId !== null) navigator.geolocation.clearWatch(this.watchId);
    if (this.intervalo) clearInterval(this.intervalo);
    this.watchId = null;
    this.intervalo = null;
    this.compartiendo.set('no');
  }

  entregar(p: Pedido): void {
    if (!confirm(this.t.instant('repartidor.confirmarEntrega', { codigo: p.codigo }))) return;
    this.pedidos.cambiarEstado(p.id, 'ENTREGADO').subscribe(() => {
      this.detener();
      this.seleccionadaId.set(null);
      this.notif.exito('repartidor.entregado', { codigo: p.codigo });
    });
  }
}
