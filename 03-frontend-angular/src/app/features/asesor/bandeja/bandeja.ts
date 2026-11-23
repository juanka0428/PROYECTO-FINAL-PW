import { DatePipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TraducirPipe, TraduccionService } from '../../../core/i18n/traduccion.service';
import { Conversacion } from '../../../core/models/models';
import { AlmacenService } from '../../../core/services/almacen.service';
import { AuthService } from '../../../core/services/auth.service';
import { ChatService } from '../../../core/services/chat.service';
import { EstadoBadge } from '../../../shared/components/estado-badge';
import { ListaMensajes } from '../../../shared/components/lista-mensajes';
import { CopPipe } from '../../../shared/pipes/cop.pipe';

@Component({
  selector: 'app-bandeja',
  imports: [FormsModule, DatePipe, TraducirPipe, ListaMensajes, EstadoBadge, CopPipe],
  templateUrl: './bandeja.html',
})
export class Bandeja {
  protected chat = inject(ChatService);
  private auth = inject(AuthService);
  private almacen = inject(AlmacenService);
  private t = inject(TraduccionService);

  protected readonly filtro = signal<'abiertas' | 'cerradas'>('abiertas');
  protected readonly seleccionadaId = signal<string | null>(null);
  protected texto = '';
  protected readonly respuestasRapidas = ['asesor.rapida1', 'asesor.rapida2', 'asesor.rapida3'];

  protected readonly lista = computed(() =>
    this.chat.todas().filter((c) =>
      this.filtro() === 'cerradas' ? c.estado === 'CERRADA' : c.estado === 'ESPERANDO_ASESOR' || c.estado === 'CON_ASESOR',
    ),
  );
  protected readonly seleccionada = computed(() => this.chat.todas().find((c) => c.id === this.seleccionadaId()) ?? null);

  protected readonly contexto = computed(() => {
    const c = this.seleccionada();
    if (!c) return null;
    const db = this.almacen.db();
    const cliente = db.usuarios.find((u) => u.id === c.clienteId);
    const ultimo = [...db.pedidos].filter((p) => p.clienteId === c.clienteId).sort((a, b) => b.fecha.localeCompare(a.fecha))[0];
    return { cliente, ultimo };
  });

  esMia(c: Conversacion): boolean {
    return c.asesorId === this.auth.usuario()?.id;
  }

  enviar(texto = this.texto): void {
    const c = this.seleccionada();
    if (!c || !texto.trim()) return;
    this.chat.enviarComoAsesor(c.id, texto.trim());
    this.texto = '';
  }

  enviarRapida(clave: string): void {
    this.enviar(this.t.instant(clave));
  }

  cerrar(): void {
    const c = this.seleccionada();
    if (!c) return;
    this.chat.cerrar(c.id);
    this.seleccionadaId.set(null);
  }
}
