import React, { useState } from 'react';
import {
  View,
  TextInput,
  Text,
  ScrollView,
  TouchableOpacity
} from 'react-native';

var styles = require('../styles');

const DynamicForm = ({ config }) => {
  const { texts, placeholders, onSubmit } = config;
  const [name, setName] = useState('');
  const [keyValues, setKeyValues] = useState([]);

  const addKeyValue = () => {
    setKeyValues([...keyValues, { key: '', value: '' }]);
  };

  const removeKeyValue = (index) => {
    const newKeyValues = [...keyValues];
    newKeyValues.splice(index, 1);
    setKeyValues(newKeyValues);
  };

  const handleKeyChange = (index, newKey) => {
    const newKeyValues = [...keyValues];
    newKeyValues[index].key = newKey;
    setKeyValues(newKeyValues);
  };

  const handleValueChange = (index, newValue) => {
    const newKeyValues = [...keyValues];
    newKeyValues[index].value = newValue;
    setKeyValues(newKeyValues);
  };

  const handleSubmit = () => {
    onSubmit({ name, keyValues });
  };

  return (
    <ScrollView contentContainerStyle={styles.container}>
      <View style={styles.row}>
        <TextInput
          placeholder={placeholders.namePlaceholder || 'Enter name'}
          placeholderTextColor='grey'
          autoCapitalize='none'
          value={name}
          onChangeText={setName}
          style={styles.input}
        />
        <View>
          <TouchableOpacity style={styles.confirmButton} onPress={addKeyValue}>
            <Text style={styles.buttonText}>{texts.add || 'Add Key-Value'}</Text>
          </TouchableOpacity>
        </View>

      </View>

      {/* Dynamic key-value inputs */}
      {keyValues.map((item, index) => (
        <View key={index} style={styles.row}>
          <TextInput
            placeholder={placeholders.keyPlaceholder || 'Key'}
            placeholderTextColor='grey'
          autoCapitalize='none'
            value={item.key}
            onChangeText={(text) => handleKeyChange(index, text)}
            style={styles.smallInput}
          />
          <TextInput
            placeholder={placeholders.valuePlaceholder || 'Value'}
            placeholderTextColor='grey'
          autoCapitalize='none'
            value={item.value}
            onChangeText={(text) => handleValueChange(index, text)}
            style={styles.smallInput}
          />
          <View>
            <TouchableOpacity style={styles.cancelButton} onPress={() => removeKeyValue(index)}>
              <Text style={styles.buttonText}>Remove</Text>
            </TouchableOpacity>
          </View>
        </View>
      ))}
      <View style={styles.buttonContainer}>
        <TouchableOpacity style={styles.submitButton} onPress={handleSubmit}>
          <Text style={styles.buttonText}>{texts.submit || 'Submit'}</Text>
        </TouchableOpacity>
      </View>

    </ScrollView>
  );
};

styles = {
  ...styles,
  container: {
    padding: 5,
    paddingBottom: 0,
  },
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 10,
  },
  input: {
    flex: 1,
    borderWidth: 1,
    borderColor: '#ccc',
    padding: 10,
    marginRight: 10,
    borderColor: 'gray',
    borderWidth: 1,
    borderRadius: 5,
  },
  smallInput: {
    flex: 1,
    borderWidth: 1,
    borderColor: '#ccc',
    padding: 10,
    marginRight: 5,
    borderColor: 'gray',
    borderWidth: 1,
    borderRadius: 5,
  },
};

export default DynamicForm;
