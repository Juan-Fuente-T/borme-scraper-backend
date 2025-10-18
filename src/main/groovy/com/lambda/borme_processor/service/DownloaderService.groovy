package com.lambda.borme_processor.service;

import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.security.KeyStore;

/**
 * Servicio especializado para descargar activos desde fuentes externas
 * que requieren una configuración SSL/TLS específica, como el BOE.
 */
@Service
public class DownloaderService {

    private final SSLContext sslContext;

    /**
     * El constructor inicializa el contexto SSL una sola vez al crear el servicio,
     * haciéndolo reutilizable y eficiente.
     */
    public DownloaderService() {
        this.sslContext = createSslContext();
    }

    /**
     * Expone el SSLContext para que otras unidades puedan usarlo para conexiones
     * al mismo dominio fortificado.
     * @return La instancia única del SSLContext.
     */
    public SSLContext getSslContext() {
        return this.sslContext;
    }
    /**
     * Descarga el contenido de una URL como un array de bytes.
     * Utiliza la configuración Jsoup fortificada que ha sido verificada en combate.
     * @param url La URL del recurso a descargar.
     * @return El contenido del recurso como byte[].
     * @throws RuntimeException si la descarga falla.
     */
    public byte[] downloadFromUrl(String url) {
        try {
            System.out.println("[UNIDAD DE DESCARGA] Asegurando activo desde: " + url);
            return Jsoup.connect(url)
                    .sslSocketFactory(this.sslContext.getSocketFactory())
                    .ignoreContentType(true)
                    .maxBodySize(0) // Permite descargar ficheros de cualquier tamaño
                    .execute()
                    .bodyAsBytes();
        } catch (Exception e) {
            System.err.println("!! FALLO CRÍTICO en la Unidad de Descarga para la URL: " + url);
            e.printStackTrace();
            // Envolvemos la excepción para que sea manejada por el GlobalExceptionHandler
            throw new RuntimeException("Fallo irrecuperable durante la descarga desde " + url, e);
        }
    }

    /**
     * Crea y configura el contexto SSL personalizado para Jsoup, utilizando el truststore.
     * Este método es ahora privado y parte de la lógica interna de esta unidad especializada.
     */
    private SSLContext createSslContext() {
        try {
            var trustStoreFile = getClass().getResourceAsStream("/truststore.p12");
            if (trustStoreFile == null) {
                throw new RuntimeException("No se pudo encontrar 'truststore.p12' en los recursos.");
            }
            KeyStore trustStore = KeyStore.getInstance("PKCS12");
            trustStore.load(trustStoreFile, "changeit".toCharArray());
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustStore);
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);
            return sslContext;
        } catch (Exception e) {
            throw new RuntimeException("Fallo crítico durante la configuración del contexto SSL.", e);
        }
    }
}
