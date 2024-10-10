package com.cdac.enrollmentstation.controller;

import com.cdac.enrollmentstation.App;
import com.cdac.enrollmentstation.api.DirectoryLookup;
import com.cdac.enrollmentstation.constant.ApplicationConstant;
import com.cdac.enrollmentstation.constant.PropertyName;
import com.cdac.enrollmentstation.exception.GenericException;
import com.cdac.enrollmentstation.logging.ApplicationLog;
import com.cdac.enrollmentstation.util.PropertyFile;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

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

import static com.cdac.enrollmentstation.constant.ApplicationConstant.SCENE_ROOT_ERR_MSG;

public class HostnameIpController extends AbstractBaseController {
    private static final Logger LOGGER = ApplicationLog.getLogger(HostnameIpController.class);
    private static final String IP_REGEX = "^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$"; // ipv4 address
    @FXML
    private BorderPane rootBorderPane;
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
    private TextField ldapUrl;
    @FXML
    private VBox confirmVbox;
    @FXML
    private Button confirmYesBtn;
    @FXML
    private Button confirmNoBtn;
    @FXML
    private Button defaultBtn;

    private String interfaceName;

    public void initialize() {
        // disable 'enter key' on keyboard
        rootBorderPane.addEventFilter(KeyEvent.ANY, keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                keyEvent.consume();
            }
        });

        backBtn.setOnAction(event -> backBtnAction());
        saveBtn.setOnAction(event -> saveBtnAction());
        defaultBtn.setOnAction(event -> defaultBtnAction());

        confirmNoBtn.setOnAction(event -> confirmNoBtnAction());
        confirmYesBtn.setOnAction(event -> confirmYesBtnAction());

        interfaceName = getInterfaceName();
        setTextFieldValuesOnUI(); // only set after getting all required fields

        hostnameTextField.setOnKeyPressed(event -> clearMessageLabelIfNotBlank());
        ipAddressTextField.setOnKeyPressed(event -> clearMessageLabelIfNotBlank());
        subnetMaskTextField.setOnKeyPressed(event -> clearMessageLabelIfNotBlank());
        defaultGatewayTextField.setOnKeyPressed(event -> clearMessageLabelIfNotBlank());
        dnsIpTextField.setOnKeyPressed(event -> clearMessageLabelIfNotBlank());
        ldapUrl.setOnKeyPressed(event -> clearMessageLabelIfNotBlank());
    }

    private void defaultBtnAction() {
        ipAddressTextField.setText("192.168.1.2");
        subnetMaskTextField.setText("255.255.255.0");
        defaultGatewayTextField.setText("192.168.1.1");
        dnsIpTextField.setText("192.168.1.3");
        ldapUrl.setText("ldap://192.168.1.4:389");
        PropertyFile.changePropertyValue(PropertyName.LDAP_URL, ldapUrl.getText());
        PropertyFile.changePropertyValue(PropertyName.INITIAL_SETUP, "1");
        try {
            saveIpaddressToFile();
            restartNetworkingService();
        } catch (Exception ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            updateUI(ex.getMessage());
            return;
        }
        messageLabel.setText("System configuration reset successfully.");
    }

    private void confirmYesBtnAction() {
        messageLabel.setText("Updating the system configuration. Please wait.");
        confirmVbox.setVisible(false);
        confirmVbox.setManaged(false);
        App.getThreadPool().execute(this::saveChanges);
    }

    private void confirmNoBtnAction() {
        confirmVbox.setVisible(false);
        confirmVbox.setManaged(false);
        enableControls(backBtn, saveBtn, defaultBtn, hostnameTextField, ipAddressTextField, subnetMaskTextField, defaultGatewayTextField, dnsIpTextField, ldapUrl);
    }

    private void clearMessageLabelIfNotBlank() {
        if (!messageLabel.getText().isBlank()) {
            messageLabel.setText("");
        }
    }

    private void backBtnAction() {
        try {
            if ("1".equals(PropertyFile.getProperty(PropertyName.INITIAL_SETUP).trim())) {
                App.setRoot("admin_auth");
            } else {
                App.setRoot("admin_config");
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, SCENE_ROOT_ERR_MSG, ex);
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
            String[] dnsIps = dnsIpTextField.getText().split(",");
            content += "\ndns-nameservers\t";
            for (String dnsIp : dnsIps) {
                content += dnsIp.trim() + " ";
            }
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
        String hostnameRegex = "^[a-zA-Z0-9](?:[a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?$"; // linux hostname
        if (!hostnameTextField.getText().matches(hostnameRegex)) {
            messageLabel.setText("Enter a valid hostname(alphanumeric chars).");
            return;
        }

        if (!ipAddressTextField.getText().matches(IP_REGEX)) {
            messageLabel.setText("Enter a valid ip address.");
            return;
        }
        if (!subnetMaskTextField.getText().matches(IP_REGEX)) {
            messageLabel.setText("Enter a valid subnet mask.");
            return;
        }
        if (defaultGatewayTextField.getText().isBlank() || (!defaultGatewayTextField.getText().matches(IP_REGEX))) {
            messageLabel.setText("Enter a valid default gateway.");
            return;
        }

        if (dnsIpTextField.getText().isBlank()) {
            messageLabel.setText("DNS IP cannot be blank.");
            return;
        }

        String[] dnsIps = dnsIpTextField.getText().split(",");
        StringBuilder tempDnsIp = new StringBuilder();
        for (String dnsIp : dnsIps) {
            if (!dnsIp.trim().matches(IP_REGEX)) {
                messageLabel.setText("Enter a valid comma separated dns ip address(s).");
                return;
            }
            // remove unnecessary commas 192.168.1.1,,,
            tempDnsIp.append(dnsIp).append(",");
        }
        // if the last char is comma(,)
        tempDnsIp.deleteCharAt(tempDnsIp.length() - 1);
        dnsIpTextField.setText(tempDnsIp.toString());

        if (ldapUrl.getText().isBlank()) {
            messageLabel.setText("LDAP URL cannot be blank.");
            return;
        }

        String[] ldapUrlParts = ldapUrl.getText().split(":");
        if (ldapUrlParts.length < 2) {
            messageLabel.setText("Invalid LDAP URL.");
            return;
        }

        if (!"ldap".equalsIgnoreCase(ldapUrlParts[0]) && !"ldaps".equalsIgnoreCase(ldapUrlParts[0])) {
            messageLabel.setText("Invalid LDAP URL scheme. Use either 'ldap' or 'ldaps'.");
            return;
        }
        ldapUrl.setText(ldapUrl.getText().trim().toLowerCase()); // in case if user type in uppercase

        // should ask for the confirmation before saving
        confirmVbox.setVisible(true);
        confirmVbox.setManaged(true);
        disableControls(backBtn, saveBtn, defaultBtn, hostnameTextField, ipAddressTextField, subnetMaskTextField, defaultGatewayTextField, dnsIpTextField, ldapUrl);
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
            restartNetworkingService();
//            // only do for production as there is no ldap connection in MISCOS
            if ("0".equals(PropertyFile.getProperty(PropertyName.ENV).trim())) {
                // test connection with the ldap server: only proceed if connection is established
                DirectoryLookup.doLookup("test", "password");
            }
            PropertyFile.changePropertyValue(PropertyName.LDAP_URL, ldapUrl.getText().trim());
            PropertyFile.changePropertyValue(PropertyName.INITIAL_SETUP, "0");
            if (!getHostname().equals(hostnameTextField.getText())) {
                setHostname();
                // reboot system
                App.getThreadPool().execute(this::rebootSystem);
                return;
            }
        } catch (Exception ex) {
            if (!ApplicationConstant.INVALID_CREDENTIALS.equals(ex.getMessage())) {
                enableControls(backBtn, saveBtn, defaultBtn, hostnameTextField, ipAddressTextField, subnetMaskTextField, defaultGatewayTextField, dnsIpTextField, ldapUrl);
                LOGGER.log(Level.INFO, () -> "***Error: " + ex.getMessage());
                if (ex instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                updateUI(ex.getMessage());
                return;
            }
        }
        enableControls(backBtn);
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
        enableControls(backBtn);
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
                    StringBuilder dnsIps = new StringBuilder();
                    for (int i = 1; i < entry.length - 1; i++) {
                        dnsIps.append(entry[i]).append(",");
                    }
                    dnsIps.append(entry[entry.length - 1]);
                    dnsIpTextField.setText(dnsIps.toString());
                }
            });
            hostnameTextField.setText(getHostname());
            ldapUrl.setText(PropertyFile.getProperty(PropertyName.LDAP_URL));
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
