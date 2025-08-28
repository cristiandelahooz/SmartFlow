package com.trafficmanagement.smartflow.ui;

import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.util.StringConverter;
import lombok.Getter;

/**
 * @author cristiandelahoz
 */
public class ComboBoxWrapper<T> {
  @Getter private final ComboBox<T> comboBox;

  public ComboBoxWrapper(ComboBox<T> comboBox) {
    this(comboBox, defaultDisplayNameConverter());
  }

  public ComboBoxWrapper(ComboBox<T> comboBox, StringConverter<T> stringConverter) {
    this.comboBox = comboBox;
    this.comboBox.setConverter(stringConverter);
  }

  private static <T> StringConverter<T> defaultDisplayNameConverter() {
    return new StringConverter<>() {
      @Override
      public String toString(T object) {
        if (object == null) return "";
        try {
          return (String) object.getClass().getMethod("getDisplayName").invoke(object);
        } catch (Exception e) {
          return object.toString();
        }
      }

      @Override
      public T fromString(String string) {
        return null;
      }
    };
  }

  public T getValue() {
    return comboBox.getValue();
  }

  public void setValue(T value) {
    comboBox.setValue(value);
  }

  public ObservableList<T> getItems() {
    return comboBox.getItems();
  }

  public void setItems(javafx.collections.ObservableList<T> items) {
    comboBox.setItems(items);
  }
}
