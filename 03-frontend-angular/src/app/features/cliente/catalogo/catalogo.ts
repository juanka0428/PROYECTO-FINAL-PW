import { FotoProducto } from '../../../shared/components/foto-producto';
import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TraducirPipe } from '../../../core/i18n/traduccion.service';
import { Producto } from '../../../core/models/models';
import { AuthService } from '../../../core/services/auth.service';
import { CarritoService } from '../../../core/services/carrito.service';
import { NotificacionService } from '../../../core/services/notificacion.service';
import { ProductoService } from '../../../core/services/producto.service';
import { CopPipe } from '../../../shared/pipes/cop.pipe';

@Component({
  selector: 'app-catalogo',
  imports: [FotoProducto, RouterLink, TraducirPipe, CopPipe],
  templateUrl: './catalogo.html',
  styleUrl: './catalogo.css',
})
export class Catalogo {
  protected productos = inject(ProductoService);
  protected carrito = inject(CarritoService);
  protected auth = inject(AuthService);
  private notif = inject(NotificacionService);

  protected readonly busqueda = signal('');
  protected readonly categoria = signal<string | null>(null);

  protected readonly filtrados = computed(() => {
    const q = this.busqueda().trim().toLowerCase();
    const c = this.categoria();
    return this.productos.activos().filter(
      (p) => (!c || p.categoriaId === c) && (!q || p.nombre.toLowerCase().includes(q) || p.palabrasClave.some((k) => k.includes(q))),
    );
  });

  agregar(p: Producto): void {
    this.carrito.agregar(p);
    this.notif.exito('catalogo.agregado', { producto: p.nombre });
  }

  buscar(e: Event): void {
    this.busqueda.set((e.target as HTMLInputElement).value);
  }
}
