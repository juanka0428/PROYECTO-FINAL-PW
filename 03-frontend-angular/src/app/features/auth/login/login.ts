import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { TraducirPipe } from '../../../core/i18n/traduccion.service';
import { Sesion } from '../../../core/models/models';
import { AuthService } from '../../../core/services/auth.service';
import { NotificacionService } from '../../../core/services/notificacion.service';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink, TraducirPipe],
  templateUrl: './login.html',
})
export class Login {
  private auth = inject(AuthService);
  private router = inject(Router);
  private ruta = inject(ActivatedRoute);
  private notif = inject(NotificacionService);

  protected readonly cargando = signal(false);
  protected readonly verClave = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly form = inject(FormBuilder).nonNullable.group({
    correo: ['', [Validators.required, Validators.email]],
    clave: ['', [Validators.required, Validators.minLength(6)]],
  });

  protected readonly demos = [
    { rol: 'CLIENTE', correo: 'maria@correo.com' },
    { rol: 'TENDERO', correo: 'tendero@tienda.com' },
    { rol: 'REPARTIDOR', correo: 'repartidor@tienda.com' },
    { rol: 'ASESOR', correo: 'asesor@tienda.com' },
  ];

  usarDemo(correo: string): void {
    this.form.setValue({ correo, clave: '123456' });
  }

  enviar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.cargando.set(true);
    this.error.set(null);
    const { correo, clave } = this.form.getRawValue();
    this.auth.login(correo, clave).subscribe({
      next: (s) => this.entrar(s),
      error: (e: Error) => {
        this.error.set(e.message);
        this.cargando.set(false);
      },
    });
  }

  constructor() {
    const error = this.ruta.snapshot.queryParamMap.get('error');
    if (error === 'google') this.error.set('auth.errorGoogle');
    if (error === 'inactivo') this.error.set('auth.errorInactivo');
  }

  conGoogle(): void {
    this.cargando.set(true);
    this.auth.loginConGoogle();
  }

  private entrar(s: Sesion): void {
    this.notif.exito('auth.bienvenida', { nombre: s.usuario.nombre.split(' ')[0] });
    const volver = this.ruta.snapshot.queryParamMap.get('volver');
    this.router.navigateByUrl(volver && s.usuario.rol === 'CLIENTE' ? volver : this.auth.rutaInicio(s.usuario.rol));
  }
}
