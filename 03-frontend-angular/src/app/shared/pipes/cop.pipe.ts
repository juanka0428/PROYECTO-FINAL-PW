import { Pipe, PipeTransform } from '@angular/core';

const FORMATO = new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 });

@Pipe({ name: 'cop' })
export class CopPipe implements PipeTransform {
  transform(valor: number | null | undefined): string {
    return FORMATO.format(valor ?? 0);
  }
}
