INSERT INTO paradas (nombre, tipo, ubicacion)
VALUES ('Independencia', 'METRO', 'Av. 30 de Marzo, SD'),
       ('PUCMM', 'BUS', 'Av. Estrella Sadahla, Stgo'),
       ('Romana', 'TREN', 'Calle Castillo, La Romana'),
       ('Mercado', 'METRO', 'Av. Hermanas Mirabal, Stgo'),
       ('AILA', 'TAXI', 'Autopista Las Américas, SDE'),
       ('Colonial', 'TAXI', 'Calle El Conde, SD'),
       ('Malecón', 'METRO', 'Av. George Washington, SD'),
       ('OMSA', 'BUS', 'Av. Máximo Gómez, SD'),
       ('Boca Chica', 'TREN', 'Av. Circunvalación, SDE'),
       ('Lama', 'METRO', 'Av. Duarte, SD');

INSERT INTO rutas (nombre, origen, destino, tiempo, costo, distancia, transbordos)
VALUES ('Ruta E01', 1, 2, 5, 8, 12.5, 0),
       ('Ruta E02', 2, 3, 3, 6, 8.0, 0),
       ('Ruta E03', 3, 4, 4, 15, 22.0, 0),
       ('Ruta E04', 4, 5, 2, 12, 18.0, 1),
       ('Ruta E05', 5, 6, 6, 20, 15.0, 0),
       ('Ruta E06', 6, 7, 2, 5, 10.0, 0),
       ('Ruta E07', 7, 8, 3, 10, 14.0, 0),
       ('Ruta E08', 8, 9, 4, 18, 22.0, 1),
       ('Ruta E09', 9, 10, 5, 20, 18.0, 0),
       ('Ruta E10', 10, 1, 2, 9, 12.0, 0);
       ('Ruta E11', 1, 3, 6, 12, 18.0, 0),
       ('Ruta E12', 1, 4, 8, 22, 28.0, 1),
       ('Ruta E13', 4, 6, 5, 14, 20.0, 0),
       ('Ruta E14', 1, 6, 12, 30, 35.0, 2),
       ('Ruta E15', 6, 8, 4, 12, 16.0, 0),
       ('Ruta E16', 8, 10, 7, 16, 25.0, 1),
       ('Ruta E17', 6, 10, 9, 25, 30.0, 1);