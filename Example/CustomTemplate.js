import React, { useState, useEffect } from 'react';
import { View, Modal, Text, TouchableOpacity, StyleSheet } from 'react-native';
import InAppMessagePopup from './InAppMessagePopup';
import FunctionPopup from './FunctionPopup';
import { WebView } from 'react-native-webview';

const CleverTap = require('clevertap-react-native');

const WebViewCaller = Object.freeze({
    INAPP_POPUP: "InAppPopup",
    FUNCTION_POPUP: "FunctionPopup"
});

const CustomTemplate = () => {
    const [modalState, setModalState] = useState({
        isTemplateVisible: false,
        isNonVisualFunctionVisible: false,
        templateName: '',
        templateDescription: '',
        isStandaloneFunction: false,
        functionName: '',
        functionDescription: '',
        webViewCaller: ''
    });

    const [filePath, setFilePath] = useState('');
    const [showWebView, setShowWebView] = useState(false);

    useEffect(() => {
        CleverTap.addListener(CleverTap.CleverTapCustomTemplatePresent, templateName => {
            presentInAppModal(templateName, false);
        });

        CleverTap.addListener(CleverTap.CleverTapCustomTemplateClose, templateName => {
            console.log('Closing template from "Close Notification" action.');
            CleverTap.customTemplateSetDismissed(templateName);
            setModalState(prevState => ({ ...prevState, isTemplateVisible: false }));
        });

        CleverTap.addListener(CleverTap.CleverTapCustomFunctionPresent, templateName => {
            presentInAppModal(templateName, true);
        });

        return () => {
            CleverTap.removeListener(CleverTap.CleverTapCustomTemplatePresent);
            CleverTap.removeListener(CleverTap.CleverTapCustomTemplateClose);
            CleverTap.removeListener(CleverTap.CleverTapCustomFunctionPresent);
        };
    }, []);

    const presentInAppModal = (name, isFunction) => {
        getTemplateValuesString(name).then((str) => {
            let description = `Arguments for "${name}":${str}`;
            setModalState(prevState => {
                // If the in-app template modal is already visible, this means `presentInAppModal`
                // was called by a isVisual:false function triggered by an action.
                if (prevState.isTemplateVisible) {
                    console.log(`Showing a custom function "${name}" with isVisual:false,
triggered by action from template "${prevState.templateName}".`);
                    // Prepare to show the non-visual function popup
                    return {
                        ...prevState,
                        isTemplateVisible: false,
                        isNonVisualFunctionVisible: true,
                        functionDescription: description,
                        functionName: name,
                    };
                } else {
                    // Show the InApp Popup
                    return {
                        ...prevState,
                        isTemplateVisible: true,
                        isNonVisualFunctionVisible: false,
                        templateDescription: description,
                        templateName: name,
                        isStandaloneFunction: isFunction
                    };
                }
            });
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
                    return `
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
                    return `
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
                    return `
color=${color}
enabled=${enabled}
image=${image}`;
                } catch (e) {
                    return e.toString();
                }

            default:
                return "Template not found"
        }
    };

    const handleCancel = () => {
        let name = modalState.templateName;
        setModalState(prevState => ({
            ...prevState,
            isTemplateVisible: false,
            templateName: '',
            templateDescription: ''
        }));

        console.log(`Dismissing ${modalState.isStandaloneFunction ? 'standalone function' : 'template'} named: "${name}".`);
        CleverTap.customTemplateSetDismissed(name);
    };

    const handleFunctionClose = () => {
        console.log(`Closing isVisual:false function named: "${modalState.functionName}".`);
        setModalState(prevState => ({
            ...prevState,
            isTemplateVisible: true,
            isNonVisualFunctionVisible: false,
            functionDescription: '',
            functionName: ''
        }));
    };

    const handleConfirm = () => {
        CleverTap.customTemplateSetPresented(modalState.templateName);
    };

    const handleTriggerAction = (actionName) => {
        console.log('Trigger action argument named:', actionName);
        CleverTap.customTemplateRunAction(modalState.templateName, actionName);
    };

    const getCurrentName = () => {
        return modalState.isTemplateVisible ? modalState.templateName : modalState.functionName;
    };

    const handleOpenFile = (name) => {
        console.log('Open file argument named:', name);
        CleverTap.customTemplateGetFileArg(getCurrentName(), name).then((filePath) => {
            console.log('Open file path:', filePath);
            setFilePath(filePath);

            setModalState(prevState => ({
                ...prevState,
                isTemplateVisible: false,
                isNonVisualFunctionVisible: false,
                webViewCaller: prevState.isTemplateVisible ? WebViewCaller.INAPP_POPUP : WebViewCaller.FUNCTION_POPUP,
            }));
            setShowWebView(true);
        });
    };

    const closeWebView = () => {
        setShowWebView(false);
        setFilePath('');
        setModalState(prevState => ({
            ...prevState,
            isTemplateVisible: prevState.webViewCaller == WebViewCaller.INAPP_POPUP,
            isNonVisualFunctionVisible: prevState.webViewCaller == WebViewCaller.FUNCTION_POPUP,
        }));
    };

    return (
        <View style={styles.customTemplateContainer}>
            <InAppMessagePopup
                visible={modalState.isTemplateVisible}
                title={modalState.templateName}
                description={modalState.templateDescription}
                isFunction={modalState.isStandaloneFunction}
                onCancel={handleCancel}
                onConfirm={handleConfirm}
                onTriggerAction={handleTriggerAction}
                onFileOpen={handleOpenFile}
            >
            </InAppMessagePopup>

            <FunctionPopup
                visible={modalState.isNonVisualFunctionVisible}
                title={modalState.functionName}
                description={modalState.functionDescription}
                onClose={handleFunctionClose}
                onFileOpen={handleOpenFile}
            >
            </FunctionPopup>

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
                            style={styles.webView}
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
    customTemplateContainer: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center'
    },
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
    webView: {
        flex: 1,
        backgroundColor: 'transparent'
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
    }
});

export default CustomTemplate;