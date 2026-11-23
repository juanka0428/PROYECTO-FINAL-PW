import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { TraducirPipe } from '../../../core/i18n/traduccion.service';
import { AuthService } from '../../../core/services/auth.service';
import { NotificacionService } from '../../../core/services/notificacion.service';

@Component({
  selector: 'app-google',
  imports: [TraducirPipe],
  template: `
    <div class="container py-5 text-center" role="status" aria-live="polite">
      <div class="spinner-border text-primary mb-3" aria-hidden="true"></div>
      <p>{{ 'auth.googleEntrando' | translate }}</p>
    </div>
  `,
})
export class Google implements OnInit {
  private ruta = inject(ActivatedRoute);
  private router = inject(Router);
  private auth = inject(AuthService);
  private notif = inject(NotificacionService);

  ngOnInit(): void {
    const token = this.ruta.snapshot.queryParamMap.get('token');
    if (!token) {
      this.router.navigate(['/login'], { queryParams: { error: 'google' } });
      return;
    }
    this.auth.sesionConToken(token).subscribe({
      next: (s) => {
        this.notif.exito('auth.bienvenida', { nombre: s.usuario.nombre.split(' ')[0] });
        this.router.navigateByUrl(this.auth.rutaInicio(s.usuario.rol), { replaceUrl: true });
      },
      error: () => this.router.navigate(['/login'], { queryParams: { error: 'google' } }),
    });
  }
}
