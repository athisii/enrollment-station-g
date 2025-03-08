package com.cdac.enrollmentstation.controller;

import com.cdac.enrollmentstation.App;
import com.cdac.enrollmentstation.api.DirectoryLookup;
import com.cdac.enrollmentstation.api.MafisServerApi;
import com.cdac.enrollmentstation.constant.ApplicationConstant;
import com.cdac.enrollmentstation.constant.PropertyName;
import com.cdac.enrollmentstation.exception.AuthException;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.cdac.enrollmentstation.constant.ApplicationConstant.SCENE_ROOT_ERR_MSG;

public class OnboardNetworkConfigController extends AbstractBaseController {
    private static final Logger LOGGER = ApplicationLog.getLogger(OnboardNetworkConfigController.class);
    private static final String IP_REGEX = "^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$"; // ipv4 address
    private static final int NIC_RESTART_TIME_IN_SECOND = 5;
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
    private Button backBtn;
    @FXML
    private Button continueBtn;
    @FXML
    private TextField ldapUrlTextField;
    @FXML
    private TextField mafisUrlTextField;
    private String interfaceName;

    public void initialize() {
        // disable 'enter key' on keyboard
        rootBorderPane.addEventFilter(KeyEvent.ANY, keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                keyEvent.consume();
            }
        });

        continueBtn.setOnAction(event -> continueBtnAction());
        backBtn.setOnAction(event -> backBtnAction());

        interfaceName = getInterfaceName();
        setTextFieldValuesOnUI(); // only set after getting all required fields


        hostnameTextField.setOnKeyPressed(event -> clearMessageLabelIfNotBlank());
        ipAddressTextField.setOnKeyPressed(event -> clearMessageLabelIfNotBlank());
        subnetMaskTextField.setOnKeyPressed(event -> clearMessageLabelIfNotBlank());
        defaultGatewayTextField.setOnKeyPressed(event -> clearMessageLabelIfNotBlank());
        dnsIpTextField.setOnKeyPressed(event -> clearMessageLabelIfNotBlank());
        ldapUrlTextField.setOnKeyPressed(event -> clearMessageLabelIfNotBlank());
        mafisUrlTextField.setOnKeyPressed(event -> clearMessageLabelIfNotBlank());
    }

    private void backBtnAction() {
        try {
            App.setRoot("main_screen");
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, SCENE_ROOT_ERR_MSG, ex);
        }
    }

    private void clearMessageLabelIfNotBlank() {
        if (!messageLabel.getText().isBlank()) {
            messageLabel.setText("");
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
        BufferedReader input = null;
        try {
            Process process = Runtime.getRuntime().exec("ip -o link show");
            input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            LinkedList<String> interfaces = new LinkedList<>();
            String line;
            while ((line = input.readLine()) != null) {
                interfaces.add(line);
            }
            if (interfaces.size() < 2) {
                LOGGER.log(Level.INFO, () -> "Error: Number of interfaces is less than 2 (including loopback)");
                throw new GenericException("Failed to get interface name.");
            }
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
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, e::getMessage);
            }
        }
    }

    private void continueBtnAction() {
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

        if (ldapUrlTextField.getText().isBlank()) {
            messageLabel.setText("LDAP URL cannot be blank.");
            return;
        }

        String[] ldapUrlParts = ldapUrlTextField.getText().split(":");
        if (ldapUrlParts.length < 2) {
            messageLabel.setText("Invalid LDAP URL.");
            return;
        }

        if (!"ldap".equalsIgnoreCase(ldapUrlParts[0]) && !"ldaps".equalsIgnoreCase(ldapUrlParts[0])) {
            messageLabel.setText("Invalid LDAP URL scheme. Use either 'ldap' or 'ldaps'.");
            return;
        }
        ldapUrlTextField.setText(ldapUrlTextField.getText().trim().toLowerCase()); // in case if user type in uppercase

        if (mafisUrlTextField.getText().isBlank()) {
            messageLabel.setText("MAFIS URL cannot be blank.");
            return;
        }

        String[] mafisUrlParts = mafisUrlTextField.getText().split(":");
        if (mafisUrlParts.length < 2) {
            messageLabel.setText("Invalid MAFIS URL.");
            return;
        }
        if (!"http".equalsIgnoreCase(mafisUrlParts[0]) && !"https".equalsIgnoreCase(mafisUrlParts[0])) {
            messageLabel.setText("Invalid MAFIS URL scheme. Use either 'http' or 'https'.");
            return;
        }
        mafisUrlTextField.setText(mafisUrlTextField.getText().trim().toLowerCase()); // in case if user type in uppercase

        // should ask for the confirmation before saving
        disableControls(backBtn, continueBtn, hostnameTextField, ipAddressTextField, subnetMaskTextField, defaultGatewayTextField, dnsIpTextField, ldapUrlTextField, mafisUrlTextField);
        messageLabel.setText("Updating the system configuration. Please wait.");
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
            restartNetworkingService();
            Thread.sleep(Duration.ofSeconds(NIC_RESTART_TIME_IN_SECOND).toMillis()); // sleeps for nic to get the new ip
            PropertyFile.changePropertyValue(PropertyName.LDAP_URL, ldapUrlTextField.getText().trim());
            PropertyFile.changePropertyValue(PropertyName.MAFIS_API_URL, mafisUrlTextField.getText().trim());
//            // only do for production as there is no ldap connection in MISCOS
            if ("0".equals(PropertyFile.getProperty(PropertyName.ENV).trim())) {
                // test connection with the ldap server: only proceed if connection is established
                try {
                    DirectoryLookup.doLookup("1234567", "test@password");
                } catch (AuthException ex) {
                    LOGGER.log(Level.INFO, () -> "Ignoring auth failure for the test connection.");
                }
            }
            // test connection with the mafis server: only proceed if connection is established
            try {
                MafisServerApi.fetchARCDetail("1234567");
            } catch (Exception ex) {
                if (ex.getMessage().toLowerCase().contains("invalid url or ip") || ex.getMessage().toLowerCase().contains("connection timeout")) {
                    throw new GenericException("Invalid MAFIS URL or connection timeout.");
                }
            }
            if (!getHostname().equals(hostnameTextField.getText())) {
                setHostname();
                App.setHostnameChanged(true);
            }
            Platform.runLater(() -> {
                try {
                    App.setRoot("onboard_auth");
                } catch (IOException ex) {
                    LOGGER.log(Level.INFO, () -> "***Error: " + ex.getMessage());
                    throw new GenericException(ex.getMessage());
                }
            });
        } catch (Exception ex) {
            LOGGER.log(Level.INFO, () -> "***Error: " + ex.getMessage());
            updateUI(ex.getMessage());
            enableControls(backBtn, continueBtn, hostnameTextField, ipAddressTextField, subnetMaskTextField, defaultGatewayTextField, dnsIpTextField, ldapUrlTextField, mafisUrlTextField);
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
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
            ldapUrlTextField.setText(PropertyFile.getProperty(PropertyName.LDAP_URL));
            mafisUrlTextField.setText(PropertyFile.getProperty(PropertyName.MAFIS_API_URL));
        } catch (Exception ex) {
            LOGGER.log(Level.INFO, () -> "***Error: " + ex.getMessage());
            messageLabel.setText(ex.getMessage());
        }
    }

    private String getHostname() {
        BufferedReader input = null;
        try {
            Process process = Runtime.getRuntime().exec("hostname");
            input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = input.readLine();
            if (line == null || line.isBlank()) {
                LOGGER.log(Level.INFO, () -> "***Error: Received null value hostname");
                throw new GenericException("Failed to get hostname.");
            }
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
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, e::getMessage);
            }
        }
    }

    private void setHostname() {
        String[] command = createCommand();
        BufferedReader error = null;
        try {
            Process process = Runtime.getRuntime().exec(command);
            error = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String eline;
            while ((eline = error.readLine()) != null) {
                String finalEline = eline;
                LOGGER.log(Level.INFO, () -> "***Error: " + finalEline);
            }
            int exitVal = process.waitFor();
            if (exitVal != 0) {
                LOGGER.log(Level.INFO, () -> "***Error: Process Exit Value: " + exitVal);
                throw new GenericException("Re-throwing error.");
            }
        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            LOGGER.log(Level.INFO, () -> "***Error: " + e.getMessage());
            throw new GenericException(ApplicationConstant.GENERIC_ERR_MSG); // to be caught by caller.
        } finally {
            try {
                if (error != null) {
                    error.close();
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, e::getMessage);
            }
        }
    }

    private String[] createCommand() {
        String hostnameCommand = "hostnamectl set-hostname " + hostnameTextField.getText();
        String hostnameSerialFile = ";echo " + hostnameTextField.getText() + " > /usr/share/enrollment/serial/serial.txt";
        String hostFileCommand = ";sed -i '2 s/^.*$/127.0.0.1 " + hostnameTextField.getText() + "/g' /etc/hosts";
        String twUpdateCommand = ";/usr/share/enrollment/startup/tw-update";
        return new String[]{"/bin/bash", "-c", hostnameCommand + hostnameSerialFile + hostFileCommand + twUpdateCommand};
    }

    private void restartNetworkingService() {
        BufferedReader error = null;
        try {
            Process process = Runtime.getRuntime().exec("systemctl restart networking");
            error = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String eline;
            while ((eline = error.readLine()) != null) {
                String finalEline = eline;
                LOGGER.log(Level.INFO, () -> "***Error: " + finalEline);
            }
            int exitVal = process.waitFor();
            if (exitVal != 0) {
                LOGGER.log(Level.INFO, () -> "***Error: Process Exit Value: " + exitVal);
                throw new GenericException("Re-throwing error.");
            }
        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            LOGGER.log(Level.INFO, () -> "***Error: " + e.getMessage());
            throw new GenericException(ApplicationConstant.GENERIC_ERR_MSG); // to be caught by caller.
        } finally {
            try {
                if (error != null) {
                    error.close();
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, e::getMessage);
            }
        }
    }

    @Override
    public void onUncaughtException() {
        LOGGER.log(Level.INFO, "***Unhandled exception occurred.");
    }
}
