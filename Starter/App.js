/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 * @flow
 */

import React, { Component } from 'react';
import { Platform, StyleSheet, Text, View, Button, Alert, TouchableHighlight, Linking, SectionList } from 'react-native';
const CleverTap = require('clevertap-react-native');

const instructions = Platform.select({
    ios: 'Press Cmd+R to reload,\n' + 'Cmd+D or shake for dev menu',
    android:
        'Double tap R on your keyboard to reload,\n' +
        'Shake or press menu button for dev menu',
});

const sectionsList = [
    {
        title: 'EVENTS', data: [{ id: '_recordEvent', title: 'Record Event' },
        { id: '_recordEventWithProps', title: 'Record Event With Properties' },
        { id: '_recordChargedEvent', title: 'Record Charged Event' }]
    },
    {
        title: 'USER PROFILE', data: [
            { id: '_updateUserProfile', title: 'Profile Push' },
            { id: '_onUserLogin', title: 'On User Login, create new profile' },
            { id: '_setMultiValuesForKey', title: 'Set Multi Values For Key' },
            { id: '_addMultiValuesForKey', title: 'Add Multi Values For Key' },
            { id: '_addMultiValueForKey', title: 'Add Multi Value For Key' },
            { id: '_removeMultiValueForKey', title: 'Remove Multi Value For Key' },
            { id: '_removeMultiValuesForKey', title: 'Remove Multi Values For Key' },
            { id: '_getUserProfileProperty', title: 'Get User Profile Property' }]
    },
    {
        title: 'APP INBOX', data: [
            { id: '_openInbox', title: 'Open Inbox' },
            { id: '_showCounts', title: 'Show Counts' },
            { id: '_getAllInboxMessages', title: 'Get All Inbox Messages' },
            { id: '_getUnreadInboxMessages', title: 'Get Unread Messages' },
            { id: '_customAppInboxAPI', title: 'Custom App Inbox API' }]
    },
    {
        title: 'DISPLAY UNITS', data: [
            { id: '_getDisplayUnitForId', title: 'Get Display Unit For Id' },
            { id: '_getAllDisplayUnits', title: 'Get All Display Units' }]
    },
    {
        title: 'PRODUCT CONFIGS', data: [
            { id: '_setDefaultProductConfigs', title: 'Set Default Product Configs' },
            { id: '_fetch', title: 'Fetch' },
            { id: '_activate', title: 'Activate' },
            { id: '_fetchAndActivate', title: 'Fetch And Activate' },
            { id: '_resetProductConfig', title: 'Reset' },
            { id: '_fetchWithMinimumIntervalInSeconds', title: 'Fetch With Minimum Fetch Interval In Seconds' },
            { id: '_getProductConfigs', title: 'Get Product Configs' }]
    },
    {
        title: 'FEATURE FLAGS', data: [
            { id: '_getFeatureFlag', title: 'Get Feature Flag' }]
    },
    {
        title: 'APP PERSONALISATION', data: [
            { id: '_enablePersonalization', title: 'Enable Personalization' },
            { id: '_disablePersonalization', title: 'Disable Personalization' }]
    },
    {
        title: 'ENABLE DEBUGGING', data: [
            { id: '_setDebugLevel', title: 'Set Debug Level' }]
    }
]

type Props = {};
export default class App extends Component<Props> {

