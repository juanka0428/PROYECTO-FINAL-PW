import { Component, inject, signal } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { TraducirPipe } from '../../../core/i18n/traduccion.service';
import { AuthService } from '../../../core/services/auth.service';
import { NotificacionService } from '../../../core/services/notificacion.service';

const clavesIguales = (g: AbstractControl): ValidationErrors | null =>
  g.get('clave')?.value === g.get('confirmar')?.value ? null : { distintas: true };

@Component({
  selector: 'app-registro',
  imports: [ReactiveFormsModule, RouterLink, TraducirPipe],
  templateUrl: './registro.html',
})
export class Registro {
  private auth = inject(AuthService);
  private router = inject(Router);
  private notif = inject(NotificacionService);

  protected readonly cargando = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly barrios = ['Altico', 'Quirinal', 'Centro', 'Las Granjas', 'Cándido', 'Santa Inés', 'Buenos Aires', 'Otro'];

  protected readonly form = inject(FormBuilder).nonNullable.group(
    {
      nombre: ['', [Validators.required, Validators.minLength(3)]],
      correo: ['', [Validators.required, Validators.email]],
      telefono: ['', [Validators.required, Validators.pattern(/^3\d{2}\s?\d{3}\s?\d{4}$/)]],
      direccion: ['', Validators.required],
      barrio: ['', Validators.required],
      clave: ['', [Validators.required, Validators.minLength(6)]],
      confirmar: ['', Validators.required],
      acepto: [false, Validators.requiredTrue],
    },
    { validators: clavesIguales },
  );

  invalido(campo: keyof typeof this.form.controls): boolean {
    const c = this.form.controls[campo];
    return c.touched && c.invalid;
  }

  enviar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.cargando.set(true);
    const { acepto, confirmar, ...datos } = this.form.getRawValue();
    this.auth.registrar(datos).subscribe({
      next: () => {
        this.notif.exito('auth.registroOk');
        this.router.navigate(['/catalogo']);
      },
      error: (e: Error) => {
        this.error.set(e.message);
        this.cargando.set(false);
      },
    });
  }
}
