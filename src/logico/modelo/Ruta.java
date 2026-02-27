package logico.modelo;

public class Ruta {

    private String id;
    private Parada origen;
    private Parada destino;
    private float tiempo;
    private float distancia;
    private double costo;
    private int transbordos;
    private TipoTransporte tipoTransporte;

    public Ruta(String id, Parada origen, Parada destino, float tiempo, float distancia, double costo) {
        this.id = id;
        this.origen = origen;
        this.destino = destino;
        this.tiempo = tiempo;
        this.distancia = distancia;
        this.costo = costo;
        this.transbordos = 0;
        this.tipoTransporte = null;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public Parada getOrigen() {
        return origen;
    }
    public Parada getDestino() {
        return destino;
    }
    public float getTiempo() {
        return tiempo;
    }
    public float getDistanciaKm() {
        return distancia;
    }
    public double getCosto() {
        return costo;
    }
    public int getTransbordos() {
        return transbordos;
    }
    public TipoTransporte getTipoTransporte() {
        return tipoTransporte;
    }

    public void setOrigen(Parada origen) {
        this.origen = origen;
    }
    public void setDestino(Parada destino) {
        this.destino = destino;
    }
    public void setTiempo(float tiempo) {
        this.tiempo = tiempo;
    }
    public void setdistancia(float distancia) {
        this.distancia = distancia;
    }
    public void setCosto(double costo) {
        this.costo = costo;
    }
    public void setTransbordos(int transbordos) {
        this.transbordos = transbordos;
    }
    public void setTipoTransporte(TipoTransporte tipoTransporte) {
        this.tipoTransporte = tipoTransporte;
    }
}
