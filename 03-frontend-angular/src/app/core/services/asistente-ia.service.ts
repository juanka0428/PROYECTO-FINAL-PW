import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { CONFIG } from '../config';
import { ItemPedido, Producto } from '../models/models';
import { AlmacenService } from './almacen.service';
import { PedidoService } from './pedido.service';
import { ProductoService } from './producto.service';

export interface ResultadoInterpretacion {
  items: ItemPedido[];
  noReconocidos: string[];
  usoUltimoPedido: boolean;
}

const NUMEROS: Record<string, number> = {
  un: 1, una: 1, uno: 1, dos: 2, tres: 3, cuatro: 4, cinco: 5, seis: 6,
  siete: 7, ocho: 8, nueve: 9, diez: 10, par: 2,
};

const PALABRAS_VACIAS = new Set([
  'quiero', 'necesito', 'me', 'manda', 'mandame', 'envia', 'enviame', 'regalame', 'porfa', 'por', 'favor',
  'de', 'del', 'la', 'el', 'los', 'las', 'unos', 'unas', 'para', 'hoy', 'tambien', 'y', 'con', 'mas',
  'buenas', 'buenos', 'dias', 'tardes', 'noches', 'hola', 'gracias', 'pero', 'lo', 'mismo', 'semana', 'pasada', 'siempre',
]);

@Injectable({ providedIn: 'root' })
export class AsistenteIaService {
  private productos = inject(ProductoService);
  private pedidos = inject(PedidoService);
  private almacen = inject(AlmacenService);
  private http = inject(HttpClient);

  interpretar(texto: string): Observable<ResultadoInterpretacion> {
    return this.almacen.simularHttp(() => this.interpretarLocal(texto), 900);
  }

  leerImagen(archivo: File): Observable<string> {
    const datos = new FormData();
    datos.append('foto', archivo);
    return this.http.post<{ texto: string }>(CONFIG.apiUrl + '/ia/foto', datos).pipe(map((r) => r.texto));
  }

  interpretarLocal(texto: string): ResultadoInterpretacion {
    const limpio = normalizar(texto);
    let items: ItemPedido[] = [];
    const noReconocidos: string[] = [];
    let usoUltimoPedido = false;

    if (/(lo mismo|lo de siempre|repetir|semana pasada|ultimo pedido)/.test(limpio)) {
      const ultimo = this.pedidos.misPedidos()[0];
      if (ultimo) {
        items = ultimo.items.map((i) => ({ ...i }));
        usoUltimoPedido = true;
      }
    }

    const trozos = limpio
      .split(/,|;|\n|\.|\by\b|\bcon\b|\bmas\b|\btambien\b|\bpero\b/)
      .map((t) => t.trim())
      .filter(Boolean);

    for (const trozo of trozos) {
      if (trozo.startsWith('sin ')) {
        const p = this.buscarProducto(trozo.slice(4));
        if (p) items = items.filter((i) => i.productoId !== p.id);
        continue;
      }
      const producto = this.buscarProducto(trozo);
      if (producto) {
        let cantidad = this.buscarCantidad(trozo);
        cantidad = this.pasarAPaquetes(producto, cantidad, trozo.includes('docena'));
        const existe = items.find((i) => i.productoId === producto.id);
        if (existe) existe.cantidad = usoUltimoPedido ? cantidad : existe.cantidad + cantidad;
        else items.push({ productoId: producto.id, nombre: producto.nombre, imagen: producto.imagen, precio: producto.precio, cantidad });
      } else {
        const palabras = trozo.split(/\s+/).filter((w) => !PALABRAS_VACIAS.has(w) && !(w in NUMEROS) && !/^\d+$/.test(w));
        if (palabras.length) noReconocidos.push(palabras.join(' '));
      }
    }
    return { items, noReconocidos, usoUltimoPedido };
  }

  canastaPorPresupuesto(presupuesto: number): ItemPedido[] {
    const prioridad = ['p7', 'p1', 'p2', 'p8', 'p4', 'p13', 'p17', 'p19', 'p18', 'p10', 'p9', 'p11', 'p5', 'p3'];
    const resultado: ItemPedido[] = [];
    let total = 0;
    for (const id of prioridad) {
      const p = this.productos.porId(id);
      if (p && p.activo && p.stock > 0 && total + p.precio <= presupuesto) {
        resultado.push({ productoId: p.id, nombre: p.nombre, imagen: p.imagen, precio: p.precio, cantidad: 1 });
        total += p.precio;
      }
    }
    return resultado;
  }

  private buscarProducto(trozo: string): Producto | undefined {
    const palabras = trozo.split(/\s+/);
    return this.productos.activos().find((p) =>
      p.palabrasClave.some((clave) => {
        const c = normalizar(clave);
        return palabras.some((w) => w === c || w === c + 's' || w === c + 'es' || w.replace(/s$/, '') === c);
      }),
    );
  }

  private buscarCantidad(trozo: string): number {
    if (trozo.includes('media docena')) return 6;
    let cantidad = 1;
    const digito = trozo.match(/\b(\d{1,2})\b/);
    if (digito) {
      cantidad = Math.max(1, parseInt(digito[1], 10));
    } else {
      for (const w of trozo.split(/\s+/)) {
        if (w in NUMEROS) {
          cantidad = NUMEROS[w];
          break;
        }
      }
    }
    if (trozo.includes('docena')) cantidad = cantidad * 12;
    return cantidad;
  }

  private pasarAPaquetes(producto: Producto, cantidad: number, esDocena: boolean): number {
    const partes = producto.nombre.split(' x ');
    if (partes.length < 2) return cantidad;
    const porPaquete = parseInt(partes[1], 10);
    if (!porPaquete) return cantidad;
    if (esDocena) return Math.ceil(cantidad / porPaquete);
    if (cantidad >= porPaquete && cantidad % porPaquete == 0) return cantidad / porPaquete;
    return cantidad;
  }
}

export function normalizar(texto: string): string {
  return texto
    .toLowerCase()
    .normalize('NFD')
    .replace(/[̀-ͯ]/g, '')
    .replace(/[¿?¡!"()]/g, ' ')
    .replace(/\s+/g, ' ')
    .trim();
}
