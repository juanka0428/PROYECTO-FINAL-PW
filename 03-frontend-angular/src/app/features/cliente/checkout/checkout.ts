import { FotoProducto } from '../../../shared/components/foto-producto';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { TraducirPipe } from '../../../core/i18n/traduccion.service';
import { toSignal } from '@angular/core/rxjs-interop';
import { MetodoPago } from '../../../core/models/models';
import { AuthService } from '../../../core/services/auth.service';
import { CarritoService } from '../../../core/services/carrito.service';
import { FiadoService } from '../../../core/services/fiado.service';
import { NotificacionService } from '../../../core/services/notificacion.service';
import { PedidoService } from '../../../core/services/pedido.service';
import { CopPipe } from '../../../shared/pipes/cop.pipe';

@Component({
  selector: 'app-checkout',
  imports: [FotoProducto, ReactiveFormsModule, RouterLink, TraducirPipe, CopPipe],
  templateUrl: './checkout.html',
})
export class Checkout {
  protected carrito = inject(CarritoService);
  protected fiado = inject(FiadoService);
  private auth = inject(AuthService);
  private pedidos = inject(PedidoService);
  private notif = inject(NotificacionService);
  private router = inject(Router);

  protected readonly enviando = signal(false);
  private usuario = this.auth.usuarioActual();

  protected readonly form = inject(FormBuilder).nonNullable.group({
    direccion: [this.usuario?.direccion ?? '', Validators.required],
    barrio: [this.usuario?.barrio ?? '', Validators.required],
    telefono: [this.usuario?.telefono ?? '', [Validators.required, Validators.pattern(/^3\d{2}\s?\d{3}\s?\d{4}$/)]],
    notas: [''],
    metodoPago: ['CONTRA_ENTREGA' as MetodoPago, Validators.required],
  });

  protected readonly tarjeta = inject(FormBuilder).nonNullable.group({
    titular: ['', Validators.required],
    numero: ['', [Validators.required, Validators.pattern(/^[\d ]{13,23}$/)]],
    vence: ['', [Validators.required, Validators.pattern(/^(0[1-9]|1[0-2])\/\d{2}$/)]],
    cvc: ['', [Validators.required, Validators.pattern(/^\d{3,4}$/)]],
  });

  formatearNumero(): void {
    const limpio = this.tarjeta.controls.numero.value.replace(/\D/g, '').slice(0, 19);
    this.tarjeta.controls.numero.setValue(limpio.replace(/(\d{4})(?=\d)/g, '$1 '));
  }

  formatearVence(): void {
    const limpio = this.tarjeta.controls.vence.value.replace(/\D/g, '').slice(0, 4);
    this.tarjeta.controls.vence.setValue(limpio.length > 2 ? limpio.slice(0, 2) + '/' + limpio.slice(2) : limpio);
  }

  protected readonly metodo = toSignal(this.form.controls.metodoPago.valueChanges, { initialValue: this.form.controls.metodoPago.value });
  protected readonly alcanzaFiado = computed(() => this.fiado.miCuenta().disponible >= this.carrito.total());

  confirmar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    if (this.metodo() === 'EN_LINEA' && this.tarjeta.invalid) {
      this.tarjeta.markAllAsTouched();
      return;
    }
    if (this.metodo() === 'FIADO' && !this.alcanzaFiado()) {
      this.notif.error(new Error('checkout.errorCupo'));
      return;
    }
    this.enviando.set(true);
    const v = this.form.getRawValue();
    this.pedidos
      .crear({
        items: this.carrito.items(),
        direccion: v.direccion,
        barrio: v.barrio,
        telefono: v.telefono,
        notas: v.notas,
        metodoPago: v.metodoPago,
        origen: this.carrito.origen(),
        pago: this.tarjeta.getRawValue(),
      })
      .subscribe({
        next: (p) => {
          this.carrito.vaciar();
          this.notif.exito('checkout.exito', { codigo: p.codigo });
          this.router.navigate(['/seguimiento', p.id]);
        },
        error: (e) => {
          this.notif.error(e);
          this.enviando.set(false);
        },
      });
  }
}
