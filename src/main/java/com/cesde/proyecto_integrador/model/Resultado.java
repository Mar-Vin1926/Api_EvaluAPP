package com.cesde.proyecto_integrador.model;

import javax.persistence.*;
import lombok.Data;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "resultados")
@Data
public class Resultado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer puntaje;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private User usuario;

    @ManyToOne
    @JoinColumn(name = "examen_id")
    private Examen examen;

    @ElementCollection
    @CollectionTable(name = "opciones_seleccionadas",
        joinColumns = @JoinColumn(name = "resultado_id"))
    @Column(name = "opcion_id")
    private List<Long> opcionesSeleccionadas; // IDs de las opciones elegidas

    /**
     * Calcula el puntaje del resultado basado en las opciones seleccionadas
     */
    public void calcularPuntaje() {
        if (examen == null) {
            throw new IllegalStateException("El examen no puede ser nulo para calcular el puntaje");
        }

        if (opcionesSeleccionadas == null || opcionesSeleccionadas.isEmpty()) {
            this.puntaje = 0;
            return;
        }

        int totalPuntos = 0;
        int puntosObtenidos = 0;

        // Obtener todas las preguntas del examen
        List<Pregunta> preguntas = examen.getPreguntas();
        
        for (Pregunta pregunta : preguntas) {
            // Obtener opciones de la pregunta
            List<Opcion> opciones = pregunta.getOpciones();
            if (opciones == null || opciones.isEmpty()) {
                continue; // Skip preguntas sin opciones
            }
            
            // Obtener IDs de opciones correctas para esta pregunta
            List<Long> opcionesCorrectas = opciones.stream()
                .filter(Opcion::isEsCorrecta)
                .map(Opcion::getId)
                .collect(Collectors.toList());

            // Filtrar las opciones seleccionadas que pertenecen a esta pregunta
            List<Long> opcionesSeleccionadasPregunta = opcionesSeleccionadas.stream()
                .filter(opcionId -> opciones.stream().anyMatch(o -> o.getId().equals(opcionId)))
                .collect(Collectors.toList());

            // Verificar si las opciones seleccionadas coinciden con las correctas
            boolean respuestaCorrecta = false;
            
            // Para selección única
            if (pregunta.getTipoPregunta() == Pregunta.TipoPregunta.SELECCION_UNICA) {
                // Debe seleccionar exactamente una opción y debe ser la correcta
                respuestaCorrecta = opcionesSeleccionadasPregunta.size() == 1 && 
                                  opcionesCorrectas.size() == 1 &&
                                  opcionesCorrectas.contains(opcionesSeleccionadasPregunta.get(0));
            }
            // Para múltiple
            else if (pregunta.getTipoPregunta() == Pregunta.TipoPregunta.MULTIPLE) {
                // Debe seleccionar todas y solo las opciones correctas
                respuestaCorrecta = !opcionesCorrectas.isEmpty() &&
                                  opcionesSeleccionadasPregunta.size() == opcionesCorrectas.size() &&
                                  opcionesSeleccionadasPregunta.containsAll(opcionesCorrectas);
            }
            // Para texto abierto, no se calcula puntaje automático
            else if (pregunta.getTipoPregunta() == Pregunta.TipoPregunta.TEXTO_ABIERTO) {
                continue;
            }

            // Si la respuesta es correcta, se suma el puntaje
            if (respuestaCorrecta) {
                puntosObtenidos++;
            }
            
            totalPuntos++;
        }

        // Calcular el puntaje final (porcentaje)
        if (totalPuntos > 0) {
            this.puntaje = (int) Math.round((puntosObtenidos / (double) totalPuntos) * 100);
        } else {
            this.puntaje = 0;
        }
    }
}