    componentWillMount() {
        console.log('Component WILL MOUNT123!')
    }
    componentDidMount() {
        // optional: add listeners for CleverTap Events
        CleverTap.addListener(CleverTap.CleverTapProfileDidInitialize, (event) => { this._handleCleverTapEvent(CleverTap.CleverTapProfileDidInitialize, event); });
        CleverTap.addListener(CleverTap.CleverTapProfileSync, (event) => { this._handleCleverTapEvent(CleverTap.CleverTapProfileSync, event); });
        CleverTap.addListener(CleverTap.CleverTapInAppNotificationDismissed, (event) => { this._handleCleverTapEvent(CleverTap.CleverTapInAppNotificationDismissed, event); });
        CleverTap.addListener(CleverTap.CleverTapInboxDidInitialize, (event) => { this._handleCleverTapInbox(CleverTap.CleverTapInboxDidInitialize, event); });
        CleverTap.addListener(CleverTap.CleverTapInboxMessagesDidUpdate, (event) => { this._handleCleverTapInbox(CleverTap.CleverTapInboxMessagesDidUpdate, event); });
        CleverTap.addListener(CleverTap.CleverTapInboxMessageButtonTapped, (event) => { this._handleCleverTapInbox(CleverTap.CleverTapInboxMessageButtonTapped, event); });
        CleverTap.addListener(CleverTap.CleverTapDisplayUnitsLoaded, (event) => { this._handleCleverTapDisplayUnitsLoaded(CleverTap.CleverTapDisplayUnitsLoaded, event); });
        CleverTap.addListener(CleverTap.CleverTapInAppNotificationButtonTapped, (event) => { this._handleCleverTapEvent(CleverTap.CleverTapInAppNotificationButtonTapped, event); });
        CleverTap.addListener(CleverTap.CleverTapFeatureFlagsDidUpdate, (event) => { this._handleCleverTapEvent(CleverTap.CleverTapFeatureFlagsDidUpdate, event); });
        CleverTap.addListener(CleverTap.CleverTapProductConfigDidInitialize, (event) => { this._handleCleverTapEvent(CleverTap.CleverTapProductConfigDidInitialize, event); });
        CleverTap.addListener(CleverTap.CleverTapProductConfigDidFetch, (event) => { this._handleCleverTapEvent(CleverTap.CleverTapProductConfigDidFetch, event); });
        CleverTap.addListener(CleverTap.CleverTapProductConfigDidActivate, (event) => { this._handleCleverTapEvent(CleverTap.CleverTapProductConfigDidActivate, event); });
        CleverTap.addListener(CleverTap.CleverTapPushNotificationClicked, (event) => { this._handleCleverTapEvent(CleverTap.CleverTapPushNotificationClicked, event); });

        CleverTap.setDebugLevel(3);
        // for iOS only: register for push notifications
        CleverTap.registerForPush();

        // for iOS only; record a Screen View
        CleverTap.recordScreenView('HomeView');

        //Create notification channel for Android O and above
        CleverTap.createNotificationChannel("RNTesting", "React Native Testing", "React Native Testing", 4, true);
        //initialize the App Inbox
        CleverTap.initializeInbox();

        // Listener to handle incoming deep links
        Linking.addEventListener('url', this._handleOpenUrl);

        // this handles the case where a deep link launches the application
        Linking.getInitialURL().then((url) => {
            if (url) {
                console.log('launch url', url);
                this._handleOpenUrl({ url });
            }
        }).catch(err => console.error('launch url error', err));

        // check to see if CleverTap has a launch deep link
        // handles the case where the app is launched from a push notification containing a deep link
        CleverTap.getInitialUrl((err, url) => {
            if (url) {
                console.log('CleverTap launch url', url);
                this._handleOpenUrl({ url }, 'CleverTap');
            } else if (err) {
                console.log('CleverTap launch url', err);
            }
        });
    }

    componentWillUnmount() {
        // clean up listeners
        Linking.removeEventListener('url', this._handleOpenUrl);
        CleverTap.removeListener(CleverTap.CleverTapProfileDidInitialize);
        CleverTap.removeListener(CleverTap.CleverTapProfileSync);
        CleverTap.removeListener(CleverTap.CleverTapInAppNotificationDismissed);
        CleverTap.removeListener(CleverTap.CleverTapInboxDidInitialize);
        CleverTap.removeListener(CleverTap.CleverTapInboxMessagesDidUpdate);
        CleverTap.removeListener(CleverTap.CleverTapInboxMessageButtonTapped);
        CleverTap.removeListener(CleverTap.CleverTapDisplayUnitsLoaded);
        CleverTap.removeListener(CleverTap.CleverTapInAppNotificationButtonTapped);
        CleverTap.removeListener(CleverTap.CleverTapFeatureFlagsDidUpdate);
        CleverTap.removeListener(CleverTap.CleverTapProductConfigDidInitialize);
        CleverTap.removeListener(CleverTap.CleverTapProductConfigDidFetch);
        CleverTap.removeListener(CleverTap.CleverTapProductConfigDidActivate);
        CleverTap.removeListener(CleverTap.CleverTapPushNotificationClicked);
    }

