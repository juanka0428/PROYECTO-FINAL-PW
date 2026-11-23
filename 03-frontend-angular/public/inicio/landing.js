document.documentElement.classList.add('js');

$(function () {
  'use strict';

  var API = location.protocol === 'https:' ? 'https://localhost:8443/api' : 'http://localhost:8080/api';
  var textos = {};
  var idioma = 'es';
  try { idioma = localStorage.getItem('tc-idioma') || 'es'; } catch (e) {  }

  var porDefecto = { contEnviar: 'Enviar', contEnviando: 'Enviando…', contOk: '¡Gracias! Te contactaremos pronto.',
    contError: 'No se pudo enviar. Intenta de nuevo más tarde.' };

  function t(clave) { return textos['inicio.' + clave] || porDefecto[clave]; }

  function aplicarIdioma() {
    $('html').attr('lang', idioma);
    $('#idioma').val(idioma);
    $('[data-i18n]').each(function () {
      var texto = t($(this).data('i18n'));
      if (texto) $(this).text(texto);
    });
    $('[data-i18n-alt]').each(function () {
      var texto = t($(this).data('i18n-alt'));
      if (texto) $(this).attr('alt', texto);
    });
    $('[data-i18n-lista]').each(function () {
      var clave = $(this).data('i18n-lista');
      $(this).children('li').each(function (i) {
        var texto = t(clave + '.' + (i + 1));
        if (texto) $(this).text(texto);
      });
    });
  }

  function cargarIdioma() {
    $.getJSON(API + '/i18n', { lang: idioma })
      .done(function (datos) { textos = datos; aplicarIdioma(); })
      .fail(function () { console.log('No se pudieron cargar los textos, se queda en español'); });
  }

  $('#idioma').on('change', function () {
    idioma = $(this).val();
    try { localStorage.setItem('tc-idioma', idioma); } catch (e) {  }
    cargarIdioma();
  });

  cargarIdioma();

  var $ventana = $(window);
  function alHacerScroll() {
    var y = $ventana.scrollTop();
    $('#barra').toggleClass('solida', y > 40);
    if (y > 500) $('#subir').fadeIn(200); else $('#subir').fadeOut(200);
    revisarAparecer();
  }
  $ventana.on('scroll', alHacerScroll);

  $('#subir').on('click', function () { $('html, body').animate({ scrollTop: 0 }, 500); });

  $('a[href^="#"]').on('click', function (e) {
    var destino = $(this).attr('href');
    if (destino.length > 1 && $(destino).length) {
      e.preventDefault();
      $('html, body').animate({ scrollTop: $(destino).offset().top - 60 }, 500, function () {
        $(destino).attr('tabindex', '-1').trigger('focus');
      });
      $('#menu').removeClass('show');
    }
  });

  function revisarAparecer() {
    var limite = $ventana.scrollTop() + $ventana.height() - 60;
    $('.aparecer:not(.visible)').each(function () {
      if ($(this).offset().top < limite) $(this).addClass('visible');
    });
  }

  var contado = false;
  function animarCifras() {
    if (contado) return;
    var $cifras = $('.cifras');
    if ($ventana.scrollTop() + $ventana.height() < $cifras.offset().top + 50) return;
    contado = true;
    $('.numero').each(function () {
      var $n = $(this);
      $({ v: 0 }).animate({ v: $n.data('hasta') }, {
        duration: 1200,
        step: function (v) { $n.text(Math.round(v)); },
        complete: function () { $n.text($n.data('hasta')); }
      });
    });
  }
  $ventana.on('scroll', animarCifras);

  $('.pregunta').on('click', function () {
    var $btn = $(this);
    var abierta = $btn.attr('aria-expanded') === 'true';
    $('.pregunta').not($btn).attr('aria-expanded', 'false').next('.respuesta').slideUp(200);
    $btn.attr('aria-expanded', String(!abierta)).next('.respuesta').slideToggle(200);
  });

  $('#form-contacto').on('submit', function (e) {
    e.preventDefault();
    var $form = $(this);
    var valido = true;
    $form.find('input').each(function () {
      var ok = this.checkValidity();
      $(this).toggleClass('is-invalid', !ok).attr('aria-invalid', String(!ok));
      if (!ok && valido) { $(this).trigger('focus'); valido = false; }
    });
    if (!valido) return;

    var datos = {
      nombre: $('#c-nombre').val(),
      tienda: $('#c-tienda').val(),
      telefono: $('#c-telefono').val()
    };
    var $boton = $form.find('button[type=submit]').prop('disabled', true).text(t('contEnviando'));

    $.ajax({
      url: API + '/contacto?lang=' + idioma,
      method: 'POST',
      contentType: 'application/json',
      data: JSON.stringify(datos)
    })
      .done(function () {
        $('#mensaje-contacto').html('<div class="alert alert-success mb-0"></div>').find('.alert').text(t('contOk'));
        $form[0].reset();
      })
      .fail(function () {
        $('#mensaje-contacto').html('<div class="alert alert-danger mb-0"></div>').find('.alert').text(t('contError'));
      })
      .always(function () { $boton.prop('disabled', false).text(t('contEnviar')); });
  });

  $('#anio').text(new Date().getFullYear());
  alHacerScroll();
  animarCifras();
});
