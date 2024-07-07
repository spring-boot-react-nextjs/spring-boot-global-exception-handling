package com.example.exception.exception;

import com.example.exception.i18n.I18nService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ProblemDetail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@Slf4j
@SpringBootTest
class GlobalExceptionHandlerTest {

    private static final String ERROR_URI = "https://example.com/error";
    private static final String ERROR_MESSAGE = "Test error message";
    private static final String I18N_MESSAGE = "Test error message (localized)";
    private static final int HTTP_STATUS_NOT_FOUND = 404;

    @Mock
    private I18nService i18nService;

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        globalExceptionHandler.setErrorUri(ERROR_URI);
    }

    @Test
    void testHandleResourceNotFoundException() {
        ResourceNotFoundExceptionWithI18n ex = new ResourceNotFoundExceptionWithI18n(ERROR_MESSAGE, null);
        when(i18nService.getMessage(ERROR_MESSAGE, (String) null)).thenReturn(I18N_MESSAGE);

        ProblemDetail pd = globalExceptionHandler.handleResourceNotFoundExceptionWithI18n(ex);

        assertEquals(HTTP_STATUS_NOT_FOUND, pd.getStatus());
        assertEquals(I18N_MESSAGE, pd.getDetail());
        assertEquals(ERROR_URI, pd.getType().toString());
    }
}