    _handleOpenUrl(event, from) {
        console.log('handleOpenUrl', event.url, from);
    }

    _handleCleverTapEvent(eventName, event) {
        console.log('handleCleverTapEvent', eventName, event);
    }

    _handleCleverTapInbox(eventName, event) {
        console.log('handleCleverTapInbox', eventName, event);
    }

    _handleCleverTapDisplayUnitsLoaded(eventName, event) {
        console.log('handleCleverTapDisplayUnitsLoaded', eventName, event);
    }

    _recordEvent(event) {
        CleverTap.recordEvent('testEvent');
        if (Platform.OS === 'android') {
            CleverTap.setPushToken("FCM-Token", CleverTap.FCM);
        }
    }

    _recordEventWithProps(event) {
        CleverTap.recordEvent('testEventWithProps', { 'start': new Date(), 'foo': 'bar' });
    }

    _recordChargedEvent(event) {
        CleverTap.recordChargedEvent({ 'totalValue': 20, 'category': 'books' }, [{ 'title': 'book1' }, { 'title': 'book2' }, { 'title': 'book3' }]);
        CleverTap.recordChargedEvent({ 'totalValue': 20, 'category': 'books', 'purchase_date': new Date() },
            [{ 'title': 'book1', 'published_date': new Date('2010-12-12T06:35:31'), 'author': 'ABC' },
            { 'title': 'book2', 'published_date': new Date('2000-12-12T06:35:31') },
            {
                'title': 'book3', 'published_date': new Date(), 'author': 'XYZ'
            }]
        );

    }

    _updateUserProfile(event) {
        CleverTap.profileSet({
            'Name': 'testUserA1', 'Identity': '123456', 'Email': 'test@test.com', 'custom1': 123,
            'birthdate': new Date('2020-03-03T06:35:31')
        });
        CleverTap.setLocation(34.15, -118.20);
    }

    _onUserLogin(event) {
        CleverTap.onUserLogin({
            'Name': 'testUserA1', 'Identity': new Date().getTime() + '',
            'Email': new Date().getTime() + 'testmobile@test.com', 'custom1': 123,
            'birthdate': new Date('1992-12-22T06:35:31')
        })
    }

    _setMultiValuesForKey(event) {
        CleverTap.profileSetMultiValuesForKey(['a', 'b', 'c'], 'letters');
    }

    _addMultiValuesForKey(event) {
        CleverTap.profileAddMultiValuesForKey(['e', 'f'], 'letters');

    }

    _addMultiValueForKey(event) {
        CleverTap.profileAddMultiValueForKey('d', 'letters');
    }

    _removeMultiValuesForKey(event) {
        CleverTap.profileRemoveMultiValuesForKey(['a', 'c'], 'letters');

    }

    _removeMultiValueForKey(event) {
        CleverTap.profileRemoveMultiValueForKey('b', 'letters');
    }

    _getUserProfileProperty(event) {

        CleverTap.profileGetProperty('Name', (err, res) => {
            console.log('CleverTap Profile Name: ', res, err);
        });

        CleverTap.profileGetCleverTapID((err, res) => {
            console.log('CleverTapID', res, err);
        });

        CleverTap.profileGetCleverTapAttributionIdentifier((err, res) => {
            console.log('CleverTapAttributionIdentifier', res, err);
        });
    }

    _openInbox(event) {
        CleverTap.showInbox({
            'tabs': ['Offers', 'Promotions'], 'navBarTitle': 'My App Inbox', 'navBarTitleColor': '#FF0000', 'navBarColor': '#FFFFFF', 'inboxBackgroundColor': '#AED6F1', 'backButtonColor': '#00FF00'
            , 'unselectedTabColor': '#0000FF', 'selectedTabColor': '#FF0000', 'selectedTabIndicatorColor': '#000000',
            'noMessageText': 'No message(s)', 'noMessageTextColor': '#FF0000'
        });
    }

