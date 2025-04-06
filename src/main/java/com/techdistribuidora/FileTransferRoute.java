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
                from("file:input?noop=true")
                .log("Archivo recibido: ${header.CamelFileName}")
                .to("file:output");
            }
        });

        context.start();
        System.out.println("Ruta Camel en ejecución...");
        Thread.sleep(10000); // Espera 10 segundos
        context.stop();
    }
}
