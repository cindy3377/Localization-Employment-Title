package org.example.demo;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.*;
import java.util.*;

public class AdvancedLocalizationController {
    @FXML
    private ComboBox<String> languageSelector;

    @FXML
    private ListView<String> employeeList;

    @FXML
    private TextField translationKeyTextField;

    @FXML
    private TextField translationEnterTextField;

    @FXML
    private Button updateTranslationBtn;


    private static final String DB_URL = "jdbc:mariadb://localhost:3306/employmentDatabase";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "test123";

    @FXML
    public void initialize() {
        System.out.println("employeeList is: " + employeeList);
        languageSelector.getItems().addAll("English", "French", "Spanish", "日本語");
        languageSelector.setValue("English");
        languageSelector.setOnAction(event -> changeLanguage());
        fetchLocalizedData("en");
    }

    @FXML
    private void changeLanguage() {
        String languageCode = getLanguageCode(languageSelector.getValue());
        fetchLocalizedData(languageCode);
    }

    @FXML
    public void fetchLocalizedData(String languageCode) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            employeeList.getItems().clear();

            String query = "SELECT Key_name, translation_text FROM translations WHERE Language_code = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, languageCode);
            ResultSet rs = stmt.executeQuery();

            boolean hasResults = false;
            while (rs.next()) {
                hasResults = true;
                String keyName = rs.getString("Key_name");
                String translation = rs.getString("translation_text");
                employeeList.getItems().add(keyName + ": " + translation);
            }

            if (!hasResults) {
                employeeList.getItems().add("No job titles found for this language.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Database Error", "Error fetching job titles.");
        }
    }



    @FXML
    public void saveLocalizedData() {
        String languageCode = getLanguageCode(languageSelector.getValue());
        String keyName = translationKeyTextField.getText().trim();
        String jobTitle = translationEnterTextField.getText().trim();

        if (keyName.isEmpty() || jobTitle.isEmpty()) {
            showError("Input Error", "Please enter both a key and a translation.");
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String insertTranslation = "INSERT INTO translations (Key_name, Language_code, translation_text) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(insertTranslation);
            stmt.setString(1, keyName);
            stmt.setString(2, languageCode);
            stmt.setString(3, jobTitle);
            stmt.executeUpdate();

            showSuccess("Success", "New job title added successfully!");
            fetchLocalizedData(languageCode); // Refresh the list
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Database Error", "Error saving job title.");
        }
    }

    private void showSuccess(String success, String s) {
    }


    private String getLanguageCode(String language) {
        Map<String, String> languageMap = new HashMap<>();
        languageMap.put("English", "en");
        languageMap.put("French", "fr");
        languageMap.put("Spanish", "es");
        languageMap.put("日本語", "ja");
        return languageMap.getOrDefault(language, "en");
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void onUpdateTranslationButtonClick(ActionEvent actionEvent) {
        saveLocalizedData();
        fetchLocalizedData(getLanguageCode(languageSelector.getValue())); // Refresh list
    }
}
