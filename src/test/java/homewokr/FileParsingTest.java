package homewokr;

import com.codeborne.pdftest.PDF;
import com.codeborne.xlstest.XLS;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileParsingTest {

    private final ClassLoader cl = FileParsingTest.class.getClassLoader();

    @DisplayName("Проверка содержимого PDF-файла из ZIP архива")
    @Test
    void pdfInZipTest() throws Exception {
        try (ZipInputStream zis = new ZipInputStream(cl.getResourceAsStream("my.zip")
        )) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().endsWith(".pdf")) {
                    PDF pdf = new PDF(zis);
                    assertTrue(pdf.text.contains("Введение"));
                    break;
                }
            }
        }
    }

    @DisplayName("Проверка содержимого XLSX-файла из ZIP архива")
    @Test
    void xlsxInZipTest() throws Exception {
        try (ZipInputStream zis = new ZipInputStream(cl.getResourceAsStream("my.zip")
        )) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().endsWith(".xlsx")) {
                    XLS xls = new XLS(zis);
                    String testValue = xls.excel.getSheetAt(0).getRow(0).getCell(0).getStringCellValue();
                    assertTrue(testValue.contains("ИНН"));
                    break;
                }
            }
        }
    }

    @DisplayName("Проверка содержимого CSV-файла из ZIP архива")
    @Test
    void csvInZipTest() throws Exception {
        try (ZipInputStream zis = new ZipInputStream(cl.getResourceAsStream("my.zip")
        )) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().endsWith(".csv")) {
                    try (CSVReader csvReader = new CSVReader(new InputStreamReader(zis))) {
                        List<String[]> data = csvReader.readAll();

                        if (!data.isEmpty() && data.get(0).length > 0) {
                            data.get(0)[0] = data.get(0)[0].replace("\uFEFF", "");
                        }
                        Assertions.assertArrayEquals(
                                new String[]{"Инн;"},
                                data.get(0)
                        );
                    }
                    break;
                }
            }
        }
    }

    @DisplayName("Проверка содержимого JSON-файла")
    @Test
    void jsonReadTest() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        try (InputStream is = cl.getResourceAsStream("json.json")) {

            JsonNode root = mapper.readTree(is);

            System.out.println(root);
            Assertions.assertEquals("Иван", root.get("firstName").asText());
            Assertions.assertEquals("Иванов", root.get("lastName").asText());

            JsonNode address = root.get("address");
            Assertions.assertEquals("Московское ш., 101, кв.101", address.get("streetAddress").asText());
            Assertions.assertEquals("Ленинград", address.get("city").asText());
            Assertions.assertEquals(101101, address.get("postalCode").asInt());

            JsonNode phoneNumbers = root.get("phoneNumbers");
            Assertions.assertEquals(2, phoneNumbers.size());
            Assertions.assertEquals("812 123-1234", phoneNumbers.get(0).asText());
            Assertions.assertEquals("916 123-4567", phoneNumbers.get(1).asText());
        }
    }

}



