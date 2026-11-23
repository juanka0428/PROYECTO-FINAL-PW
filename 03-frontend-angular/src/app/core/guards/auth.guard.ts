import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Rol } from '../models/models';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = (_route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (auth.autenticado()) return true;
  return router.createUrlTree(['/login'], { queryParams: { volver: state.url } });
};

export const rolGuard = (...roles: Rol[]): CanActivateFn => (_route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (!auth.autenticado()) {
    return router.createUrlTree(['/login'], { queryParams: { volver: state.url } });
  }
  return auth.tieneRol(...roles) ? true : router.createUrlTree(['/no-autorizado']);
};

export const invitadoGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return auth.autenticado() ? router.createUrlTree([auth.rutaInicio()]) : true;
};
