import React, { useState } from 'react';
import { Modal, View, Text, TextInput, TouchableOpacity, StyleSheet } from 'react-native';

const styles = require('../styles');

const FunctionPopup = ({ visible, title, description,
   onClose, onFileOpen }) => {
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

          <View style={styles.buttonContainer}>
            <TouchableOpacity style={styles.cancelButton} onPress={onClose}>
              <Text style={styles.buttonText}>Close</Text>
            </TouchableOpacity>
          </View>
        </View>
      </View>
    </Modal>
  );
};

styles.cancelButton = {
    ...styles.cancelButton,
    marginRight: 'initial',
}

export default FunctionPopup;
