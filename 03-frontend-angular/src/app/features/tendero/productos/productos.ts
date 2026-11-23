import { FotoProducto } from '../../../shared/components/foto-producto';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { TraducirPipe } from '../../../core/i18n/traduccion.service';
import { Producto } from '../../../core/models/models';
import { NotificacionService } from '../../../core/services/notificacion.service';
import { ProductoService } from '../../../core/services/producto.service';
import { CopPipe } from '../../../shared/pipes/cop.pipe';

@Component({
  selector: 'app-productos',
  imports: [FotoProducto, ReactiveFormsModule, TraducirPipe, CopPipe],
  templateUrl: './productos.html',
})
export class Productos {
  protected productos = inject(ProductoService);
  private notif = inject(NotificacionService);
  private fb = inject(FormBuilder);

  protected readonly busqueda = signal('');
  protected readonly modalAbierto = signal(false);
  protected readonly editando = signal<Producto | null>(null);
  protected readonly aEliminar = signal<Producto | null>(null);
  protected readonly guardando = signal(false);
  protected readonly subiendo = signal(false);

  protected readonly lista = computed(() => {
    const q = this.busqueda().toLowerCase();
    return this.productos.todos().filter((p) => !q || p.nombre.toLowerCase().includes(q));
  });

  protected readonly form = this.fb.nonNullable.group({
    nombre: ['', [Validators.required, Validators.maxLength(60)]],
    categoriaId: ['', Validators.required],
    precio: [0, [Validators.required, Validators.min(100)]],
    stock: [0, [Validators.required, Validators.min(0)]],
    stockMinimo: [5, [Validators.required, Validators.min(0)]],
    unidad: ['unidad', Validators.required],
    imagen: ['📦', Validators.required],
    palabrasClave: [''],
    activo: [true],
  });

  nuevo(): void {
    this.editando.set(null);
    this.form.reset({ nombre: '', categoriaId: '', precio: 0, stock: 0, stockMinimo: 5, unidad: 'unidad', imagen: '📦', palabrasClave: '', activo: true });
    this.modalAbierto.set(true);
  }

  editar(p: Producto): void {
    this.editando.set(p);
    this.form.reset({ ...p, palabrasClave: p.palabrasClave.join(', ') });
    this.modalAbierto.set(true);
  }

  cerrar(): void {
    this.modalAbierto.set(false);
  }

  invalido(c: keyof typeof this.form.controls): boolean {
    return this.form.controls[c].touched && this.form.controls[c].invalid;
  }

  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    const datos = {
      ...v,
      palabrasClave: v.palabrasClave.split(',').map((s) => s.trim().toLowerCase()).filter(Boolean),
    };
    if (!datos.palabrasClave.length) datos.palabrasClave = [v.nombre.split(' ')[0].toLowerCase()];
    this.guardando.set(true);
    const actual = this.editando();
    const op = actual ? this.productos.actualizar({ ...datos, id: actual.id }) : this.productos.crear(datos);
    op.subscribe({
      next: () => {
        this.notif.exito(actual ? 'productos.actualizado' : 'productos.creado');
        this.guardando.set(false);
        this.cerrar();
      },
      error: (e) => {
        this.notif.error(e);
        this.guardando.set(false);
      },
    });
  }

  subirFoto(e: Event): void {
    const archivo = (e.target as HTMLInputElement).files?.[0];
    const actual = this.editando();
    if (!archivo || !actual) return;
    this.subiendo.set(true);
    this.productos.subirFoto(actual.id, archivo).subscribe({
      next: (p) => {
        this.form.controls.imagen.setValue(p.imagen);
        this.notif.exito('productos.fotoSubida');
        this.subiendo.set(false);
      },
      error: (err) => {
        this.notif.error(err);
        this.subiendo.set(false);
      },
    });
  }

  confirmarEliminar(): void {
    const p = this.aEliminar();
    if (!p) return;
    this.productos.eliminar(p.id).subscribe(() => {
      this.notif.info('productos.eliminado', { nombre: p.nombre });
      this.aEliminar.set(null);
    });
  }

  buscar(e: Event): void {
    this.busqueda.set((e.target as HTMLInputElement).value);
  }
}
