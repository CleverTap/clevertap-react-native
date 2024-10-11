
import React, { Component } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  Image,
  StyleSheet
} from 'react-native';

export class ExpandableListView extends Component {
    constructor() {
      super();
      this.state = {
        layoutHeight: 0,
      };
    }

    onCategoryPress = () => {
        let height = this.props.item.expanded ? 0 : null;
        this.setState({ layoutHeight: height});
        this.props.onToggleView();
    };
  
    render() {
      return (
        <View>
          <TouchableOpacity
            activeOpacity={0.8}
            onPress={this.onCategoryPress}
            style={styles.categoryView}>
            <Text style={styles.categoryText}>
              {this.props.item.category_Name}
            </Text>
            <Image
              source={{
                uri: 'https://reactnativecode.com/wp-content/uploads/2019/02/arrow_right_icon.png',
              }}
              style={styles.iconStyle}
            />
  
          </TouchableOpacity>
          <View style={{ height: this.state.layoutHeight, overflow: 'hidden' }}>
            {this.props.item.sub_Category.map((item, key) => (
              <TouchableOpacity
                key={key}
                style={styles.subCategory}
                onPress={() => this.props.onItemPress(item)}>
                <Text style={styles.subCategoryText}>{item.name}</Text>
                <View style={{ width: '100%', height: 1, backgroundColor: '#000' }} />
              </TouchableOpacity>
            ))}
          </View>
        </View>
      );
    }
  };

  const styles = StyleSheet.create({
    iconStyle: {
      width: 22,
      height: 22,
      justifyContent: 'flex-end',
      alignItems: 'center',
      tintColor: '#fff',
    },
    subCategory: {
      fontSize: 20,
      color: '#000',
      padding: 10,
    },
    categoryText: {
      textAlign: 'left',
      color: '#fff',
      fontSize: 22,
      padding: 12,
    },
    categoryView: {
      marginVertical: 5,
      flexDirection: 'row',
      justifyContent: 'space-between',
      alignItems: 'center',
      backgroundColor: '#DC2626',
    },
    subCategoryText: {
      fontSize: 18,
    },
  });