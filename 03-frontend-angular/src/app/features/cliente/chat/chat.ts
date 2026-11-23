import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TraducirPipe, TraduccionService } from '../../../core/i18n/traduccion.service';
import { ChatService } from '../../../core/services/chat.service';
import { NotificacionService } from '../../../core/services/notificacion.service';
import { ListaMensajes } from '../../../shared/components/lista-mensajes';

@Component({
  selector: 'app-chat',
  imports: [FormsModule, TraducirPipe, ListaMensajes],
  templateUrl: './chat.html',
})
export class Chat {
  protected chat = inject(ChatService);
  private t = inject(TraduccionService);
  private notif = inject(NotificacionService);
  protected texto = '';
  protected readonly escribiendo = signal(false);

  protected readonly conv = this.chat.miConversacion;
  protected readonly estado = computed(() => this.conv()?.estado ?? 'IA');
  protected readonly rapidas = ['chat.rapida1', 'chat.rapida2', 'chat.rapida3', 'chat.rapida4'];

  enviar(texto = this.texto): void {
    const t = texto.trim();
    if (!t) return;
    this.texto = '';
    const conIa = this.estado() === 'IA';
    if (conIa) this.escribiendo.set(true);
    this.chat.enviarComoCliente(t).subscribe({
      next: () => this.escribiendo.set(false),
      error: (e) => {
        this.escribiendo.set(false);
        this.notif.error(e);
      },
    });
  }

  rapida(clave: string): void {
    this.enviar(this.t.instant(clave));
  }
}
