import React, { useState } from 'react';
import { Modal, View, Text, TextInput, TouchableOpacity, StyleSheet } from 'react-native';

const styles = require('../styles');

const InAppMessagePopup = ({ visible, title, description, isFunction,
   onCancel, onConfirm, onTriggerAction, onFileOpen }) => {
  const [actionName, setActionName] = useState('');
  const [fileName, setFileName] = useState('');

  return (
    <Modal
      visible={visible}
      transparent={true}
      animationType="fade"
    >
      <View style={styles.modalBackground}>
        <View style={styles.popupContainer}>
          <Text style={styles.title}>{title}</Text>
          <Text style={styles.description}>{description}</Text>

          <TextInput
            style={styles.input}
            placeholder="File Arg name"
            placeholderTextColor="grey"
            autoCapitalize="none"
            value={fileName}
            onChangeText={setFileName}
          />
          <TouchableOpacity style={styles.submitButton} onPress={() => onFileOpen(fileName)}>
            <Text style={styles.buttonText}>Open File</Text>
          </TouchableOpacity>

          {!isFunction &&
            <View style={styles.inputContainer}>
              <TextInput
                style={styles.input}
                placeholder="Action Arg name"
                placeholderTextColor="grey"
                autoCapitalize="none"
                value={actionName}
                onChangeText={setActionName}
              />
              <TouchableOpacity style={styles.submitButton} onPress={() => onTriggerAction(actionName)}>
                <Text style={styles.buttonText}>Trigger Action</Text>
              </TouchableOpacity>
            </View>
          }

          <View style={styles.buttonContainer}>
            <TouchableOpacity style={styles.cancelButton} onPress={onCancel}>
              <Text style={styles.buttonText}>Dismiss</Text>
            </TouchableOpacity>
            <TouchableOpacity style={styles.confirmButton} onPress={onConfirm}>
              <Text style={styles.buttonText}>Set Presented</Text>
            </TouchableOpacity>
          </View>
        </View>
      </View>
    </Modal>
  );
};

export default InAppMessagePopup;