    _showCounts(event) {
        CleverTap.getInboxMessageCount((err, res) => {
            console.log('Total Messages: ', res, err);
        });
        CleverTap.getInboxMessageUnreadCount((err, res) => {
            console.log('Unread Messages: ', res, err);
        });
    }

    _getAllInboxMessages(event) {
        CleverTap.getAllInboxMessages((err, res) => {
            console.log('All Inbox Messages: ', res, err);
        });
    }

    _getUnreadInboxMessages(event) {
        CleverTap.getUnreadInboxMessages((err, res) => {
            console.log('Unread Inbox Messages: ', res, err);
        });
    }

    _customAppInboxAPI(event) {
        CleverTap.getInboxMessageForId('Message Id', (err, res) => {
            console.log("marking message read = " + res);
        });

        CleverTap.deleteInboxMessageForId('Message Id');
        CleverTap.markReadInboxMessageForId('Message Id');
        CleverTap.pushInboxNotificationViewedEventForId('Message Id');
        CleverTap.pushInboxNotificationClickedEventForId('Message Id');
    }

    _getDisplayUnitForId(event) {
        CleverTap.getDisplayUnitForId('Unit Id', (err, res) => {
            console.log('Get Display Unit for Id:', res, err);
        });
    }

    _getAllDisplayUnits(event) {
        CleverTap.getAllDisplayUnits((err, res) => {
            console.log('All Display Units: ', res, err);
        });
    }

    //Product configs

    _setDefaultProductConfigs(event) {
        CleverTap.setDefaultsMap({ 'text_color': 'red', 'msg_count': 100, 'price': 100.50, 'is_shown': true, 'json': '{"key":"val"}' });
    }

    _fetch(event) {
        CleverTap.fetch();
    }

    _activate(event) {
        CleverTap.activate();
    }

    _fetchAndActivate(event) {
        CleverTap.fetchAndActivate();
    }

    _resetProductConfig(event) {
        CleverTap.resetProductConfig();
    }

    _fetchWithMinimumIntervalInSeconds() {
        CleverTap.fetchWithMinimumIntervalInSeconds(60);
    }

    _setMinimumFetchIntervalInSeconds() {
        CleverTap.setMinimumFetchIntervalInSeconds(60);
    }

    _getLastFetchTimeStampInMillis(event) {
        CleverTap.getLastFetchTimeStampInMillis((err, res) => {
            console.log('LastFetchTimeStampInMillis in string: ', res, err);
        });
    }

    _getProductConfigs(event) {
        CleverTap.getProductConfigString('text_color', (err, res) => {
            console.log('PC text_color val in string :', res, err);
        });
        CleverTap.getProductConfigBoolean('is_shown', (err, res) => {
            console.log('PC is_shown val in boolean :', res, err);
        });
        CleverTap.getNumber('msg_count', (err, res) => {
            console.log('PC msg_count val in number :', res, err);
        });
        CleverTap.getNumber('price', (err, res) => {
            console.log('PC price val in number :', res, err);
        });
        CleverTap.getProductConfigString('json', (err, res) => {
            console.log('PC json val in string :', res, err);
        });

    }

    // Feature flags

    _getFeatureFlag(event) {
        CleverTap.getFeatureFlag('is_dark_mode', false, (err, res) => {
            console.log('FF is_dark_mode val in boolean :', res, err);
        });
    }


    // Personalization

    _enablePersonalization() {
        CleverTap.enablePersonalization();
    }

    _disablePersonalization() {
        CleverTap.disablePersonalization();
    }

    // Enable Debugging

    _setDebugLevel() {
        CleverTap.setDebugLevel(3);
    }

