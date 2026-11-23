import { DatePipe } from '@angular/common';
import { Component, computed, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TraducirPipe } from '../../../core/i18n/traduccion.service';
import { CONFIG } from '../../../core/config';
import { FiadoService } from '../../../core/services/fiado.service';
import { CopPipe } from '../../../shared/pipes/cop.pipe';

@Component({
  selector: 'app-mi-fiado',
  imports: [RouterLink, DatePipe, TraducirPipe, CopPipe],
  templateUrl: './mi-fiado.html',
})
export class MiFiado {
  protected fiado = inject(FiadoService);
  protected readonly config = CONFIG;
  protected readonly porcentaje = computed(() => {
    const c = this.fiado.miCuenta();
    return c.cupo ? Math.min(100, Math.round((c.saldo / c.cupo) * 100)) : 0;
  });
}
