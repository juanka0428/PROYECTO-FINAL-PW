import { DatePipe } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { TraducirPipe, TraduccionService } from '../../../core/i18n/traduccion.service';
import { CONFIG } from '../../../core/config';
import { NotificacionService } from '../../../core/services/notificacion.service';
import { PedidoService } from '../../../core/services/pedido.service';
import { CopPipe } from '../../../shared/pipes/cop.pipe';

interface Barra { etiqueta: string; valor: number; detalle?: string }

@Component({
  selector: 'app-reportes',
  imports: [DatePipe, TraducirPipe, CopPipe],
  templateUrl: './reportes.html',
  styleUrl: './reportes.css',
})
export class Reportes {
  private pedidos = inject(PedidoService);
  private t = inject(TraduccionService);
  private http = inject(HttpClient);
  private notif = inject(NotificacionService);

  protected readonly descargando = signal<'pdf' | 'excel' | null>(null);

  protected readonly dias = signal(7);
  protected readonly verTabla = signal(false);
  protected readonly hoy = new Date();

  private readonly enRango = computed(() => {
    const desde = new Date();
    desde.setHours(0, 0, 0, 0);
    desde.setDate(desde.getDate() - (this.dias() - 1));
    return this.pedidos.todos().filter((p) => p.estado !== 'CANCELADO' && new Date(p.fecha) >= desde);
  });

  protected readonly totalVentas = computed(() => this.enRango().reduce((s, p) => s + p.total, 0));
  protected readonly cantidad = computed(() => this.enRango().length);
  protected readonly ticket = computed(() => (this.cantidad() ? Math.round(this.totalVentas() / this.cantidad()) : 0));
  protected readonly vendidoFiado = computed(() => this.enRango().filter((p) => p.metodoPago === 'FIADO').reduce((s, p) => s + p.total, 0));

  protected readonly porDia = computed<Barra[]>(() => {
    const lista: Barra[] = [];
    for (let i = this.dias() - 1; i >= 0; i--) {
      const d = new Date();
      d.setDate(d.getDate() - i);
      const clave = d.toDateString();
      const delDia = this.enRango().filter((p) => new Date(p.fecha).toDateString() === clave);
      lista.push({
        etiqueta: d.toISOString(),
        valor: delDia.reduce((s, p) => s + p.total, 0),
        detalle: String(delDia.length),
      });
    }
    return lista;
  });
  protected readonly maxDia = computed(() => Math.max(1, ...this.porDia().map((b) => b.valor)));

  protected readonly topProductos = computed<Barra[]>(() => {
    const mapa = new Map<string, { unidades: number; valor: number }>();
    for (const p of this.enRango()) {
      for (const i of p.items) {
        const a = mapa.get(i.nombre) ?? { unidades: 0, valor: 0 };
        mapa.set(i.nombre, { unidades: a.unidades + i.cantidad, valor: a.valor + i.cantidad * i.precio });
      }
    }
    return [...mapa.entries()]
      .map(([etiqueta, v]) => ({ etiqueta, valor: v.valor, detalle: String(v.unidades) }))
      .sort((a, b) => b.valor - a.valor)
      .slice(0, 8);
  });

  protected readonly porMetodo = computed(() => this.agrupar((p) => 'pago.' + p.metodoPago));
  protected readonly porOrigen = computed(() => this.agrupar((p) => 'origen.' + p.origen));

  max(lista: Barra[]): number {
    return Math.max(1, ...lista.map((b) => b.valor));
  }

  exportarCsv(): void {
    const filas = [
      ['Codigo', 'Fecha', 'Cliente', 'Barrio', 'Metodo de pago', 'Origen', 'Estado', 'Total'],
      ...this.enRango().map((p) => [
        p.codigo, new Date(p.fecha).toLocaleString('es-CO'), p.clienteNombre, p.barrio,
        this.t.instant('pago.' + p.metodoPago), this.t.instant('origen.' + p.origen), this.t.instant('estados.' + p.estado), String(p.total),
      ]),
    ];
    const csv = filas.map((f) => f.map((c) => `"${c.replace(/"/g, '""')}"`).join(';')).join('\n');
    const blob = new Blob(['﻿' + csv], { type: 'text/csv;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `reporte-ventas-${new Date().toISOString().slice(0, 10)}.csv`;
    a.click();
    URL.revokeObjectURL(url);
  }

  descargar(tipo: 'pdf' | 'excel'): void {
    this.descargando.set(tipo);
    this.http.get(`${CONFIG.apiUrl}/reportes/ventas/${tipo}?dias=${this.dias()}`, { responseType: 'blob' }).subscribe({
      next: (archivo) => {
        const url = URL.createObjectURL(archivo);
        const a = document.createElement('a');
        a.href = url;
        a.download = `reporte-ventas-${new Date().toISOString().slice(0, 10)}.${tipo === 'pdf' ? 'pdf' : 'xlsx'}`;
        a.click();
        URL.revokeObjectURL(url);
        this.descargando.set(null);
      },
      error: (e) => {
        this.notif.error(e);
        this.descargando.set(null);
      },
    });
  }

  imprimir(): void {
    window.print();
  }

  private agrupar(clave: (p: ReturnType<PedidoService['todos']>[number]) => string): Barra[] {
    const mapa = new Map<string, { n: number; valor: number }>();
    for (const p of this.enRango()) {
      const k = clave(p);
      const a = mapa.get(k) ?? { n: 0, valor: 0 };
      mapa.set(k, { n: a.n + 1, valor: a.valor + p.total });
    }
    return [...mapa.entries()].map(([etiqueta, v]) => ({ etiqueta, valor: v.valor, detalle: String(v.n) })).sort((a, b) => b.valor - a.valor);
  }
}
