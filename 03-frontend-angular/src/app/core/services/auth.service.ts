import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, catchError, map, throwError } from 'rxjs';
import { CONFIG } from '../config';
import { Rol, Sesion, Usuario } from '../models/models';
import { AlmacenService, idsATexto } from './almacen.service';

const CLAVE_SESION = 'tc-sesion';

export interface DatosRegistro {
  nombre: string;
  correo: string;
  telefono: string;
  direccion: string;
  barrio: string;
  clave: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private almacen = inject(AlmacenService);
  private http = inject(HttpClient);
  private router = inject(Router);

  private sesion = signal<Sesion | null>(this.leerSesion());

  readonly usuario = computed(() => this.sesion()?.usuario ?? null);
  readonly autenticado = computed(() => this.sesion() !== null);
  readonly rol = computed(() => this.sesion()?.usuario.rol ?? null);
  readonly token = computed(() => this.sesion()?.token ?? null);

  constructor() {
    const s = this.sesion();
    if (s) setTimeout(() => this.almacen.iniciarSesion(s.usuario.rol, s.usuario.id));
  }

  login(correo: string, clave: string): Observable<Sesion> {
    return this.http.post<Sesion>(CONFIG.apiUrl + '/auth/login', { correo, clave }).pipe(
      map((s) => this.abrirSesion(s)),
      catchError((e: HttpErrorResponse) => {
        if (e.status === 401) return throwError(() => new Error('auth.errorCredenciales'));
        if (e.status === 403) return throwError(() => new Error('auth.errorInactivo'));
        return throwError(() => new Error('comun.sinServidor'));
      }),
    );
  }

  loginConGoogle(): void {
    window.location.href = CONFIG.apiUrl.replace(/\/api$/, '') + '/oauth2/authorization/google';
  }

  sesionConToken(token: string): Observable<Sesion> {
    return this.http
      .get<Sesion['usuario']>(CONFIG.apiUrl + '/auth/yo', { headers: { Authorization: `Bearer ${token}` } })
      .pipe(
        map((usuario) => this.abrirSesion({ token, usuario })),
        catchError(() => throwError(() => new Error('auth.errorGoogle'))),
      );
  }

  registrar(datos: DatosRegistro): Observable<Sesion> {
    return this.http.post<Sesion>(CONFIG.apiUrl + '/auth/registro', datos).pipe(
      map((s) => this.abrirSesion(s)),
      catchError((e: HttpErrorResponse) => {
        if (e.status === 409) return throwError(() => new Error('auth.errorCorreoExiste'));
        if (e.error?.mensaje) return throwError(() => new Error(e.error.mensaje));
        return throwError(() => new Error('comun.sinServidor'));
      }),
    );
  }

  logout(): void {
    localStorage.removeItem(CLAVE_SESION);
    this.sesion.set(null);
    this.almacen.cerrarSesion();
    this.router.navigate(['/login']);
  }

  tieneRol(...roles: Rol[]): boolean {
    const r = this.rol();
    return r !== null && roles.includes(r);
  }

  rutaInicio(rol: Rol | null = this.rol()): string {
    switch (rol) {
      case 'TENDERO': return '/tendero';
      case 'REPARTIDOR': return '/repartidor';
      case 'ASESOR': return '/asesor';
      default: return '/catalogo';
    }
  }

  usuarioActual(): Usuario | undefined {
    const id = this.usuario()?.id;
    const delBackend = this.almacen.db().usuarios.find((u) => u.id === id);
    return delBackend ?? (this.usuario() as Usuario | undefined);
  }

  private abrirSesion(respuesta: Sesion): Sesion {
    const sesion: Sesion = { token: respuesta.token, usuario: idsATexto(respuesta.usuario) };
    localStorage.setItem(CLAVE_SESION, JSON.stringify(sesion));
    this.sesion.set(sesion);
    this.almacen.iniciarSesion(sesion.usuario.rol, sesion.usuario.id);
    return sesion;
  }

  private leerSesion(): Sesion | null {
    try {
      const texto = localStorage.getItem(CLAVE_SESION);
      if (!texto) return null;
      const sesion = JSON.parse(texto) as Sesion;
      const parte = sesion.token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
      const contenido = JSON.parse(atob(parte));
      if (contenido.exp * 1000 < Date.now()) return null;
      return sesion;
    } catch {
      return null;
    }
  }
}
