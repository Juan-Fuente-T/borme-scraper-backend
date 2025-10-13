package com.lambda.borme_processor.service;

import com.lambda.borme_processor.dto.ProcessingResultDTO;
import com.lambda.borme_processor.dto.ScrapedFileInfoDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class) // Habilita Mockito
class BormeProcessorServiceIT {

    @Autowired
    private BormeProcessorService processorService;

    @Mock
    private BormeScraperService mockScraperService;

    @Test
    void testProcesamientoCompleto() {
        // Given
        LocalDate testDate = LocalDate.of(2025, 10, 10);
        File resource = new File(getClass().getClassLoader().getResource("test-borme.pdf").toURI());
        ScrapedFileInfoDTO fakeScrapedInfo = new ScrapedFileInfoDTO(
                resource,
                "http://fake.url/test.pdf"
        );

        when(mockScraperService.scrapeAndDownloadPdfs(any(LocalDate.class)))
                .thenReturn(java.util.List.of(fakeScrapedInfo));

        // When
        ProcessingResultDTO result = processorService.processBormeForDate(testDate, false);

        // Then
        assertTrue(result.isSuccess());
        assertTrue(result.getCompaniesFound() > 0);
    }
}