import { FotoProducto } from '../../../shared/components/foto-producto';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { TraducirPipe } from '../../../core/i18n/traduccion.service';
import { CONFIG } from '../../../core/config';
import { Frecuencia } from '../../../core/models/models';
import { AuthService } from '../../../core/services/auth.service';
import { CarritoService } from '../../../core/services/carrito.service';
import { NotificacionService } from '../../../core/services/notificacion.service';
import { RecurrenteService } from '../../../core/services/recurrente.service';
import { CopPipe } from '../../../shared/pipes/cop.pipe';

@Component({
  selector: 'app-carrito',
  imports: [FotoProducto, RouterLink, FormsModule, TraducirPipe, CopPipe],
  templateUrl: './carrito.html',
})
export class Carrito {
  protected carrito = inject(CarritoService);
  protected auth = inject(AuthService);
  private recurrentes = inject(RecurrenteService);
  private notif = inject(NotificacionService);
  private router = inject(Router);
  protected readonly config = CONFIG;

  protected readonly guardando = signal(false);
  protected nombreRecurrente = '';
  protected frecuencia: Frecuencia = 'SEMANAL';
  protected dia = 'LUNES';
  protected readonly dias = ['LUNES', 'MARTES', 'MIERCOLES', 'JUEVES', 'VIERNES', 'SABADO', 'DOMINGO'];

  continuar(): void {
    if (this.auth.rol() === 'CLIENTE') this.router.navigate(['/checkout']);
    else this.router.navigate(['/login'], { queryParams: { volver: '/checkout' } });
  }

  guardarRecurrente(): void {
    const nombre = this.nombreRecurrente.trim();
    if (!nombre) return;
    this.recurrentes.crear(nombre, this.carrito.items(), this.frecuencia, this.dia).subscribe(() => {
      this.notif.exito('recurrentes.creado');
      this.guardando.set(false);
      this.nombreRecurrente = '';
    });
  }
}