    _onListItemClick(item) {
        console.log('_onListItemClick', item.id);
        switch (item.id) {
            case "_recordEvent":
                this._recordEvent();
                break;
            case "_recordChargedEvent":
                this._recordChargedEvent();
                break;
            case "_updateUserProfile":
                this._updateUserProfile();
                break;
            case "_onUserLogin":
                this._onUserLogin();
                break;
            case "_getUserProfileProperty":
                this._getUserProfileProperty();
                break;
            case "_openInbox":
                this._openInbox();
                break;
            case "_showCounts":
                this._showCounts();
                break;
            case "_getAllInboxMessages":
                this._getAllInboxMessages();
                break;
            case "_getUnreadInboxMessages":
                this._getUnreadInboxMessages();
                break;
            case "_customAppInboxAPI":
                this._customAppInboxAPI();
                break;
            case "_getDisplayUnitForId":
                this._getDisplayUnitForId();
                break;
            case "_getAllDisplayUnits":
                this._getAllDisplayUnits();
                break;
            case "_setDefaultProductConfigs":
                this._setDefaultProductConfigs();
                break;
            case "_fetch":
                this._fetch();
                break;
            case "_activate":
                this._activate();
                break;
            case "_fetchAndActivate":
                this._fetchAndActivate();
                break;
            case "_resetProductConfig":
                this._resetProductConfig();
                break;
            case "_fetchWithMinimumIntervalInSeconds":
                this._fetchWithMinimumIntervalInSeconds();
                break;
            case "_getProductConfigs":
                this._getProductConfigs();
                break;
            case "_getFeatureFlag":
                this._getFeatureFlag();
                break;
            case "_setDebugLevel":
                this._setDebugLevel();
                break;
            case "_recordEventWithProps":
                this._recordEventWithProps();
                break;
            case "_setMultiValuesForKey":
                this._setMultiValuesForKey();
                break;
            case "_addMultiValuesForKey":
                this._addMultiValuesForKey();
                break;
            case "_removeMultiValuesForKey":
                this._removeMultiValuesForKey();
                break;
            case "_addMultiValueForKey":
                this._addMultiValueForKey();
                break;
            case "_removeMultiValueForKey":
                this._removeMultiValueForKey();
                break;
            case "_enablePersonalization":
                this._enablePersonalization();
                break;
            case "_disablePersonalization":
                this._disablePersonalization();
                break;
        }
    }


    render() {
        return (
            <View style={styles.containerList}>
                <Text style={styles.instructionsApi}>Please go through "index.js" file for all sets of APIs</Text>
                <SectionList
                    sections={sectionsList}
                    renderItem={({ item, index }) => <TouchableHighlight underlayColor='#cce0ff' onPress={() => this._onListItemClick(item)} style={styles.item}><Text>{item.title}</Text></TouchableHighlight>}
                    renderSectionHeader={({ section }) => <Text style={styles.sectionHeader}>{section.title}</Text>}
                    keyExtractor={(item, index) => index}
                    stickySectionHeadersEnabled={true}
                    stickyHeaderIndices={[0]}
                />
            </View>
        );
    }
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
        backgroundColor: '#F5FCFF',
    },
    welcome: {
        fontSize: 20,
        textAlign: 'center',
        margin: 10,
    },
    instructions: {
        textAlign: 'center',
        color: '#333333',
        marginBottom: 5,

    },
    button: {
        marginBottom: 20,
        backgroundColor: '#fcca03',

    },
    item: {
        padding: 14,
        justifyContent: 'center',
        fontSize: 18,
        fontWeight: 'bold',
        height: 54,
        marginBottom: 5,
        backgroundColor: '#E5E7EB',

    },
    sectionHeader: {
        padding: 18,
        paddingLeft: 10,
        paddingRight: 10,
        paddingBottom: 18,
        fontSize: 16,
        fontWeight: 'bold',
        backgroundColor: '#EF4444',
        color: '#FFFFFF'
    },
    containerList: {
        flex: 1,
        paddingBottom: 10
    },
    instructionsApi: {
        textAlign: 'center',
        backgroundColor: '#fcba03',
        color: '#FFFFFF',
        fontWeight: 'bold',
        marginBottom: 5,
        paddingBottom: 10,
        paddingTop: 64,
    }
});
