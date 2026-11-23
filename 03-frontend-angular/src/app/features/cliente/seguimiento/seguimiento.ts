import { Component, computed, inject, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TraducirPipe } from '../../../core/i18n/traduccion.service';
import { DatePipe } from '@angular/common';
import { CONFIG } from '../../../core/config';
import { Coordenada } from '../../../core/models/models';
import { AuthService } from '../../../core/services/auth.service';
import { PedidoService } from '../../../core/services/pedido.service';
import { EstadoBadge } from '../../../shared/components/estado-badge';
import { LineaTiempo } from '../../../shared/components/linea-tiempo';
import { Mapa, PuntoMapa } from '../../../shared/components/mapa';
import { CopPipe } from '../../../shared/pipes/cop.pipe';

export function distanciaKm(a: Coordenada, b: Coordenada): number {
  const R = 6371;
  const rad = (g: number) => (g * Math.PI) / 180;
  const dLat = rad(b.lat - a.lat);
  const dLng = rad(b.lng - a.lng);
  const h = Math.sin(dLat / 2) ** 2 + Math.cos(rad(a.lat)) * Math.cos(rad(b.lat)) * Math.sin(dLng / 2) ** 2;
  return 2 * R * Math.asin(Math.sqrt(h));
}

@Component({
  selector: 'app-seguimiento',
  imports: [RouterLink, DatePipe, TraducirPipe, CopPipe, EstadoBadge, LineaTiempo, Mapa],
  templateUrl: './seguimiento.html',
})
export class Seguimiento {
  readonly id = input.required<string>();
  private pedidos = inject(PedidoService);
  private auth = inject(AuthService);

  protected readonly pedido = computed(() => {
    const p = this.pedidos.porId(this.id());
    return p && p.clienteId === this.auth.usuario()?.id ? p : undefined;
  });

  protected readonly puntos = computed<PuntoMapa[]>(() => {
    const p = this.pedido();
    if (!p) return [];
    const lista: PuntoMapa[] = [
      { id: 'tienda', icono: '🏪', etiqueta: CONFIG.nombreTienda, ...CONFIG.ubicacionTienda },
      { id: 'casa', icono: '🏠', etiqueta: p.direccion, ...p.destino },
    ];
    if (p.ubicacionRepartidor && p.estado === 'EN_CAMINO') {
      lista.push({ id: 'moto', icono: '🛵', etiqueta: p.repartidorNombre ?? 'Repartidor', lat: p.ubicacionRepartidor.lat, lng: p.ubicacionRepartidor.lng });
    }
    return lista;
  });

  protected readonly ruta = computed<Coordenada[]>(() => {
    const p = this.pedido();
    if (!p) return [];
    const moto = p.estado === 'EN_CAMINO' && p.ubicacionRepartidor;
    return moto ? [p.ubicacionRepartidor!, p.destino] : [CONFIG.ubicacionTienda, p.destino];
  });

  protected readonly minutosEstimados = computed(() => {
    const p = this.pedido();
    if (!p || p.estado !== 'EN_CAMINO') return null;
    const origen = p.ubicacionRepartidor ?? CONFIG.ubicacionTienda;
    return Math.max(1, Math.round((distanciaKm(origen, p.destino) / 20) * 60) + 2);
  });
}
