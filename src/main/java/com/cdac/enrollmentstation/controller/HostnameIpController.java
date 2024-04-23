package com.cdac.enrollmentstation.controller;

import com.cdac.enrollmentstation.App;
import com.cdac.enrollmentstation.constant.ApplicationConstant;
import com.cdac.enrollmentstation.exception.GenericException;
import com.cdac.enrollmentstation.logging.ApplicationLog;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
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

    public void initialize() {
        backBtn.setOnAction(event -> backBtnAction());
        saveBtn.setOnAction(event -> saveBtnAction());
        homeBtn.setOnAction(event -> homeBtnAction());
        interfaceName = getInterfaceName();
        setTextFieldValuesOnUI(); // only set after getting all required fields

        hostnameTextField.setOnKeyPressed(event -> clearMessageLabelIfNotBlank());
        ipAddressTextField.setOnKeyPressed(event -> clearMessageLabelIfNotBlank());
        subnetMaskTextField.setOnKeyPressed(event -> clearMessageLabelIfNotBlank());
        defaultGatewayTextField.setOnKeyPressed(event -> clearMessageLabelIfNotBlank());
        dnsIpTextField.setOnKeyPressed(event -> clearMessageLabelIfNotBlank());
    }

    private void clearMessageLabelIfNotBlank() {
        if (!messageLabel.getText().isBlank()) {
            messageLabel.setText("");
        }
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
                "\nnetmask\t" + subnetMaskTextField.getText();

        if (!defaultGatewayTextField.getText().isBlank()) {
            content += "\ngateway\t" + defaultGatewayTextField.getText();
        }
        if (!dnsIpTextField.getText().isBlank()) {
            content += "\ndns-nameservers\t" + dnsIpTextField.getText();
        }
        content += "\n";

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
        messageLabel.setText("Updating the system configuration. Please wait.");
        disableControls(backBtn, homeBtn, saveBtn);
        App.getThreadPool().execute(this::saveChanges);
    }

    private void disableControls(Node... nodes) {
        for (Node node : nodes) {
            node.setDisable(true);
        }
    }

    private void enableControls(Node... nodes) {
        for (Node node : nodes) {
            node.setDisable(false);
        }
    }

    private void saveChanges() {
        try {
            saveIpaddressToFile();
            if (!getHostname().equals(hostnameTextField.getText())) {
                setHostname();
                // reboot system
                App.getThreadPool().execute(this::rebootSystem);
                return;
            }
            restartNetworkingService();
        } catch (Exception ex) {
            enableControls(backBtn, homeBtn, saveBtn);
            LOGGER.log(Level.INFO, () -> "***Error: " + ex.getMessage());
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            updateUI(ex.getMessage());
            return;
        }
        enableControls(backBtn, homeBtn, saveBtn);
        LOGGER.log(Level.INFO, () -> "System configuration saved successfully.");
        updateUI("System configuration updated successfully.");
    }

    private void rebootSystem() {
        try {
            int counter = 5;
            while (counter >= 1) {
                updateUI("Rebooting system to take effect in " + counter + " second(s)...");
                Thread.sleep(1000);
                counter--;
            }
            Process process = Runtime.getRuntime().exec("reboot");
            BufferedReader error = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String eline;
            while ((eline = error.readLine()) != null) {
                String finalEline = eline;
                LOGGER.log(Level.INFO, () -> "***Error: " + finalEline);
            }
            error.close();
            int exitVal = process.waitFor();
            if (exitVal != 0) {
                LOGGER.log(Level.INFO, () -> "***Error: Process Exit Value: " + exitVal);
                updateUI(ApplicationConstant.GENERIC_ERR_MSG);
            }
        } catch (Exception ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            LOGGER.log(Level.INFO, () -> "**Error while rebooting: " + ex.getMessage());
            updateUI(ApplicationConstant.GENERIC_ERR_MSG);
        }
        enableControls(backBtn, homeBtn, saveBtn);
    }

    private void updateUI(String message) {
        Platform.runLater(() -> messageLabel.setText(message));
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
            hostnameTextField.setText(getHostname());
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
            if (line == null || line.isBlank()) {
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
        String[] command = createCommand();
        Process process = Runtime.getRuntime().exec(command);
        BufferedReader error = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        String eline;
        while ((eline = error.readLine()) != null) {
            String finalEline = eline;
            LOGGER.log(Level.INFO, () -> "***Error: " + finalEline);
        }
        error.close();
        int exitVal = process.waitFor();
        if (exitVal != 0) {
            LOGGER.log(Level.INFO, () -> "***Error: Process Exit Value: " + exitVal);
            throw new GenericException(ApplicationConstant.GENERIC_ERR_MSG);
        }
    }

    private String[] createCommand() {
        String hostnameCommand = "hostnamectl set-hostname " + hostnameTextField.getText();
        String hostnameSerialFile = ";echo " + hostnameTextField.getText() + " > /usr/share/enrollment/serial/serial.txt";
        String hostFileCommand = ";sed -i '2 s/^.*$/127.0.0.1 " + hostnameTextField.getText() + "/g' /etc/hosts";
        String twUpdateCommand = ";/usr/share/enrollment/startup/tw-update";
        return new String[]{"/bin/bash", "-c", hostnameCommand + hostnameSerialFile + hostFileCommand + twUpdateCommand};
    }

    private void restartNetworkingService() throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec("systemctl restart networking");
        BufferedReader error = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        String eline;
        while ((eline = error.readLine()) != null) {
            String finalEline = eline;
            LOGGER.log(Level.INFO, () -> "***Error: " + finalEline);
        }
        error.close();
        int exitVal = process.waitFor();
        if (exitVal != 0) {
            LOGGER.log(Level.INFO, () -> "***Error: Process Exit Value: " + exitVal);
            throw new GenericException("Failed to restart networking service.");
        }
    }


    @Override
    public void onUncaughtException() {
        LOGGER.log(Level.INFO, "***Unhandled exception occurred.");
    }
}
