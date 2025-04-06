package com.techdistribuidora;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

public class FileTransferRoute {

    public static void main(String[] args) throws Exception {

        CamelContext context = new DefaultCamelContext();

        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() {

                // 🟢 Ruta principal: monitorear carpeta input
                from("file:input?noop=true")
                    .log("Archivo recibido para validación: ${header.CamelFileName}")
                    .convertBodyTo(String.class)
                    .choice()
                        .when(body().startsWith("id"))
                            .log("Encabezado válido detectado en archivo: ${header.CamelFileName}")
                            .to("direct:clasificarCliente")
                        .otherwise()
                            .log("Archivo rechazado (sin encabezado): ${header.CamelFileName}")
                            .to("file:output/rechazados")
                    .end();

                // 🔄 Ruta secundaria: clasificación por tipo de cliente
                from("direct:clasificarCliente")
                    .log("Analizando tipo de cliente para: ${header.CamelFileName}")
                    .process(exchange -> {
                        String body = exchange.getIn().getBody(String.class);
                        String[] lineas = body.split("\n");
                        String tipoCliente = "desconocido";

                        for (String linea : lineas) {
                            if (linea.toLowerCase().contains("vip")) {
                                tipoCliente = "VIP";
                                break;
                            } else if (linea.toLowerCase().contains("regular")) {
                                tipoCliente = "regular";
                                break;
                            }
                        }

                        exchange.setProperty("tipoCliente", tipoCliente);
                    })
                    .log("Clasificado como: ${exchangeProperty.tipoCliente}")
                    .toD("file:output/${exchangeProperty.tipoCliente}/")
                    .log("Archivo ${header.CamelFileName} movido a carpeta output/${exchangeProperty.tipoCliente}/");
            }
        });

        // 🚀 Inicio de ejecución
        context.start();
        System.out.println("Ruta Camel en ejecución. Esperando archivos...");
        Thread.sleep(60000); // Esperar 60 segundos (1 minuto)
        context.stop();
        System.out.println("Ruta Camel detenida.");
    }
}
