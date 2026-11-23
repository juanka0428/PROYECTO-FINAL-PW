import {
  AfterViewInit, Component, ElementRef, OnDestroy, effect, input, viewChild,
} from '@angular/core';
import * as L from 'leaflet';
import { Coordenada } from '../../core/models/models';

export interface PuntoMapa extends Coordenada {
  id: string;
  icono: string;
  etiqueta: string;
}

@Component({
  selector: 'app-mapa',
  template: `<div #contenedor class="mapa" role="region" [attr.aria-label]="descripcion()"></div>`,
  styles: `
    :host { display: block; }
    .mapa { height: var(--alto, 320px); border-radius: var(--tc-radio); border: 1px solid var(--tc-borde); z-index: 0; }
  `,
})
export class Mapa implements AfterViewInit, OnDestroy {
  readonly puntos = input<PuntoMapa[]>([]);
  readonly ruta = input<Coordenada[]>([]);
  readonly descripcion = input('Mapa');
  readonly ajustar = input(true);

  private contenedor = viewChild.required<ElementRef<HTMLDivElement>>('contenedor');
  private mapa?: L.Map;
  private marcadores = new Map<string, L.Marker>();
  private linea?: L.Polyline;
  private primeraVez = true;

  constructor() {
    effect(() => {
      const puntos = this.puntos();
      const ruta = this.ruta();
      if (this.mapa) this.dibujar(puntos, ruta);
    });
  }

  ngAfterViewInit(): void {
    this.mapa = L.map(this.contenedor().nativeElement, { scrollWheelZoom: false }).setView([2.9345, -75.2809], 15);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>',
    }).addTo(this.mapa);
    this.dibujar(this.puntos(), this.ruta());
    setTimeout(() => this.mapa?.invalidateSize(), 150);
  }

  ngOnDestroy(): void {
    this.mapa?.remove();
  }

  private dibujar(puntos: PuntoMapa[], ruta: Coordenada[]): void {
    const mapa = this.mapa!;
    const ids = new Set(puntos.map((p) => p.id));
    for (const [id, m] of this.marcadores) {
      if (!ids.has(id)) {
        m.remove();
        this.marcadores.delete(id);
      }
    }
    for (const p of puntos) {
      const existente = this.marcadores.get(p.id);
      if (existente) {
        existente.setLatLng([p.lat, p.lng]);
      } else {
        const icono = L.divIcon({
          className: 'marcador-mapa',
          html: `<span aria-hidden="true">${p.icono}</span>`,
          iconSize: [38, 38],
          iconAnchor: [19, 19],
        });
        const m = L.marker([p.lat, p.lng], { icon: icono, title: p.etiqueta, alt: p.etiqueta, keyboard: true })
          .bindTooltip(p.etiqueta)
          .addTo(mapa);
        this.marcadores.set(p.id, m);
      }
    }
    this.linea?.remove();
    if (ruta.length > 1) {
      this.linea = L.polyline(ruta.map((c) => [c.lat, c.lng] as L.LatLngTuple), {
        color: '#1f7a4d', weight: 4, dashArray: '8 8', opacity: 0.8,
      }).addTo(mapa);
    }
    if (puntos.length && (this.ajustar() || this.primeraVez)) {
      const limites = L.latLngBounds(puntos.map((p) => [p.lat, p.lng] as L.LatLngTuple));
      mapa.fitBounds(limites, { padding: [40, 40], maxZoom: 16 });
      this.primeraVez = false;
    }
  }
}
