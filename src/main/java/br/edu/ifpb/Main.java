package br.edu.ifpb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootApplication
public class Main {

    public static void main(String[] args) {
        String folderPath = "/Users/rocha/Downloads/artigos/";

        try {
            // Obtém a lista de arquivos .sql na pasta
            File folder = new File(folderPath);
            File[] files = folder.listFiles((dir, name) -> name.endsWith(".sql"));

            if (files != null) {
                // Mapeia os IDs repetidos e seus novos IDs sequenciais
                Map<Integer, Integer> idMappings = new HashMap<>();
                int newId = 1;

                System.out.println(files.length);
                for (File file : files) {
                    Path filePath = Paths.get(file.getAbsolutePath());

                    // Cria um novo arquivo com o sufixo "_new" no nome
                    String newFileName = file.getName().replace(".sql", "_new.sql");
                    Path newFilePath = filePath.resolveSibling(newFileName);

                    try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()));
                         BufferedWriter writer = new BufferedWriter(new FileWriter(newFilePath.toFile()))) {

                        String line;
                        while ((line = reader.readLine()) != null) {
                            // Verifica se a linha contém um INSERT e o campo ID
                            if ( extractIdFromLineWithoutInsert(line) != -1 ) {
                                line = replaceIdInLine(line, newId++);
                            } else {
                                System.out.println("linha nao contemplada: " + line);
                            }

                            // Escreve a linha no novo arquivo
                            writer.write(line);
                            writer.newLine();
                        }
                    }
                }

            } else {
                System.out.println("Não foram encontrados arquivos .sql na pasta especificada.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String replaceIdInLine(String line, int newId) {
        Pattern pattern = Pattern.compile("(\\()\\s*\\d+\\s*(,)");
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            return matcher.replaceFirst("$1 " + newId + " $2");
        }
        return line;
    }

    private static int extractIdFromLineWithoutInsert(String line) {
        Pattern pattern = Pattern.compile("\\s*(\\d+)\\s*,.*");
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return -1; // Valor inválido para o ID
    }

}