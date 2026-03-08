package com.rutas.visual.controladores;

import com.rutas.logico.modelo.Criterio;
import com.rutas.logico.modelo.Parada;
import com.rutas.logico.modelo.Ruta;
import com.rutas.logico.modelo.TipoParada;
import com.rutas.servicios.ServicioGrafo;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class PrincipalController {

    @FXML private Label lblParadas;
    @FXML private Label lblRutas;
    @FXML private Label lblPrecio;
    @FXML private Label lblTiposCount;
    @FXML private VBox vboxSegmentacion;

    @FXML
    public void initialize() {
        List<Parada> paradas = ServicioGrafo.get().getParadas();

        lblParadas.setText(String.valueOf(paradas.size()));

        List<Ruta> todasRutas = new ArrayList<>();
        for (Parada p : paradas) {
            todasRutas.addAll(ServicioGrafo.get().getRutas(p));
        }
        lblRutas.setText(String.valueOf(todasRutas.size()));

        double totalCosto = 0;
        for (Ruta r : todasRutas) {
            Object costo = r.getPeso(Criterio.COSTO);
            if (costo instanceof Number) {
                totalCosto += ((Number) costo).doubleValue();
            }
        }
        double promedio = todasRutas.isEmpty() ? 0 : totalCosto / todasRutas.size();
        lblPrecio.setText(String.format("%.0f$", promedio));

        generarSegmentacion(paradas);
    }

    /*
        Nombre: generarSegmentacion
        Argumentos:
            (List<Parada>) paradas: Representa la lista de paradas registradas en el grafo.
        Objetivo: Generar dinámicamente las barras de progreso que muestran la distribución
                  de paradas por tipo de transporte en la pantalla principal.
        Retorno: (void) No retorna valor.
     */

    private void generarSegmentacion(List<Parada> paradas) {
        TipoParada[] tipos = TipoParada.values();
        lblTiposCount.setText(tipos.length + " tipos");

        int maxConteo = 1;
        int[] conteos = new int[tipos.length];
        for (int i = 0; i < tipos.length; i++) {
            for (Parada p : paradas) {
                if (p.getTipo() == tipos[i]) conteos[i]++;
            }
            if (conteos[i] > maxConteo) maxConteo = conteos[i];
        }

        for (int i = 0; i < tipos.length; i++) {
            double progreso = (double) conteos[i] / maxConteo;

            Label lblNombre = new Label(tipos[i].getNombre());
            lblNombre.setPrefWidth(60);
            lblNombre.setStyle("-fx-font-size: 13px; -fx-text-fill: #555;");

            ProgressBar barra = new ProgressBar(progreso);
            barra.setPrefWidth(300);
            barra.setPrefHeight(8);
            barra.setStyle("-fx-accent: #234C6A;");

            Label lblPct = new Label(String.format("%.0f%%", progreso * 100));
            lblPct.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #1B3C53;");

            HBox fila = new HBox(10, lblNombre, barra, lblPct);
            fila.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            vboxSegmentacion.getChildren().add(fila);
        }
    }
}