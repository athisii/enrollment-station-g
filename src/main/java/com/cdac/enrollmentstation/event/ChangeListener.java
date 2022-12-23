/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cdac.enrollmentstation.event;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;

/**
 *
 * @author root
 */

public class ChangeListener implements javafx.beans.value.ChangeListener<String> {


    private int maxLength;
    private TextField textField;


    public ChangeListener(TextField textField, int maxLength) {
        this.textField= textField;
        this.maxLength = maxLength;
    }


    public int getMaxLength() {
        return maxLength;
    }


    @Override
    public void changed(ObservableValue<? extends String> ov, String oldValue,
            String newValue) {


        if (newValue == null) {
            return;
        }

        if (newValue.length() > maxLength) {
            textField.setText(oldValue);
        } else {
            textField.setText(newValue);
        }
    }


}// End of Class

