import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { CONFIG } from '../config';
import { AuthService } from '../services/auth.service';
import { idiomaGuardado } from '../services/idioma.service';

export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const token = auth.token();
  if (req.url.startsWith(CONFIG.apiUrl)) {
    req = req.clone({ setParams: { lang: idiomaGuardado() } });
    if (token && !req.headers.has('Authorization')) {
      req = req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
    }
  }
  return next(req).pipe(
    catchError((e: HttpErrorResponse) => {
      if (token && e.status === 401) auth.logout();
      return throwError(() => e);
    }),
  );
};
