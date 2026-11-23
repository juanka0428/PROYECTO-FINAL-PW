import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TraducirPipe } from '../../../core/i18n/traduccion.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-no-autorizado',
  imports: [RouterLink, TraducirPipe],
  template: `
    <div class="container py-5 text-center">
      <div class="display-1" aria-hidden="true">🔒</div>
      <h1 class="h3 fw-bold">{{ 'auth.noAutorizado' | translate }}</h1>
      <p class="text-body-secondary">{{ 'auth.noAutorizadoTexto' | translate }}</p>
      <a class="btn btn-primary" [routerLink]="auth.rutaInicio()">{{ 'comun.volverInicio' | translate }}</a>
    </div>
  `,
})
export class NoAutorizado {
  protected auth = inject(AuthService);
}
