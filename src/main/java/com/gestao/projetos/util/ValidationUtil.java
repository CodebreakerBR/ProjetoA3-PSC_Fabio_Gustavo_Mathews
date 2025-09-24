package com.gestao.projetos.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/**
 * Classe utilitária para validações diversas
 */
public class ValidationUtil {
    
    // Padrões de validação
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@" +
        "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );
    
    private static final Pattern CPF_PATTERN = Pattern.compile("\\d{11}");
    
    // Formatadores de data
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Valida se um texto não é nulo nem vazio
     */
    public static boolean isNotEmpty(String text) {
        return text != null && !text.trim().isEmpty();
    }

    /**
     * Valida se um texto tem o tamanho mínimo exigido
     */
    public static boolean hasMinLength(String text, int minLength) {
        return isNotEmpty(text) && text.trim().length() >= minLength;
    }

    /**
     * Valida se um texto tem o tamanho máximo permitido
     */
    public static boolean hasMaxLength(String text, int maxLength) {
        return text == null || text.length() <= maxLength;
    }

    /**
     * Valida formato de email
     */
    public static boolean isValidEmail(String email) {
        return isNotEmpty(email) && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Valida formato de CPF (apenas dígitos)
     */
    public static boolean isValidCPF(String cpf) {
        if (!isNotEmpty(cpf)) {
            return false;
        }
        
        // Remove caracteres não numéricos
        String cleanCpf = cpf.replaceAll("\\D", "");
        
        // Verifica se tem 11 dígitos e não é uma sequência repetida
        if (!CPF_PATTERN.matcher(cleanCpf).matches() || isRepeatedSequence(cleanCpf)) {
            return false;
        }
        
        // Validação dos dígitos verificadores
        return isValidCPFDigits(cleanCpf);
    }

    /**
     * Verifica se uma string é uma sequência de caracteres repetidos
     */
    private static boolean isRepeatedSequence(String str) {
        return str.chars().allMatch(c -> c == str.charAt(0));
    }

    /**
     * Valida os dígitos verificadores do CPF
     */
    private static boolean isValidCPFDigits(String cpf) {
        try {
            // Primeiro dígito verificador
            int sum = 0;
            for (int i = 0; i < 9; i++) {
                sum += Character.getNumericValue(cpf.charAt(i)) * (10 - i);
            }
            int firstDigit = (sum * 10) % 11;
            if (firstDigit == 10) firstDigit = 0;
            
            if (firstDigit != Character.getNumericValue(cpf.charAt(9))) {
                return false;
            }
            
            // Segundo dígito verificador
            sum = 0;
            for (int i = 0; i < 10; i++) {
                sum += Character.getNumericValue(cpf.charAt(i)) * (11 - i);
            }
            int secondDigit = (sum * 10) % 11;
            if (secondDigit == 10) secondDigit = 0;
            
            return secondDigit == Character.getNumericValue(cpf.charAt(10));
            
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Valida se uma data não é nula
     */
    public static boolean isValidDate(LocalDate date) {
        return date != null;
    }

    /**
     * Valida se uma data é futura
     */
    public static boolean isFutureDate(LocalDate date) {
        return date != null && date.isAfter(LocalDate.now());
    }

    /**
     * Valida se uma data é passada ou presente
     */
    public static boolean isPastOrPresentDate(LocalDate date) {
        return date != null && !date.isAfter(LocalDate.now());
    }

    /**
     * Valida se a data de fim é posterior à data de início
     */
    public static boolean isValidDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return true; // Permite datas nulas
        }
        return !endDate.isBefore(startDate);
    }

    /**
     * Valida se um número está dentro de um intervalo
     */
    public static boolean isInRange(int value, int min, int max) {
        return value >= min && value <= max;
    }

    /**
     * Valida se um ID é válido (não nulo e positivo)
     */
    public static boolean isValidId(Long id) {
        return id != null && id > 0;
    }

    /**
     * Formata uma data para exibição
     */
    public static String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : "";
    }

    /**
     * Formata uma data e hora para exibição
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATETIME_FORMATTER) : "";
    }

    /**
     * Converte string para LocalDate
     */
    public static LocalDate parseDate(String dateStr) {
        if (!isNotEmpty(dateStr)) {
            return null;
        }
        
        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Limpa e formata CPF
     */
    public static String formatCPF(String cpf) {
        if (!isNotEmpty(cpf)) {
            return "";
        }
        
        String cleanCpf = cpf.replaceAll("\\D", "");
        if (cleanCpf.length() == 11) {
            return cleanCpf.substring(0, 3) + "." + 
                   cleanCpf.substring(3, 6) + "." + 
                   cleanCpf.substring(6, 9) + "-" + 
                   cleanCpf.substring(9);
        }
        
        return cpf;
    }

    /**
     * Remove formatação do CPF
     */
    public static String cleanCPF(String cpf) {
        return isNotEmpty(cpf) ? cpf.replaceAll("\\D", "") : "";
    }

    /**
     * Valida senha forte
     */
    public static boolean isStrongPassword(String password) {
        if (!hasMinLength(password, 8)) {
            return false;
        }
        
        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = password.chars().anyMatch(c -> "!@#$%^&*()_+-=[]{}|;:,.<>?".indexOf(c) >= 0);
        
        return hasUpper && hasLower && hasDigit && hasSpecial;
    }

    /**
     * Trunca texto se necessário
     */
    public static String truncate(String text, int maxLength) {
        if (!isNotEmpty(text) || text.length() <= maxLength) {
            return text;
        }
        
        return text.substring(0, maxLength - 3) + "...";
    }

    /**
     * Capitaliza primeira letra de cada palavra
     */
    public static String capitalizeWords(String text) {
        if (!isNotEmpty(text)) {
            return text;
        }
        
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;
        
        for (char c : text.toCharArray()) {
            if (Character.isWhitespace(c)) {
                capitalizeNext = true;
                result.append(c);
            } else if (capitalizeNext) {
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                result.append(Character.toLowerCase(c));
            }
        }
        
        return result.toString();
    }
}
