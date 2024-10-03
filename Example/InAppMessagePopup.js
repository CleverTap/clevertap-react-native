import React, { useState } from 'react';
import { Modal, View, Text, TextInput, TouchableOpacity, StyleSheet } from 'react-native';

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
          <Text style={styles.description}>{description}.</Text>

          <TextInput
            style={styles.input}
            placeholder="FileArg name"
            placeholderTextColor="grey"
            autoCapitalize='none'
            value={fileName}
            onChangeText={setFileName}
          />
          <TouchableOpacity style={styles.submitButton} onPress={() => onFileOpen(fileName)}>
            <Text style={styles.submitButtonText}>Open File</Text>
          </TouchableOpacity>

          {!isFunction &&
            <View style={styles.inputContainer}>
              <TextInput
                style={styles.input}
                placeholder="Action name"
                placeholderTextColor="grey"
                autoCapitalize='none'
                value={actionName}
                onChangeText={setActionName}
              />
              <TouchableOpacity style={styles.submitButton} onPress={() => onTriggerAction(actionName)}>
                <Text style={styles.submitButtonText}>Trigger Action</Text>
              </TouchableOpacity>
            </View>
          }

          <View style={styles.buttonContainer}>
            <TouchableOpacity style={styles.cancelButton} onPress={onCancel}>
              <Text style={styles.buttonText}>Cancel</Text>
            </TouchableOpacity>
            <TouchableOpacity style={styles.confirmButton} onPress={onConfirm}>
              <Text style={styles.buttonText}>Confirm</Text>
            </TouchableOpacity>
          </View>
        </View>
      </View>
    </Modal>
  );
};

const styles = StyleSheet.create({
  modalBackground: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: 'rgba(0, 0, 0, 0.5)',
  },
  popupContainer: {
    width: '80%',
    padding: 20,
    backgroundColor: 'white',
    borderRadius: 10,
    alignItems: 'center',
  },
  title: {
    fontSize: 18,
    fontWeight: 'bold',
    marginBottom: 10,
  },
  description: {
    fontSize: 14,
    color: 'gray',
    marginBottom: 20,
    textAlign: 'center',
  },
  inputContainer: {
    width: '100%'
  },
  input: {
    width: '100%',
    height: 40,
    borderColor: 'gray',
    borderWidth: 1,
    borderRadius: 5,
    paddingHorizontal: 10,
    marginBottom: 20
  },
  submitButton: {
    width: '100%',
    padding: 10,
    backgroundColor: '#4CAF50',
    borderRadius: 5,
    marginBottom: 20,
  },
  submitButtonText: {
    color: 'white',
    textAlign: 'center',
  },
  buttonContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    width: '100%',
  },
  cancelButton: {
    flex: 1,
    padding: 10,
    backgroundColor: '#f44336',
    borderRadius: 5,
    marginRight: 10,
  },
  confirmButton: {
    flex: 1,
    padding: 10,
    backgroundColor: '#4CAF50',
    borderRadius: 5,
    marginLeft: 10,
  },
  buttonText: {
    color: 'white',
    textAlign: 'center',
  },
});

export default InAppMessagePopup;
