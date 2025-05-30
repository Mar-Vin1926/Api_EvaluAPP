package com.cesde.proyecto_integrador.dto;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "DTO para la creación de un resultado")
public class ResultadoRequestDTO {

    @Schema(description = "Puntuacion obtenida por el estudiante")
    @Min(value = 0, message = "La puntuacion no puede ser menor a 0")
    private Integer puntuacion;

    @Schema(description = "ID del examen asociado al resultado")
    @NotNull(message = "El ID del examen es obligatorio")
    private Long examenId;

    @Schema(description = "ID del estudiante que responde el examen")
    @NotNull(message = "El ID del estudiante es obligatorio")
    private Long estudianteId;

    @Schema(description = "IDs de las opciones seleccionadas por el estudiante")
    private List<Long> opcionesSeleccionadas;

    private LocalDateTime fechaFinalizacion;

}
