import { Component, inject } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';
import { TraducirPipe } from './core/i18n/traduccion.service';
import { CONFIG } from './core/config';
import { AccesibilidadService } from './core/services/accesibilidad.service';
import { AuthService } from './core/services/auth.service';
import { Avisos } from './shared/components/avisos';
import { BarraAccesibilidad } from './shared/components/barra-accesibilidad';
import { Encabezado } from './shared/components/encabezado';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, TraducirPipe, Encabezado, BarraAccesibilidad, Avisos],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  protected readonly config = CONFIG;
  protected readonly anio = new Date().getFullYear();
  protected auth = inject(AuthService);

  constructor() {
    inject(AccesibilidadService);
  }
}
