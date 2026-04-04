CREATE TABLE IF NOT EXISTS paradas
(
    codigo    SERIAL  NOT NULL,
    nombre    VARCHAR(100) NOT NULL,
    tipo      VARCHAR(50)  NOT NULL,
    ubicacion VARCHAR(200),
    CONSTRAINT paradas_pkey PRIMARY KEY (codigo)
    );

CREATE TABLE IF NOT EXISTS rutas
(
    id          SERIAL NOT NULL,
    nombre      VARCHAR(100) NOT NULL,
    origen      INTEGER  NOT NULL,
    destino     INTEGER  NOT NULL,
    tiempo      DOUBLE PRECISION,
    costo       DOUBLE PRECISION,
    distancia   DOUBLE PRECISION,
    transbordos INTEGER,
    CONSTRAINT rutas_pkey    PRIMARY KEY (id),
    CONSTRAINT rutas_origen  FOREIGN KEY (origen)  REFERENCES paradas(codigo) ON DELETE CASCADE,
    CONSTRAINT rutas_destino FOREIGN KEY (destino) REFERENCES paradas(codigo) ON DELETE CASCADE,
    CONSTRAINT rutas_unica   UNIQUE (origen, destino)
    );