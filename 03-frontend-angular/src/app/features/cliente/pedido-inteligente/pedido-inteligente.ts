import { FotoProducto } from '../../../shared/components/foto-producto';
import { Component, OnDestroy, OnInit, inject, input, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { TraducirPipe, TraduccionService } from '../../../core/i18n/traduccion.service';
import { ItemPedido, OrigenPedido } from '../../../core/models/models';
import { AsistenteIaService } from '../../../core/services/asistente-ia.service';
import { CarritoService } from '../../../core/services/carrito.service';
import { IdiomaService } from '../../../core/services/idioma.service';
import { NotificacionService } from '../../../core/services/notificacion.service';
import { CopPipe } from '../../../shared/pipes/cop.pipe';

type Modo = 'texto' | 'voz' | 'foto';

@Component({
  selector: 'app-pedido-inteligente',
  imports: [FotoProducto, FormsModule, RouterLink, TraducirPipe, CopPipe],
  templateUrl: './pedido-inteligente.html',
  styleUrl: './pedido-inteligente.css',
})
export class PedidoInteligente implements OnInit, OnDestroy {
  private ia = inject(AsistenteIaService);
  private carrito = inject(CarritoService);
  private notif = inject(NotificacionService);
  private idioma = inject(IdiomaService);
  private router = inject(Router);
  private t = inject(TraduccionService);

  readonly modoInicial = input<string | undefined>(undefined, { alias: 'modo' });

  protected readonly modo = signal<Modo>('texto');
  protected readonly texto = signal('');
  protected readonly procesando = signal(false);
  protected readonly items = signal<ItemPedido[] | null>(null);
  protected readonly noReconocidos = signal<string[]>([]);
  protected readonly usoUltimo = signal(false);

  protected readonly vozDisponible = 'webkitSpeechRecognition' in window || 'SpeechRecognition' in window;
  protected readonly escuchando = signal(false);
  private reconocimiento: any = null;

  protected readonly vistaPrevia = signal<string | null>(null);
  private archivo: File | null = null;

  protected readonly ejemplos = ['ia.ejemplo1', 'ia.ejemplo2', 'ia.ejemplo3'];

  ngOnInit(): void {
    const m = this.modoInicial();
    if (m === 'voz' || m === 'foto' || m === 'texto') this.modo.set(m);
  }

  ngOnDestroy(): void {
    this.reconocimiento?.stop();
    const url = this.vistaPrevia();
    if (url) URL.revokeObjectURL(url);
  }

  cambiarModo(m: Modo): void {
    this.modo.set(m);
    this.items.set(null);
  }

  interpretar(origen: OrigenPedido = 'TEXTO'): void {
    const t = this.texto().trim();
    if (!t) return;
    this.procesando.set(true);
    this.ia.interpretar(t).subscribe((r) => {
      this.items.set(r.items);
      this.noReconocidos.set(r.noReconocidos);
      this.usoUltimo.set(r.usoUltimoPedido);
      this.procesando.set(false);
      this.origen = origen;
    });
  }

  private origen: OrigenPedido = 'TEXTO';

  alternarVoz(): void {
    if (this.escuchando()) {
      this.reconocimiento?.stop();
      return;
    }
    const Clase = (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition;
    if (!Clase) return;
    this.reconocimiento = new Clase();
    this.reconocimiento.lang = { es: 'es-CO', en: 'en-US', pt: 'pt-BR' }[this.idioma.actual()];
    this.reconocimiento.interimResults = true;
    this.reconocimiento.continuous = false;
    this.reconocimiento.onresult = (e: any) => {
      const texto = Array.from(e.results).map((r: any) => r[0].transcript).join(' ');
      this.texto.set(texto);
    };
    this.reconocimiento.onerror = () => {
      this.escuchando.set(false);
      this.notif.info('ia.vozError');
    };
    this.reconocimiento.onend = () => {
      this.escuchando.set(false);
      if (this.texto().trim()) this.interpretar('VOZ');
    };
    this.texto.set('');
    this.escuchando.set(true);
    this.reconocimiento.start();
  }

  elegirFoto(e: Event): void {
    const f = (e.target as HTMLInputElement).files?.[0];
    if (!f) return;
    if (!f.type.startsWith('image/')) {
      this.notif.error(new Error('ia.fotoTipo'));
      return;
    }
    this.archivo = f;
    const anterior = this.vistaPrevia();
    if (anterior) URL.revokeObjectURL(anterior);
    this.vistaPrevia.set(URL.createObjectURL(f));
    this.items.set(null);
  }

  leerFoto(): void {
    if (!this.archivo) return;
    this.procesando.set(true);
    this.ia.leerImagen(this.archivo).subscribe({
      next: (texto) => {
        this.texto.set(texto);
        this.interpretar('FOTO');
      },
      error: (e) => {
        this.procesando.set(false);
        this.notif.error(e);
      },
    });
  }

  cambiarCantidad(i: number, cantidad: number): void {
    this.items.update((lista) => {
      if (!lista) return lista;
      const copia = [...lista];
      if (cantidad <= 0) copia.splice(i, 1);
      else copia[i] = { ...copia[i], cantidad };
      return copia;
    });
  }

  total(): number {
    return (this.items() ?? []).reduce((s, i) => s + i.precio * i.cantidad, 0);
  }

  agregarAlCarrito(irAPagar: boolean): void {
    const lista = this.items();
    if (!lista?.length) return;
    this.carrito.agregarVarios(lista, this.origen);
    this.notif.exito('ia.agregados', { n: lista.length });
    this.router.navigate([irAPagar ? '/checkout' : '/carrito']);
  }

  usarEjemplo(clave: string): void {
    this.texto.set(this.t.instant(clave));
    this.interpretar();
  }
}
