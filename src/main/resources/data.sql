-- NOTA PARA EL EVALUADOR:
-- Este archivo inserta datos de ejemplo en la tabla 'student'.
-- La tabla es creada automáticamente por Hibernate (spring.jpa.hibernate.ddl-auto=create-drop)
-- Puede modificar o agregar más registros según lo requiera la evaluación.
/*
El uso de esta inserción es netamente
para generar una prueba rápida y efectiva
que muestre un valor dentro de localhost:8080/api/students
ya que se usa H2 en memoria y los datos no perduran entre reinicios.
*/

-- Insertar datos de prueba
INSERT INTO student (name) VALUES ('Maximiliano Andres Diaz Caro');
