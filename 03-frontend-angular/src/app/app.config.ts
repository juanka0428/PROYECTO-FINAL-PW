import { registerLocaleData } from '@angular/common';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import localeEsCo from '@angular/common/locales/es-CO';
import localePt from '@angular/common/locales/pt';
import {
  ApplicationConfig, LOCALE_ID, inject, provideAppInitializer, provideBrowserGlobalErrorListeners,
} from '@angular/core';
import { provideRouter, withComponentInputBinding, withHashLocation, withInMemoryScrolling } from '@angular/router';

import { routes } from './app.routes';
import { jwtInterceptor } from './core/interceptors/jwt.interceptor';
import { IdiomaService, idiomaGuardado } from './core/services/idioma.service';

registerLocaleData(localeEsCo, 'es-CO');
registerLocaleData(localePt, 'pt');

function idiomaFechas(): string {
  const idioma = idiomaGuardado();
  if (idioma === 'en') return 'en-US';
  if (idioma === 'pt') return 'pt';
  return 'es-CO';
}

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(
      routes,
      withHashLocation(),
      withComponentInputBinding(),
      withInMemoryScrolling({ scrollPositionRestoration: 'top' }),
    ),
    provideHttpClient(withInterceptors([jwtInterceptor])),
    { provide: LOCALE_ID, useFactory: idiomaFechas },
    provideAppInitializer(() => inject(IdiomaService).iniciar()),
  ],
};
