import { Routes } from '@angular/router';
import { invitadoGuard, rolGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'catalogo' },

  { path: 'login', canActivate: [invitadoGuard], loadComponent: () => import('./features/auth/login/login').then((m) => m.Login) },
  { path: 'registro', canActivate: [invitadoGuard], loadComponent: () => import('./features/auth/registro/registro').then((m) => m.Registro) },
  { path: 'auth/google', loadComponent: () => import('./features/auth/google/google').then((m) => m.Google) },
  { path: 'no-autorizado', loadComponent: () => import('./features/auth/no-autorizado/no-autorizado').then((m) => m.NoAutorizado) },
  { path: 'catalogo', loadComponent: () => import('./features/cliente/catalogo/catalogo').then((m) => m.Catalogo) },
  { path: 'carrito', loadComponent: () => import('./features/cliente/carrito/carrito').then((m) => m.Carrito) },

  { path: 'checkout', canActivate: [rolGuard('CLIENTE')], loadComponent: () => import('./features/cliente/checkout/checkout').then((m) => m.Checkout) },
  { path: 'pedido-inteligente', canActivate: [rolGuard('CLIENTE')], loadComponent: () => import('./features/cliente/pedido-inteligente/pedido-inteligente').then((m) => m.PedidoInteligente) },
  { path: 'mis-pedidos', canActivate: [rolGuard('CLIENTE')], loadComponent: () => import('./features/cliente/mis-pedidos/mis-pedidos').then((m) => m.MisPedidos) },
  { path: 'seguimiento/:id', canActivate: [rolGuard('CLIENTE')], loadComponent: () => import('./features/cliente/seguimiento/seguimiento').then((m) => m.Seguimiento) },
  { path: 'mi-fiado', canActivate: [rolGuard('CLIENTE')], loadComponent: () => import('./features/cliente/mi-fiado/mi-fiado').then((m) => m.MiFiado) },
  { path: 'recurrentes', canActivate: [rolGuard('CLIENTE')], loadComponent: () => import('./features/cliente/recurrentes/recurrentes').then((m) => m.Recurrentes) },
  { path: 'chat', canActivate: [rolGuard('CLIENTE')], loadComponent: () => import('./features/cliente/chat/chat').then((m) => m.Chat) },

  {
    path: 'tendero',
    canActivate: [rolGuard('TENDERO')],
    loadComponent: () => import('./features/tendero/panel/panel').then((m) => m.Panel),
    children: [
      { path: '', loadComponent: () => import('./features/tendero/resumen/resumen').then((m) => m.Resumen) },
      { path: 'pedidos', loadComponent: () => import('./features/tendero/pedidos/pedidos').then((m) => m.Pedidos) },
      { path: 'productos', loadComponent: () => import('./features/tendero/productos/productos').then((m) => m.Productos) },
      { path: 'clientes', loadComponent: () => import('./features/tendero/clientes/clientes').then((m) => m.Clientes) },
      { path: 'reportes', loadComponent: () => import('./features/tendero/reportes/reportes').then((m) => m.Reportes) },
    ],
  },

  { path: 'repartidor', canActivate: [rolGuard('REPARTIDOR')], loadComponent: () => import('./features/repartidor/entregas/entregas').then((m) => m.Entregas) },
  { path: 'asesor', canActivate: [rolGuard('ASESOR')], loadComponent: () => import('./features/asesor/bandeja/bandeja').then((m) => m.Bandeja) },

  { path: '**', redirectTo: 'catalogo' },
];
