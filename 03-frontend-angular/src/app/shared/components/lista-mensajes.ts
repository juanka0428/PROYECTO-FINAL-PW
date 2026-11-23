import { DatePipe } from '@angular/common';
import { AfterViewChecked, Component, ElementRef, input, viewChild } from '@angular/core';
import { TraducirPipe } from '../../core/i18n/traduccion.service';
import { AutorMensaje, Mensaje } from '../../core/models/models';

@Component({
  selector: 'app-lista-mensajes',
  imports: [DatePipe, TraducirPipe],
  template: `
    <div #lista class="lista" role="log" aria-live="polite" [attr.aria-label]="'chat.conversacion' | translate">
      @for (m of mensajes(); track m.id) {
        <div class="fila" [class.propia]="m.autor === propio()">
          <div class="burbuja" [class.ia]="m.autor === 'IA'" [class.asesor]="m.autor === 'ASESOR'">
            <div class="autor small fw-semibold">
              @switch (m.autor) {
                @case ('IA') { <i class="bi bi-robot me-1" aria-hidden="true"></i>{{ 'chat.asistente' | translate }} }
                @case ('ASESOR') { <i class="bi bi-headset me-1" aria-hidden="true"></i>{{ 'chat.asesor' | translate }} }
                @default { <i class="bi bi-person me-1" aria-hidden="true"></i>{{ nombreCliente() || ('chat.tu' | translate) }} }
              }
            </div>
            <div class="texto">{{ m.texto }}</div>
            <div class="hora small">{{ m.fecha | date: 'shortTime' }}</div>
          </div>
        </div>
      } @empty {
        <p class="text-center text-body-secondary my-5">{{ 'chat.vacio' | translate }}</p>
      }
    </div>
  `,
  styles: `
    .lista { height: var(--alto, 420px); overflow-y: auto; padding: 1rem; background: var(--tc-fondo-suave); border-radius: var(--tc-radio); }
    .fila { display: flex; margin-bottom: .75rem; }
    .fila.propia { justify-content: flex-end; }
    .burbuja { max-width: 80%; background: #fff; border: 1px solid var(--tc-borde); border-radius: 1rem 1rem 1rem .25rem; padding: .5rem .85rem; }
    .propia .burbuja { background: var(--tc-primario); color: #fff; border-color: var(--tc-primario); border-radius: 1rem 1rem .25rem 1rem; }
    .fila:not(.propia) .burbuja.ia { background: #eef6f1; }
    .fila:not(.propia) .burbuja.asesor { background: #fff6dd; }
    .texto { white-space: pre-wrap; }
    .hora { opacity: .7; text-align: right; }
  `,
})
export class ListaMensajes implements AfterViewChecked {
  readonly mensajes = input.required<Mensaje[]>();
  readonly propio = input<AutorMensaje>('CLIENTE');
  readonly nombreCliente = input<string>('');
  private lista = viewChild.required<ElementRef<HTMLDivElement>>('lista');
  private cantidadAnterior = 0;

  ngAfterViewChecked(): void {
    const n = this.mensajes().length;
    if (n !== this.cantidadAnterior) {
      this.cantidadAnterior = n;
      const el = this.lista().nativeElement;
      el.scrollTop = el.scrollHeight;
    }
  }
}
