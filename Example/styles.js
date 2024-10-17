'use strict';

import { StyleSheet } from 'react-native';

module.exports = StyleSheet.create({
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