package com.cibertec.apptecnotienda.controller;

import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.cibertec.apptecnotienda.model.Product;
import com.cibertec.apptecnotienda.repository.IProductRepository;

@RestController
public class JasperReportsController {
	// Declaracion e Inicializacion de objetos
	@Autowired
	private IProductRepository productRepository;
	
	@GetMapping("/GenerateJasperReport")
    public ResponseEntity<byte[]> generarInforme() throws Exception {
        // Cargar el informe JRXML y compilarlo
    	ClassPathResource resource = new ClassPathResource("static/ReportTecnotienda.jrxml");
        JasperReport jasperReport = JasperCompileManager.compileReport(resource.getInputStream());

        // Obtener datos 
        List<Product> productos = productRepository.findAll();
            
        // Filtrar los productos con stock menor a 50
        List<Product> productosConStockBajo = productos.stream()
                .filter(producto -> Integer.parseInt(producto.getStock()) < 50)
                .collect(Collectors.toList());
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(productosConStockBajo);

        // Parámetros para el informe
        Map<String, Object> params = new HashMap<>();

        // Generar el informe
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, dataSource);

        // Exportar el informe a bytes en PDF
        byte[] informeBytes = JasperExportManager.exportReportToPdf(jasperPrint);

        // Devolver el informe
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename", "informe.pdf");

        return new ResponseEntity<>(informeBytes, headers, HttpStatus.OK);
    }
}
