import { Component, computed, inject, signal } from '@angular/core';
import { NavigationEnd, Router, RouterLink, RouterLinkActive } from '@angular/router';
import { TraducirPipe } from '../../core/i18n/traduccion.service';
import { filter } from 'rxjs';
import { CONFIG } from '../../core/config';
import { AuthService } from '../../core/services/auth.service';
import { CarritoService } from '../../core/services/carrito.service';
import { ChatService } from '../../core/services/chat.service';

interface Enlace {
  ruta: string;
  texto: string;
  icono: string;
  exacto?: boolean;
}

@Component({
  selector: 'app-encabezado',
  imports: [RouterLink, RouterLinkActive, TraducirPipe],
  templateUrl: './encabezado.html',
  styleUrl: './encabezado.css',
})
export class Encabezado {
  protected auth = inject(AuthService);
  protected carrito = inject(CarritoService);
  protected chat = inject(ChatService);
  protected readonly nombreApp = CONFIG.nombreApp;
  protected readonly menuAbierto = signal(false);

  protected readonly enlaces = computed<Enlace[]>(() => {
    switch (this.auth.rol()) {
      case 'CLIENTE':
        return [
          { ruta: '/catalogo', texto: 'nav.catalogo', icono: 'bi-shop' },
          { ruta: '/pedido-inteligente', texto: 'nav.pedidoInteligente', icono: 'bi-mic' },
          { ruta: '/mis-pedidos', texto: 'nav.misPedidos', icono: 'bi-bag-check' },
          { ruta: '/mi-fiado', texto: 'nav.miFiado', icono: 'bi-journal-text' },
          { ruta: '/recurrentes', texto: 'nav.recurrentes', icono: 'bi-arrow-repeat' },
          { ruta: '/chat', texto: 'nav.ayuda', icono: 'bi-chat-dots' },
        ];
      case 'TENDERO':
        return [{ ruta: '/tendero', texto: 'nav.panel', icono: 'bi-speedometer2' }];
      case 'REPARTIDOR':
        return [{ ruta: '/repartidor', texto: 'nav.entregas', icono: 'bi-scooter' }];
      case 'ASESOR':
        return [{ ruta: '/asesor', texto: 'nav.bandeja', icono: 'bi-headset' }];
      default:
        return [{ ruta: '/catalogo', texto: 'nav.catalogo', icono: 'bi-shop' }];
    }
  });

  protected readonly muestraCarrito = computed(() => !this.auth.rol() || this.auth.rol() === 'CLIENTE');

  constructor() {
    inject(Router).events.pipe(filter((e) => e instanceof NavigationEnd)).subscribe(() => this.menuAbierto.set(false));
  }
}
