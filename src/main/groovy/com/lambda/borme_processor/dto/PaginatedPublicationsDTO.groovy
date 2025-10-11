package com.lambda.borme_processor.dto

class PaginatedPublicationsDTO {
    boolean success
    String message
    long total
    int totalPages
    int currentPage
    int pageSize
    List<BormePublicationDTO> publications // <-- Contiene lista de publicaciones
}
