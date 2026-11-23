import { Component, computed, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { TraducirPipe } from '../../../core/i18n/traduccion.service';
import { PedidoService } from '../../../core/services/pedido.service';

@Component({
  selector: 'app-panel',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, TraducirPipe],
  template: `
    <div class="container-fluid container-lg py-4">
      <div class="row g-4">
        <aside class="col-lg-3 col-xl-2">
          <nav class="nav nav-pills flex-lg-column gap-1 menu-panel" [attr.aria-label]="'tendero.menu' | translate">
            @for (e of enlaces; track e.ruta) {
              <a class="nav-link" [routerLink]="e.ruta" routerLinkActive="active" [routerLinkActiveOptions]="{ exact: e.ruta === '/tendero' }" ariaCurrentWhenActive="page">
                <i class="bi {{ e.icono }} me-2" aria-hidden="true"></i>{{ e.texto | translate }}
                @if (e.ruta === '/tendero/pedidos' && nuevos() > 0) { <span class="badge bg-acento ms-1">{{ nuevos() }}</span> }
              </a>
            }
          </nav>
        </aside>
        <section class="col-lg-9 col-xl-10">
          <router-outlet />
        </section>
      </div>
    </div>
  `,
  styles: `
    .menu-panel { overflow-x: auto; flex-wrap: nowrap; }
    .menu-panel .nav-link { white-space: nowrap; }
    @media (min-width: 992px) { .menu-panel { position: sticky; top: 1rem; } }
  `,
})
export class Panel {
  private pedidos = inject(PedidoService);
  protected readonly nuevos = computed(() => this.pedidos.todos().filter((p) => p.estado === 'RECIBIDO').length);
  protected readonly enlaces = [
    { ruta: '/tendero', texto: 'tendero.resumen', icono: 'bi-speedometer2' },
    { ruta: '/tendero/pedidos', texto: 'tendero.pedidos', icono: 'bi-receipt' },
    { ruta: '/tendero/productos', texto: 'tendero.productos', icono: 'bi-box-seam' },
    { ruta: '/tendero/clientes', texto: 'tendero.clientes', icono: 'bi-people' },
    { ruta: '/tendero/reportes', texto: 'tendero.reportes', icono: 'bi-bar-chart' },
  ];
}
