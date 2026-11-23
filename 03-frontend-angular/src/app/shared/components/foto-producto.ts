import { Component, computed, input } from '@angular/core';
import { CONFIG } from '../../core/config';

@Component({
  selector: 'app-foto-producto',
  template: `
    @if (url()) {
      <img [src]="url()" alt="" [class.en-linea]="enLinea()" loading="lazy" />
    } @else {
      {{ imagen() }}
    }
  `,
  styles: `
    img { width: 100%; height: 100%; object-fit: cover; border-radius: inherit; }
    img.en-linea { width: 1.5em; height: 1.5em; border-radius: .25rem; vertical-align: middle; }
  `,
})
export class FotoProducto {
  readonly imagen = input<string>('');
  readonly enLinea = input(false);

  protected readonly url = computed(() => {
    const img = this.imagen() ?? '';
    if (img.startsWith('/api/')) return CONFIG.apiUrl.replace(/\/api$/, '') + img;
    if (img.startsWith('http') || img.startsWith('img/')) return img;
    return null;
  });
}
