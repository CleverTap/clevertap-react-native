import React, { useState, useEffect } from 'react';
import { View, Modal, Text, TouchableOpacity, StyleSheet } from 'react-native';
import InAppMessagePopup from './InAppMessagePopup';
import { WebView } from 'react-native-webview';

const CleverTap = require('clevertap-react-native');

const CustomTemplate = () => {
    const [isModalVisible, setIsModalVisible] = useState(false);
    const [templateName, setTemplateName] = useState('');
    const [description, setDescription] = useState('');
    const [isFunction, setIsFunction] = useState(false);
    const [filePath, setFilePath] = useState('');
    const [showWebView, setShowWebView] = useState(false);
    const [templateNameVisual, setTemplateNameVisual] = useState('');

    useEffect(() => {
        CleverTap.addListener(CleverTap.CleverTapCustomTemplatePresent, templateName => {
            setIsFunction(false);
            presentInAppModal(templateName);
        });

        CleverTap.addListener(CleverTap.CleverTapCustomTemplateClose, templateName => {
            CleverTap.customTemplateSetDismissed(templateName);
            setIsModalVisible(true);
        });

        CleverTap.addListener(CleverTap.CleverTapCustomFunctionPresent, templateName => {
            setIsFunction(true);
            presentInAppModal(templateName);
        });

        return () => {
            CleverTap.removeListener(CleverTap.CleverTapCustomTemplatePresent);
            CleverTap.removeListener(CleverTap.CleverTapCustomTemplateClose);
            CleverTap.removeListener(CleverTap.CleverTapCustomFunctionPresent);
        };
    }, []);

    const presentInAppModal = (name) => {
        if (isModalVisible) {
            // Showing a custom function with isVisual:false
            setTemplateNameVisual(templateName);
        }
        getTemplateValuesString(name).then((str) => {
            setDescription(str);
            setTemplateName(name);
            setIsModalVisible(true);
        });
    };

    const getTemplateValuesString = async (templateName) => {
        switch (templateName) {
            case "Example template":
                try {
                    var bool = await CleverTap.customTemplateGetBooleanArg(templateName, "bool");
                    var string = await CleverTap.customTemplateGetStringArg(templateName, "string");
                    var mapInt = await CleverTap.customTemplateGetNumberArg(templateName, "map.int");
                    var mapString = await CleverTap.customTemplateGetStringArg(templateName, "map.string");
                    var map = await CleverTap.customTemplateGetObjectArg(templateName, "map");
                    var file = await CleverTap.customTemplateGetFileArg(templateName, "file");
                    return `Arguments for ${templateName}:
bool=${bool}
string=${string}
map.int=${mapInt}
map.string=${mapString}
map=${JSON.stringify(map)}
file=${file}`;
                } catch (e) {
                    return e.toString();
                }

            case "Example function":
                try {
                    var double = await CleverTap.customTemplateGetNumberArg(templateName, "double");
                    var string = await CleverTap.customTemplateGetStringArg(templateName, "string");
                    var file = await CleverTap.customTemplateGetFileArg(templateName, "file");
                    return `Arguments for ${templateName}:
double=${double}
string=${string}
file=${file}`;
                } catch (e) {
                    return e.toString();
                }

            case "Example visual function":
                try {
                    var color = await CleverTap.customTemplateGetStringArg(templateName, "color");
                    var enabled = await CleverTap.customTemplateGetBooleanArg(templateName, "enabled");
                    var image = await CleverTap.customTemplateGetFileArg(templateName, "image");
                    return `Arguments for ${templateName}:
color=${double}
string=${string}
image=${image}`;
                } catch (e) {
                    return e.toString();
                }

            default:
                return "Template not found"
        }
    };

    const handleCancel = () => {
        CleverTap.customTemplateSetDismissed(templateName);
        setIsModalVisible(false);

        // If a function with isVisual:false was presented,
        // show the template modal, so it can be dismissed
        if (templateNameVisual && templateNameVisual != '') {
            presentInAppModal(templateNameVisual);
            setTemplateNameVisual('');
        }
    };

    const handleConfirm = () => {
        CleverTap.customTemplateSetPresented(templateName);
        setIsModalVisible(false);
    };

    const handleTriggerAction = (actionName) => {
        console.log('trigger action:', actionName);
        CleverTap.customTemplateRunAction(templateName, actionName);
    };

    const handleOpenFile = (fileName) => {
        console.log('open file name:', fileName);
        CleverTap.customTemplateGetFileArg(templateName, fileName).then((filePath) => {
            console.log('open file path:', filePath);
            setFilePath(filePath);

            setIsModalVisible(false);
            setShowWebView(true);
        });
    };

    const closeWebView = () => {
        setShowWebView(false);
        setFilePath('');
        setIsModalVisible(true);
    };

    return (
        <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center' }}>
            {/* <Button title="Show Modal" onPress={() => setIsModalVisible(true)} /> */}

            <InAppMessagePopup
                visible={isModalVisible}
                title={templateName}
                description={description}
                isFunction={isFunction}
                onCancel={handleCancel}
                onConfirm={handleConfirm}
                onTriggerAction={handleTriggerAction}
                onFileOpen={handleOpenFile}
            >
            </InAppMessagePopup>

            {/* {showWebView && (
                <View style={{ height: 400, width: '90%', marginTop: 20 }}>
                    <WebView
                        source={{ uri: filePath }}
                        style={{ flex: 1 }}
                        originWhitelist={['*']}
                        allowFileAccessFromFileURLs={true}
                    />
                </View>
            )} */}

            <Modal
                visible={showWebView}
                transparent={true}
                animationType="slide"
                onRequestClose={closeWebView}
            >
                <View style={styles.webViewContainer}>
                    <View style={styles.webViewHeader}>
                        <TouchableOpacity onPress={closeWebView} style={styles.closeButton}>
                            <Text style={styles.closeButtonText}>X</Text>
                        </TouchableOpacity>
                    </View>

                    <View style={styles.webViewWrapper}>
                        <WebView
                            source={{ uri: filePath }}
                            style={{ flex: 1, backgroundColor: 'transparent' }}
                            originWhitelist={['*']}
                            allowFileAccessFromFileURLs={true}
                        />
                    </View>
                </View>
            </Modal>
        </View>
    );
};

const styles = StyleSheet.create({
    webViewContainer: {
        flex: 1,
        backgroundColor: 'rgba(0, 0, 0, 0.8)',
        justifyContent: 'center',
        alignItems: 'center'
    },
    webViewWrapper: {
        width: '90%',
        height: '90%',
        paddingTop: 10
    },
    webViewHeader: {
        position: 'absolute',
        top: 40,
        right: 20,
        zIndex: 1,
    },
    closeButton: {
        backgroundColor: '#fff',
        borderRadius: 15,
        padding: 10,
    },
    closeButtonText: {
        fontSize: 18,
        fontWeight: 'bold',
        color: 'red',
    },
});

export default CustomTemplate;