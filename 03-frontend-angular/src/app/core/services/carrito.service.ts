import { Injectable, computed, effect, signal } from '@angular/core';
import { CONFIG } from '../config';
import { ItemPedido, OrigenPedido, Producto } from '../models/models';

const CLAVE = 'tc-carrito';

@Injectable({ providedIn: 'root' })
export class CarritoService {
  readonly items = signal<ItemPedido[]>(this.cargar());
  readonly origen = signal<OrigenPedido>('CATALOGO');

  readonly cantidadTotal = computed(() => this.items().reduce((s, i) => s + i.cantidad, 0));
  readonly subtotal = computed(() => this.items().reduce((s, i) => s + i.precio * i.cantidad, 0));
  readonly domicilio = computed(() =>
    this.subtotal() === 0 || this.subtotal() >= CONFIG.domicilioGratisDesde ? 0 : CONFIG.costoDomicilio,
  );
  readonly total = computed(() => this.subtotal() + this.domicilio());

  constructor() {
    effect(() => {
      try {
        localStorage.setItem(CLAVE, JSON.stringify(this.items()));
      } catch {  }
    });
  }

  agregar(p: Producto, cantidad = 1): void {
    this.items.update((lista) => {
      const existe = lista.find((i) => i.productoId === p.id);
      if (existe) {
        return lista.map((i) => (i.productoId === p.id ? { ...i, cantidad: i.cantidad + cantidad } : i));
      }
      return [...lista, { productoId: p.id, nombre: p.nombre, imagen: p.imagen, precio: p.precio, cantidad }];
    });
  }

  agregarVarios(items: ItemPedido[], origen: OrigenPedido): void {
    this.items.update((lista) => {
      const copia = [...lista];
      for (const nuevo of items) {
        const i = copia.findIndex((x) => x.productoId === nuevo.productoId);
        if (i >= 0) copia[i] = { ...copia[i], cantidad: copia[i].cantidad + nuevo.cantidad };
        else copia.push({ ...nuevo });
      }
      return copia;
    });
    this.origen.set(origen);
  }

  cambiarCantidad(productoId: string, cantidad: number): void {
    if (cantidad <= 0) return this.quitar(productoId);
    this.items.update((lista) => lista.map((i) => (i.productoId === productoId ? { ...i, cantidad } : i)));
  }

  cantidadDe(productoId: string): number {
    return this.items().find((i) => i.productoId === productoId)?.cantidad ?? 0;
  }

  quitar(productoId: string): void {
    this.items.update((lista) => lista.filter((i) => i.productoId !== productoId));
  }

  vaciar(): void {
    this.items.set([]);
    this.origen.set('CATALOGO');
  }

  private cargar(): ItemPedido[] {
    try {
      const items: ItemPedido[] = JSON.parse(localStorage.getItem(CLAVE) ?? '[]');
      return items.filter((i) => /^\d+$/.test(String(i.productoId)));
    } catch {
      return [];
    }
  }
}
