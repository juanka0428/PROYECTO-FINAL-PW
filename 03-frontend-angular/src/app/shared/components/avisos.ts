import { Component, inject } from '@angular/core';
import { TraducirPipe } from '../../core/i18n/traduccion.service';
import { NotificacionService } from '../../core/services/notificacion.service';

@Component({
  selector: 'app-avisos',
  imports: [TraducirPipe],
  template: `
    <div class="toast-container position-fixed top-0 end-0 p-3" aria-live="polite" aria-atomic="false">
      @for (a of notif.avisos(); track a.id) {
        <div class="toast show align-items-center border-0 text-white"
             [class.bg-success]="a.tipo === 'exito'" [class.bg-danger]="a.tipo === 'error'" [class.bg-dark]="a.tipo === 'info'"
             [attr.role]="a.tipo === 'error' ? 'alert' : 'status'">
          <div class="d-flex">
            <div class="toast-body">{{ a.texto }}</div>
            <button type="button" class="btn-close btn-close-white me-2 m-auto" (click)="notif.cerrar(a.id)"
                    [attr.aria-label]="'comun.cerrar' | translate"></button>
          </div>
        </div>
      }
    </div>
  `,
  styles: `.toast-container { z-index: 1100; margin-top: 5.5rem; }`,
})
export class Avisos {
  protected notif = inject(NotificacionService);
}
