package com.cdac.enrollmentstation.controller;

import com.cdac.enrollmentstation.App;
import com.cdac.enrollmentstation.exception.GenericException;
import com.cdac.enrollmentstation.logging.ApplicationLog;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HostnameIpController extends AbstractBaseController {
    private static final Logger LOGGER = ApplicationLog.getLogger(HostnameIpController.class);
    @FXML
    private Label messageLabel;
    @FXML
    private TextField hostnameTextField;
    @FXML
    private TextField ipAddressTextField;
    @FXML
    private TextField subnetMaskTextField;
    @FXML
    private TextField defaultGatewayTextField;
    @FXML
    private TextField dnsIpTextField;
    @FXML
    private Button saveBtn;
    @FXML
    private Button backBtn;
    @FXML
    private Button homeBtn;

    private String interfaceName;
    private String hostname;

    public void initialize() {
        backBtn.setOnAction(event -> backBtnAction());
        saveBtn.setOnAction(event -> saveBtnAction());
        homeBtn.setOnAction(event -> homeBtnAction());
        interfaceName = getInterfaceName();
        hostname = getHostname();
        setTextFieldValuesOnUI(); // only set after getting all required fields
    }

    private void homeBtnAction() {
        try {
            App.setRoot("main_screen");
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, () -> "Error loading fxml: " + ex.getMessage());
        }
    }


    private void backBtnAction() {
        try {
            App.setRoot("admin_auth");
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, () -> "Error loading fxml: " + ex.getMessage());
        }
    }

    private void saveIpaddressToFile() throws IOException {
        String content = "auto lo\niface lo inet loopback\n" + "\nauto " + interfaceName +
                "\niface " + interfaceName + " inet static" +
                "\naddress\t" + ipAddressTextField.getText() +
                "\nnetmask\t" + subnetMaskTextField.getText() +
                "\ngateway\t" + defaultGatewayTextField.getText() +
                "\ndns-nameservers\t" + dnsIpTextField.getText() +
                "\n";
        Files.writeString(Paths.get("/etc/network/interfaces"), content);
    }


    private String getInterfaceName() {
        try {
            Process process = Runtime.getRuntime().exec("ip -o link show");
            BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            LinkedList<String> interfaces = new LinkedList<>();
            String line;
            while ((line = input.readLine()) != null) {
                interfaces.add(line);
            }
            if (interfaces.size() < 2) {
                LOGGER.log(Level.INFO, () -> "Error: Number of interfaces is less than 2 (including loopback)");
                throw new GenericException("Failed to get interface name.");
            }
            input.close();
            int exitVal = process.waitFor();
            if (exitVal != 0) {
                LOGGER.log(Level.INFO, () -> "***Error: Process Exit Value: " + exitVal);
                throw new GenericException("Process exited with error code.");
            }
            return interfaces.get(1).split(":")[1].trim();
        } catch (Exception ex) {
            LOGGER.log(Level.INFO, () -> "***Error: " + ex.getMessage());
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new GenericException(ex.getMessage());
        }
    }

    private void saveBtnAction() {
        String regex = "^[a-zA-Z0-9](?:[a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?$"; // linux hostname
        if (!hostnameTextField.getText().matches(regex)) {
            messageLabel.setText("Enter a valid hostname(alphanumeric chars).");
            return;
        }
        regex = "^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$"; // ipv4 address

        if (!ipAddressTextField.getText().matches(regex)) {
            messageLabel.setText("Enter a valid ip address.");
            return;
        }
        if (!subnetMaskTextField.getText().matches(regex)) {
            messageLabel.setText("Enter a valid subnet mask.");
            return;
        }
        if (!defaultGatewayTextField.getText().isBlank() && (!defaultGatewayTextField.getText().matches(regex))) {
            messageLabel.setText("Enter a valid default gateway.");
            return;
        }

        if (!dnsIpTextField.getText().isBlank() && (!dnsIpTextField.getText().matches(regex))) {
            messageLabel.setText("Enter a valid dns ip.");
            return;
        }

        try {
            if (!hostname.equals(hostnameTextField.getText())) {
                setHostname();
            }
            saveIpaddressToFile();
            restartNetworkingService();
        } catch (Exception ex) {
            LOGGER.log(Level.INFO, () -> "***Error: " + ex.getMessage());
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            messageLabel.setText(ex.getMessage());
            return;
        }
        LOGGER.log(Level.INFO, () -> "System configuration saved successfully.");
        messageLabel.setText("System configuration saved successfully.");
    }

    private void setTextFieldValuesOnUI() {
        try {
            List<String> lines = Files.readAllLines(Path.of("/etc/network/interfaces"));
            lines.forEach(line -> {
                line = line.trim();
                String[] entry = line.split("\\s+");
                if (entry[0].equalsIgnoreCase("address")) {
                    ipAddressTextField.setText(entry[1]);
                } else if (entry[0].equalsIgnoreCase("netmask")) {
                    subnetMaskTextField.setText(entry[1]);
                } else if (entry[0].equalsIgnoreCase("gateway")) {
                    defaultGatewayTextField.setText(entry[1]);
                } else if (entry[0].equalsIgnoreCase("dns-nameservers")) {
                    dnsIpTextField.setText(entry[1]);
                }
            });
            hostnameTextField.setText(hostname);
        } catch (Exception ex) {
            LOGGER.log(Level.INFO, () -> "***Error: " + ex.getMessage());
            messageLabel.setText(ex.getMessage());
        }
    }

    private String getHostname() {
        try {
            Process process = Runtime.getRuntime().exec("hostname");
            BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = input.readLine();
            if (line == null) {
                LOGGER.log(Level.INFO, () -> "***Error: Received null value hostname");
                throw new GenericException("Failed to get hostname.");
            }
            input.close();
            int exitVal = process.waitFor();
            if (exitVal != 0) {
                LOGGER.log(Level.INFO, () -> "***Error: Process Exit Value: " + exitVal);
                throw new GenericException("Failed to get hostname.");
            }
            return line;
        } catch (Exception ex) {
            LOGGER.log(Level.INFO, () -> "***Error: " + ex.getMessage());
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new GenericException(ex.getMessage());
        }
    }

    private void setHostname() throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec("hostnamectl set-hostname " + hostnameTextField.getText());
        int exitVal = process.waitFor();
        if (exitVal != 0) {
            LOGGER.log(Level.INFO, () -> "***Error: Process Exit Value: " + exitVal);
            throw new GenericException("Failed to set hostname.");
        }
        hostname = hostnameTextField.getText();
    }

    private void restartNetworkingService() throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec("systemctl restart networking");
        int exitVal = process.waitFor();
        if (exitVal != 0) {
            LOGGER.log(Level.INFO, () -> "***Error: Process Exit Value: " + exitVal);
            throw new GenericException("Failed to restart networking service.");
        }
        hostname = hostnameTextField.getText();
    }


    @Override
    public void onUncaughtException() {
        LOGGER.log(Level.INFO, "***Unhandled exception occurred.");
    }
}
