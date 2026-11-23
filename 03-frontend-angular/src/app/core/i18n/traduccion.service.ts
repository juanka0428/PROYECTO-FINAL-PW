import { HttpClient } from '@angular/common/http';
import { Injectable, Pipe, PipeTransform, inject, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { CONFIG } from '../config';

@Injectable({ providedIn: 'root' })
export class TraduccionService {
  private http = inject(HttpClient);

  readonly textos = signal<Record<string, string>>({});

  async cargar(idioma: string): Promise<void> {
    try {
      const textos = await firstValueFrom(this.http.get<Record<string, string>>(`${CONFIG.apiUrl}/i18n?lang=${idioma}`));
      this.textos.set(textos);
    } catch {
      console.error('No se pudieron cargar los textos del servidor');
    }
  }

  instant(clave: string, valores?: object): string {
    let texto = this.textos()[clave] ?? clave;
    if (valores) {
      for (const [k, v] of Object.entries(valores)) {
        texto = texto.split('{{' + k + '}}').join(String(v));
      }
    }
    return texto;
  }
}

@Pipe({ name: 'translate', pure: false })
export class TraducirPipe implements PipeTransform {
  private traduccion = inject(TraduccionService);

  transform(clave: string, valores?: object): string {
    return this.traduccion.instant(clave, valores);
  }
}
