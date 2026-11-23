import { DatePipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TraducirPipe } from '../../../core/i18n/traduccion.service';
import { Usuario } from '../../../core/models/models';
import { FiadoService } from '../../../core/services/fiado.service';
import { NotificacionService } from '../../../core/services/notificacion.service';
import { CopPipe } from '../../../shared/pipes/cop.pipe';

@Component({
  selector: 'app-clientes',
  imports: [FormsModule, DatePipe, TraducirPipe, CopPipe],
  templateUrl: './clientes.html',
})
export class Clientes {
  protected fiado = inject(FiadoService);
  private notif = inject(NotificacionService);

  protected readonly seleccionadoId = signal<string | null>(null);
  protected readonly seleccionado = computed(() => this.fiado.clientes().find((c) => c.id === this.seleccionadoId()) ?? null);
  protected readonly movimientos = computed(() => (this.seleccionadoId() ? this.fiado.movimientosDe(this.seleccionadoId()!) : []));

  protected nuevoCupo = 0;
  protected abono = 0;
  protected descripcionAbono = '';

  abrir(c: Usuario): void {
    this.seleccionadoId.set(c.id);
    this.nuevoCupo = c.cupoFiado ?? 0;
    this.abono = 0;
    this.descripcionAbono = '';
  }

  disponible(c: Usuario): number {
    return Math.max(0, (c.cupoFiado ?? 0) - (c.saldoFiado ?? 0));
  }

  guardarCupo(): void {
    const c = this.seleccionado();
    if (!c) return;
    this.fiado.cambiarCupo(c.id, Number(this.nuevoCupo)).subscribe({
      next: () => this.notif.exito('clientes.cupoActualizado'),
      error: (e) => this.notif.error(e),
    });
  }

  registrarAbono(): void {
    const c = this.seleccionado();
    if (!c) return;
    this.fiado.registrarAbono(c.id, Number(this.abono), this.descripcionAbono).subscribe({
      next: () => {
        this.notif.exito('clientes.abonoRegistrado');
        this.abono = 0;
        this.descripcionAbono = '';
      },
      error: (e) => this.notif.error(e),
    });
  }
}
