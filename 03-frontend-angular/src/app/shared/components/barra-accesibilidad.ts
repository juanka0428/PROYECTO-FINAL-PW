import { Component, inject } from '@angular/core';
import { TraducirPipe } from '../../core/i18n/traduccion.service';
import { AccesibilidadService } from '../../core/services/accesibilidad.service';
import { IDIOMAS, Idioma, IdiomaService } from '../../core/services/idioma.service';

@Component({
  selector: 'app-barra-accesibilidad',
  imports: [TraducirPipe],
  template: `
    <div class="barra-acc" role="toolbar" [attr.aria-label]="'acc.titulo' | translate">
      <div class="container d-flex flex-wrap align-items-center gap-2 py-1">
        <span class="small fw-semibold me-1 d-none d-sm-inline"><i class="bi bi-universal-access me-1" aria-hidden="true"></i>{{ 'acc.titulo' | translate }}</span>
        <div class="btn-group btn-group-sm" role="group" [attr.aria-label]="'acc.tamano' | translate">
          <button type="button" class="btn btn-outline-light" (click)="acc.achicar()" [attr.aria-label]="'acc.achicar' | translate">A−</button>
          <button type="button" class="btn btn-outline-light" (click)="acc.escala.set(1)" [attr.aria-label]="'acc.normal' | translate">A</button>
          <button type="button" class="btn btn-outline-light" (click)="acc.agrandar()" [attr.aria-label]="'acc.agrandar' | translate">A+</button>
        </div>
        <button type="button" class="btn btn-sm btn-outline-light" [attr.aria-pressed]="acc.altoContraste()"
                (click)="acc.altoContraste.set(!acc.altoContraste())">
          <i class="bi bi-circle-half me-1" aria-hidden="true"></i>{{ 'acc.contraste' | translate }}
        </button>
        <button type="button" class="btn btn-sm btn-outline-light" [attr.aria-pressed]="acc.reducirMovimiento()"
                (click)="acc.reducirMovimiento.set(!acc.reducirMovimiento())">
          <i class="bi bi-pause-circle me-1" aria-hidden="true"></i>{{ 'acc.movimiento' | translate }}
        </button>
        <div class="ms-auto d-flex align-items-center gap-1">
          <label for="idioma" class="small"><i class="bi bi-translate" aria-hidden="true"></i><span class="visually-hidden">{{ 'acc.idioma' | translate }}</span></label>
          <select id="idioma" class="form-select form-select-sm w-auto" [value]="idioma.actual()" (change)="cambiarIdioma($event)">
            @for (i of idiomas; track i.codigo) {
              <option [value]="i.codigo" [selected]="i.codigo === idioma.actual()">{{ i.nombre }}</option>
            }
          </select>
        </div>
      </div>
    </div>
  `,
  styles: `
    .barra-acc { background: var(--tc-oscuro); color: #fff; }
    .btn-outline-light { --bs-btn-padding-y: .1rem; }
    .btn-outline-light[aria-pressed='true'] { background: #fff; color: var(--tc-oscuro); }
  `,
})
export class BarraAccesibilidad {
  protected acc = inject(AccesibilidadService);
  protected idioma = inject(IdiomaService);
  protected readonly idiomas = IDIOMAS;

  cambiarIdioma(e: Event): void {
    this.idioma.cambiar((e.target as HTMLSelectElement).value as Idioma);
  }
}
