import java.io.*;
import java.text.Normalizer;
import java.util.*;

public class java_chatbot {

    static Map<String, String> conocimiento = new HashMap<>();
    static Map<String, List<String>> palabrasClave = new HashMap<>();
    static final String ARCHIVO_CONOCIMIENTO = "conocimiento.txt";
    static final String ARCHIVO_SUGERENCIAS = "sugerencias.txt";

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        cargarConocimientoBase();
        cargarConocimientoArchivo();

        System.out.println("Bot: ¡Hola, sobrino! Soy tu bot aprendiz. Pregunta algo o enséñame con: ensenar: pregunta | respuesta");

        while (true) {
            System.out.print("Tú: ");
            String input = sc.nextLine().trim();

            if (input.equalsIgnoreCase("salir")) {
                System.out.println("Bot: Chau! Me voy a seguir entrenando. 💪");
                break;
            }

            if (input.toLowerCase().startsWith("ensenar:")) {
                manejarEnsenar(input);
            } else {
                String respuesta = responder(input);
                System.out.println("Bot: " + respuesta);
            }
        }

        sc.close();
    }

    public static void manejarEnsenar(String input) {
        try {
            String contenido = input.substring(8).trim(); // quitar "ensenar:"
            String[] partes = contenido.split("\\|", 2);

            if (partes.length == 2) {
                String preguntaOriginal = partes[0].trim();
                String respuesta = partes[1].trim();

                if (!preguntaOriginal.isEmpty() && !respuesta.isEmpty()) {
                    String preguntaNorm = normalizar(preguntaOriginal);
                    conocimiento.put(preguntaNorm, respuesta);
                    palabrasClave.put(preguntaNorm, extraerPalabrasClave(preguntaOriginal));
                    guardarConocimientoArchivo(preguntaOriginal, respuesta);
                    System.out.println("Bot: ¡Ya aprendí eso, sensei! 🧠✅");
                } else {
                    System.out.println("Bot: Hmm... ¿vacío? ¡No me dejes en blanco!");
                }
            } else {
                System.out.println("Bot: Formato incorrecto. Usa: ensenar: pregunta | respuesta");
            }
        } catch (Exception e) {
            System.out.println("Bot: Algo se me trabó el chip. Intenta de nuevo con: ensenar: pregunta | respuesta");
        }
    }

    public static String responder(String inputUsuario) {
        String inputNorm = normalizar(inputUsuario);
        List<String> inputPalabras = extraerPalabrasClave(inputUsuario);

        int umbral = 2;

        for (String preguntaGuardada : conocimiento.keySet()) {
            List<String> claves = palabrasClave.get(preguntaGuardada);
            int coincidencias = 0;

            for (String palabra : inputPalabras) {
                if (claves.contains(palabra)) {
                    coincidencias++;
                }
            }

            if (coincidencias >= umbral) {
                return conocimiento.get(preguntaGuardada);
            }
        }

        // Entrenamiento no supervisado: guarda la pregunta no entendida
        guardarSugerenciaNoSupervisada(inputUsuario);
        return "Hmm... no entendí eso. ¿Me lo enseñas? Usa: ensenar: pregunta | respuesta";
    }

    public static String normalizar(String texto) {
        texto = texto.toLowerCase();
        texto = Normalizer.normalize(texto, Normalizer.Form.NFD);
        texto = texto.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        texto = texto.replaceAll("[^a-z0-9\\s]", "");
        texto = texto.trim().replaceAll("\\s+", " ");
        return texto;
    }

    public static List<String> extraerPalabrasClave(String texto) {
        texto = normalizar(texto);
        String[] palabras = texto.split("\\s+");
        List<String> claves = new ArrayList<>();
        for (String p : palabras) {
            if (!p.isEmpty()) claves.add(p);
        }
        return claves;
    }

    public static void cargarConocimientoBase() {
        String[][] baseDatos = {
            {"cuanto cuesta el producto x", "puedes buscar el producto x en el inventario para ver su precio actual"},
            {"puedo modificar el precio de un producto", "sí, edita el producto y cambia el precio en el campo correspondiente"},
            {"como realizo un pedido nuevo", "haz clic en nuevo pedido, selecciona los productos y confirma"},
            {"que pasa si no carga la pagina", "revisa tu conexión o contacta al administrador del sistema"},
            {"puedo cambiar mi contraseña", "sí, desde el panel de usuario puedes modificar tu contraseña actual"},
            {"hola", "¡Hola sobrino! ¿Todo bien? 😄"},
            {"como agrego un nuevo cliente", "ve a la sección de clientes y haz clic en nuevo cliente"}
        };

        for (String[] par : baseDatos) {
            String preguntaNorm = normalizar(par[0]);
            conocimiento.put(preguntaNorm, par[1]);
            palabrasClave.put(preguntaNorm, extraerPalabrasClave(par[0]));
        }
    }

    public static void cargarConocimientoArchivo() {
        File archivo = new File(ARCHIVO_CONOCIMIENTO);
        if (!archivo.exists()) {
            try {
                archivo.createNewFile();
            } catch (IOException e) {
                System.out.println("Bot: No pude crear el archivo de conocimiento. 😢");
            }
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split("\\|", 2);
                if (partes.length == 2) {
                    String pregunta = partes[0].trim();
                    String respuesta = partes[1].trim();
                    String preguntaNorm = normalizar(pregunta);
                    conocimiento.put(preguntaNorm, respuesta);
                    palabrasClave.put(preguntaNorm, extraerPalabrasClave(pregunta));
                }
            }
        } catch (IOException e) {
            System.out.println("Bot: Hubo un error al leer el archivo de conocimiento. 😵");
        }
    }

    public static void guardarConocimientoArchivo(String pregunta, String respuesta) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ARCHIVO_CONOCIMIENTO, true))) {
            bw.write(pregunta + " | " + respuesta);
            bw.newLine();
        } catch (IOException e) {
            System.out.println("Bot: No pude guardar el conocimiento. Ayuda humana necesaria. 🆘");
        }
    }

    public static void guardarSugerenciaNoSupervisada(String preguntaNoComprendida) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ARCHIVO_SUGERENCIAS, true))) {
            bw.write(preguntaNoComprendida);
            bw.newLine();
        } catch (IOException e) {
            System.out.println("Bot: Fallé guardando la sugerencia no supervisada. 🤖💀");
        }
    }
}





