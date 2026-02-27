package logico.algoritmos;

public enum TipoTransporte {

    TAXI("Parada de Taxi"),
    BUS("Estación de Bus"),
    TREN("Estación de Tren");

    String descripcion;
    TipoTransporte(String descripcion) {

        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

}