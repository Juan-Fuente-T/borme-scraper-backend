package com.lambda.borme_processor.service

import com.lambda.borme_processor.entity.Company
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import static com.lambda.borme_processor.utils.DateUtils.parseDate // Importa el método estático
@Service
class BormeParserService {

    /**
     * Procesa el texto completo de un BORME y devuelve una lista de empresas constituidas.
     * Se basa en patrones que detectan bloques de texto que empiezan con un número (ID del asiento)
     * y contienen la palabra "Constitución".
     */
    List<Company> extractCompaniesFromText(String text, String pdfPath) {
        // Divide el texto por saltos de línea dobles o patrones de numeración
        def blocks = text.split(/(?=\d{6}\s+-)/).toList()
        def companies = []

        blocks.eachWithIndex { rawBlock, i ->
            // Solo interesan los bloques que contengan la palabra "Constitucion"
            if (!rawBlock.toLowerCase().contains("constitución")) {
                return
            }

            // Normaliza el texto eliminando saltos de línea innecesarios
            //def block = rawBlock.replaceAll("\\s+", " ").trim()
            def block = rawBlock.trim()

            def company = new Company()
            //company.pdfPath = pdfPath

            // === ID y nombre ===
            //def patternIdName = ~/(?m)^(\d{6})\s*-\s*(.+)/
            def patternIdName = ~/(?m)^(\d{1,10})\s*-\s*(.+)/
            def mIdName = patternIdName.matcher(block)
            if (mIdName.find()) {
                company.bormeId = clean(mIdName.group(1))
                company.name = clean(mIdName.group(2))
            }else {
            println "[FALLO REGEX] No encuentro ID en este bloque: " + block.take(50)
        }

            // === Tipo de acto ===
            //def patternAct = ~/(?i)(Constitucion|Nombramientos|Ceses|Declaracion de unipersonalidad)/
            //            def mAct = patternAct.matcher(block)
            //            if (mAct.find()) {
            //                company.actType = clean(mAct.group(1)?.capitalize())
            //            }
            // Se fuerza "Constitución" sin regex múltiple
            company.actType = "Constitución"

            // === Comienzo de operaciones ===
            def mStart = (block =~ /Comienzo de operaciones:\s*([\d.]+)/)
            if (mStart.find()) {
                company.startDate = parseDate(clean(mStart.group(1)))
            }

            // === Objeto social ===
            def patternObj = ~/(?i)Objeto social:\s*(.+?)(?=Domicilio:|Capital:|Declaracion|Nombramientos|Datos registrales)/
            def mObj = patternObj.matcher(block)
            if (mObj.find()) {
                company.object = clean(mObj.group(1))
            }

            // === Domicilio ===
            def patternAddr = ~/(?i)Domicilio:\s*(.+?)(?=Capital:|Declaracion|Nombramientos|Datos registrales)/
            def mAddress = patternAddr.matcher(block)
            if (mAddress.find()) {
                company.address = clean(mAddress.group(1))
            }

            // === Capital social ===
            String capitalStr = ''
            def patternCap = ~/(?i)Capital:\s*([0-9.,]+ Euros)/
            def mCapital = patternCap.matcher(block)
            if (mCapital.find()) {
                capitalStr = clean(mCapital.group(1))
                company.capital = capitalStr
            }

            // Limpia el texto y lo convierte a un número (en céntimos para evitar decimales)
            long capitalNum = 0
            if (!capitalStr.isEmpty()) {
                try {
                    // 1. Quita to.do lo que no sea un dígito o una coma.
                    String numericPart = capitalStr.replaceAll("[^\\d,]", "")
                    // 2. Reemplaza la coma decimal por nada para tener los céntimos.
                    numericPart = numericPart.replace(",", "")
                    // 3. Convierte a Long.
                    capitalNum = numericPart.toLong()
                } catch (NumberFormatException e) {
                    println "[ALERTA DE PARSEO] No se pudo convertir el capital a número: '${capitalStr}'"
                    // Se deja en 0 si falla.
                }
            }
            company.capitalNumeric = capitalNum

            // === Socio único ===
            def patternPartner = ~/(?i)Socio único:\s*([A-ZÁÉÍÓÚÑ ]+)/
            def mSolePartner = patternPartner.matcher(block)
            if (mSolePartner.find()) {
                company.solePartner = clean(mSolePartner.group(1))
            }

            // === Administrador ===
            def patternAdmin = ~/(?i)Adm\.\s*(Unico|Solid\.):\s*([A-ZÁÉÍÓÚÑ ]+)/
            def mAdmin = patternAdmin.matcher(block)
            if (mAdmin.find()) {
                company.admin = clean(mAdmin.group(2))
            }

            // === Datos registrales ===
            def patternReg = ~/(?i)Datos registrales\.\s*([^\n]+)/
            def mReg = patternReg.matcher(block)
            if (mReg.find()) {
                company.registryData = clean(mReg.group(1))
            }

            company.createdAt = LocalDateTime.now()

            if (company.bormeId != null && !company.bormeId.isBlank()) {
                companies << company
            } else {
                println "[ALERTA DE PARSEO] Se ha descartado un registro por falta de BORME ID en el bloque: ${rawBlock.take(100)}..."
            }
        }

        println "[PARSER] Parseo completado: ${companies.size()} constituciones detectadas."
        return companies
    }


    /**
     * Limpia un texto eliminando espacios y puntuación sobrante al final.
     * Ejemplo: "VIDAL RICO RICARDO." -> "VIDAL RICO RICARDO"
     *          "3.000,00 Euros." -> "3.000,00 Euros"
     */
    /**
     * Limpia un texto eliminando espacios o puntuación sobrante al final.
     * Aplica a cualquier campo textual del BORME.
     */
    private static String clean(String text) {
        if (!text) return null
        return text
                .replaceAll(/[ \t\n\r]+$/, '')  // limpia espacios finales
                .replaceAll(/[.,;:]+$/, '')     // elimina puntuación final suelta
                .trim()
    }

}
