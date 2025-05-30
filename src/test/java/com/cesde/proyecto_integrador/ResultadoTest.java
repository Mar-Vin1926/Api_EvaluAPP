package com.cesde.proyecto_integrador;

import com.cesde.proyecto_integrador.model.Examen;
import com.cesde.proyecto_integrador.model.Opcion;
import com.cesde.proyecto_integrador.model.Pregunta;
import com.cesde.proyecto_integrador.model.Resultado;
import com.cesde.proyecto_integrador.model.User;
import com.cesde.proyecto_integrador.model.Pregunta.TipoPregunta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias para la clase Resultado")
class ResultadoTest {

    private Examen examen;
    private User estudiante;
    private Resultado resultado;

    @BeforeEach
    void setUp() {
        // Configuración común para todas las pruebas
        configurarEntidadesDePrueba();
    }

    private void configurarEntidadesDePrueba() {
        // Crear un examen de prueba
        examen = new Examen();
        examen.setId(1L);
        examen.setTitulo("Examen de Prueba");
        
        // Configurar preguntas y opciones
        Pregunta pregunta1 = crearPreguntaSeleccionUnica();
        Pregunta pregunta2 = crearPreguntaMultiple();
        
        // Asignar preguntas al examen
        examen.setPreguntas(Arrays.asList(pregunta1, pregunta2));
        
        // Configurar estudiante
        estudiante = new User();
        estudiante.setId(1L);
        estudiante.setEmail("estudiante@test.com");
        estudiante.setRole(User.Role.STUDENT);
        
        // Configurar resultado
        resultado = new Resultado();
        resultado.setId(1L);
        resultado.setUsuario(estudiante);
        resultado.setExamen(examen);
    }
    
    private Pregunta crearPreguntaSeleccionUnica() {
        // Crear pregunta de selección única
        Pregunta pregunta = new Pregunta();
        pregunta.setId(1L);
        pregunta.setTextoPregunta("¿Qué es Java?");
        pregunta.setTipoPregunta(TipoPregunta.SELECCION_UNICA);
        
        // Opciones para pregunta de selección única
        Opcion opcionCorrecta = crearOpcion(1L, "Un lenguaje de programación", true);
        Opcion opcionIncorrecta = crearOpcion(2L, "Un tipo de café", false);
        
        // Establecer la relación bidireccional
        opcionCorrecta.setPregunta(pregunta);
        opcionIncorrecta.setPregunta(pregunta);
        pregunta.setOpciones(Arrays.asList(opcionCorrecta, opcionIncorrecta));
        
        return pregunta;
    }
    
    private Pregunta crearPreguntaMultiple() {
        // Crear pregunta de opción múltiple
        Pregunta pregunta = new Pregunta();
        pregunta.setId(2L);
        pregunta.setTextoPregunta("¿Qué características tiene Java?");
        pregunta.setTipoPregunta(TipoPregunta.MULTIPLE);
        
        // Opciones para pregunta múltiple
        Opcion opcion1 = crearOpcion(3L, "Es orientado a objetos", true);
        Opcion opcion2 = crearOpcion(4L, "Es de código abierto", true);
        Opcion opcion3 = crearOpcion(5L, "Es un framework", false);
        
        // Establecer la relación bidireccional
        opcion1.setPregunta(pregunta);
        opcion2.setPregunta(pregunta);
        opcion3.setPregunta(pregunta);
        pregunta.setOpciones(Arrays.asList(opcion1, opcion2, opcion3));
        
        return pregunta;
    }
    
    private Opcion crearOpcion(Long id, String texto, boolean esCorrecta) {
        Opcion opcion = new Opcion();
        opcion.setId(id);
        opcion.setTextoOpcion(texto);
        opcion.setEsCorrecta(esCorrecta);
        return opcion;
    }

    @Test
    @DisplayName("Debería calcular 100% cuando todas las respuestas son correctas")
    void calcularPuntaje_TodasCorrectas_DeberiaRetornar100() {
        // Arrange
        resultado.setOpcionesSeleccionadas(Arrays.asList(1L, 3L, 4L));
        
        // Act
        resultado.calcularPuntaje();
        
        // Assert
        assertEquals(100, resultado.getPuntaje(), 
            "Debería ser 100% cuando todas las respuestas son correctas");
    }

    @Test
    @DisplayName("Debería calcular 0% cuando todas las respuestas son incorrectas")
    void calcularPuntaje_TodasIncorrectas_DeberiaRetornar0() {
        // Arrange
        resultado.setOpcionesSeleccionadas(Arrays.asList(2L, 5L));
        
        // Act
        resultado.calcularPuntaje();
        
        // Assert
        assertEquals(0, resultado.getPuntaje(),
            "Debería ser 0% cuando todas las respuestas son incorrectas");
    }

    @Test
    @DisplayName("Debería calcular 50% cuando la mitad de las respuestas son correctas")
    void calcularPuntaje_MitadCorrectas_DeberiaRetornar50() {
        // Arrange
        resultado.setOpcionesSeleccionadas(Arrays.asList(1L, 3L, 5L));
        
        // Act
        resultado.calcularPuntaje();
        
        // Assert
        assertEquals(50, resultado.getPuntaje(),
            "Debería ser 50% cuando la mitad de las respuestas son correctas");
    }
    
    @Test
    @DisplayName("Debería lanzar excepción cuando el examen es nulo")
    void calcularPuntaje_ExamenNulo_DeberiaLanzarExcepcion() {
        // Arrange
        resultado.setExamen(null);
        
        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, 
            () -> resultado.calcularPuntaje(),
            "Debería lanzar IllegalStateException cuando el examen es nulo");
            
        assertEquals("El examen no puede ser nulo para calcular el puntaje", exception.getMessage());
    }
}
