import Toast from 'react-native-toast-message';
import {
    Platform,
    Linking
} from 'react-native';

const CleverTap = require('clevertap-react-native');

const toastsQueue = [];
var toastIsShowing = false;

export const showToast = (text1, text2) => {

    if (toastIsShowing) {
        toastsQueue.push([text1, text2]);
        return;
    }

    toastIsShowing = true;

    const queueNext = () => {
        toastIsShowing = false;
        if (toastsQueue.length > 0) {
            let item = toastsQueue.shift();
            showToast(...item);
        }
    };

    Toast.show({
        type: 'info',
        position: 'bottom',
        visibilityTime: 2000,
        text1: text1,
        text2: text2 ?? '',
        onHide: queueNext,
    });
};

export const set_userProfile = () => {
    showToast('User Profile Updated');

    CleverTap.profileSet({
        Name: 'testUserA1',
        Identity: '123456',
        Email: 'test@test.com',
        custom1: 123,
        birthdate: new Date('2020-03-03T06:35:31'),
    });
};

// Identity_Management
export const onUser_Login = () => {
    showToast('User Profile Updated');

    // On user Login
    CleverTap.onUserLogin({
        Name: 'testUserA1',
        Identity: new Date().getTime() + '',
        Email: new Date().getTime() + 'testmobile@test.com',
        custom1: 123,
        birthdate: new Date('1992-12-22T06:35:31'),
    });
};

export const getCleverTap_id = () => {
    // Below method is deprecated since 0.6.0, please check index.js for deprecation, instead use CleverTap.getCleverTapID()
    /*CleverTap.profileGetCleverTapID((err, res) => {
          console.log('CleverTapID', res, err);
          alert(`CleverTapID: \n ${res}`);
      });*/

    // Use below newly added method
    CleverTap.getCleverTapID((err, res) => {
        console.log('CleverTapID', res, err);
        alert(`CleverTapID: \n ${res}`);
    });
};

// Location
export const set_userLocation = () => {
    showToast('User Location set');

    CleverTap.setLocation(34.15, -118.2);
};

// Locale
export const set_Locale = () => {
    showToast('User Locale set');

    CleverTap.setLocale("en_IN");
};

// Events
export const pushevent = () => {
    showToast('Event Recorded');

    // Recording an Event
    // CleverTap.recordEvent('testEvent');
    // CleverTap.recordEvent('Send Basic Push');
    // CleverTap.recordEvent('testEventWithProps', {start: new Date(), foo: 'bar'});
    CleverTap.recordEvent('pushEvent');
};

export const pushChargedEvent = () => {
    showToast('Charged Event Recorded');

    // Recording an Event
    CleverTap.recordChargedEvent(
        { totalValue: 20, category: 'books', purchase_date: new Date() },
        [
            {
                title: 'book1',
                published_date: new Date('2010-12-12T06:35:31'),
                author: 'ABC',
            },
            { title: 'book2', published_date: new Date('2000-12-12T06:35:31') },
            { title: 'book3', published_date: new Date(), author: 'XYZ' },
        ],
    );
};

export const getUserEventLog = () => {
    CleverTap.getUserEventLog("testEvent", (err, res) => {
        console.log('User Event Log: ', res);
        showToast(`User Event Log: ${JSON.stringify(res)}`);
    });
};

export const getUserEventLogCount = () => {
    CleverTap.getUserEventLogCount("testEvent", (err, res) => {
        console.log('User Event Log Count: ', res);
        showToast(`User Event Log Count: ${res}`);
    });
};

export const getUserLastVisitTs = () => {
    CleverTap.getUserLastVisitTs((err, res) => {
        console.log('User Last Visit Timestamp: ', res);
        showToast(`User Last Visit Timestamp: ${res}`);
    });
};

export const getUserAppLaunchCount = () => {
    CleverTap.getUserAppLaunchCount((err, res) => {
        console.log('User App Launch Count: ', res);
        showToast(`User App Launch Count: ${res}`);
    });
};

export const getUserEventLogHistory = () => {
    CleverTap.getUserEventLogHistory((err, res) => {
        console.log('User Event Log History: ', res);
        showToast(`User Event Log History: ${JSON.stringify(res)}`);
    });
};


// App Inbox
export const show_appInbox = () => {
    // Show Inbox
    CleverTap.showInbox({
        navBarTitle: 'My App Inbox',
        navBarTitleColor: '#FF0000',
        navBarColor: '#FFFFFF',
        inboxBackgroundColor: '#AED6F1',
        backButtonColor: '#00FF00',
        unselectedTabColor: '#0000FF',
        selectedTabColor: '#FF0000',
        selectedTabIndicatorColor: '#000000',
        noMessageText: 'No message(s)',
        noMessageTextColor: '#FF0000',
    });
};

export const show_appInboxwithTabs = () => {
    // Show Inbox
    CleverTap.showInbox({
        tabs: ['Offers', 'Promotions'],
        navBarTitle: 'My App Inbox',
        navBarTitleColor: '#FF0000',
        navBarColor: '#FFFFFF',
        inboxBackgroundColor: '#AED6F1',
        backButtonColor: '#00FF00',
        unselectedTabColor: '#0000FF',
        selectedTabColor: '#FF0000',
        selectedTabIndicatorColor: '#000000',
        noMessageText: 'No message(s)',
        noMessageTextColor: '#FF0000',
        firstTabTitle: "First Tab",
    });
};

export const get_TotalMessageCount = () => {
    // Get the total message count
    CleverTap.getInboxMessageCount((err, res) => {
        console.log('Total Messages: ', res, err);
        showToast(`Total Messages: \n ${res}`);
    });
};

export const get_UnreadMessageCount = () => {
    // Get the count of unread messages
    CleverTap.getInboxMessageUnreadCount((err, res) => {
        console.log('Unread Messages: ', res, err);
        showToast(`Unread Messages: \n ${res}`);
    });
};

export const Get_All_InboxMessages = () => {
    // Get All Inbox Messages
    CleverTap.getAllInboxMessages((err, res) => {
        console.log('All Inbox Messages: ', res, err);
        showToast(`All Inbox Messages: \n ${res}`);

        // Uncomment to print payload data.
        // printInboxMessagesArray(res);
    });
};

export const get_All_InboxUnreadMessages = () => {
    // Get all unread messages
    CleverTap.getUnreadInboxMessages((err, res) => {
        console.log('Unread Inbox Messages: ', res, err);
        showToast(`Unread Inbox Messages: \n ${res}`);

        // Uncomment to print payload data.
        // printInboxMessagesArray(res);
    });
};

export const Get_InboxMessageForId = () => {
    // Get inbox Id
    CleverTap.getInboxMessageForId('Message Id', (err, res) => {
        console.log('marking message read = ' + res);
        showToast(`marking message read: \n ${res}`);

        // Uncomment to print payload data.
        // printInboxMessageMap(res);
    });
};

export const delete_InboxMessageForId = () => {
    showToast('Check Console for values');
    CleverTap.deleteInboxMessageForId('Message Id');
};

export const markRead_InboxMessageForId = () => {
    showToast('Check Console for values');
    CleverTap.markReadInboxMessageForId('Message Id');
};

export const pushInboxNotificationViewed = () => {
    showToast('Check Console for values');
    CleverTap.pushInboxNotificationViewedEventForId('Message Id');
};

export const pushInboxNotificationClicked = () => {
    showToast('Check Console for values');
    CleverTap.pushInboxNotificationClickedEventForId('Message Id');
};

export const printInboxMessagesArray = (data) => {
    if (data != null) {
        console.log('Total Inbox Message count = ' + data.length);
        data.forEach(inboxMessage => {
            printInboxMessageMap(inboxMessage);
        });
    }
};

export const printInboxMessageMap = (inboxMessage) => {
    if (inboxMessage != null) {
        console.log('Inbox Message wzrk_id = ' + inboxMessage['wzrk_id']);
        let msg = inboxMessage['msg'];
        console.log('Type of Inbox = ' + msg['type']);
        let content = msg['content'];
        content.forEach(element => {
            let title = element['title'];
            let message = element['message'];
            console.log('Inbox Message Title = ' + title['text'] + ' and message = ' + message['text']);
            let action = element['action'];
            let links = action['links'];
            links.forEach(link => {
                console.log('Inbox Message have link type = ' + link['type']);
            });
        });
    }
};

// Push Notifications
export const create_NotificationChannel = () => {
    showToast('Notification Channel Created');
    // Creating Notification Channel
    CleverTap.createNotificationChannel(
        'CtRNS',
        'Clever Tap React Native Testing',
        'CT React Native Testing',
        1,
        true,
    );
    CleverTap.createNotificationChannel(
        'BRTesting',
        'Clever Tap BR Testing',
        'CT BR Testing',
        1,
        true,
    );
    CleverTap.createNotificationChannel(
        'PTTesting',
        'Clever Tap PT Testing',
        'CT PT Testing',
        1,
        true,
    );
};

export const delete_NotificationChannel = () => {
    showToast('Notification Channel Deleted');
    // Delete Notification Channel
    CleverTap.deleteNotificationChannel('CtRNS');
};

export const create_NotificationChannelGroup = () => {
    showToast('Notification Channel Group Created');
    // Creating a group notification channel
    CleverTap.createNotificationChannelGroup(
        'Offers',
        'All Offers related notifications',
    );
};

export const delete_NotificationChannelGroup = () => {
    showToast('Notification Channel Group Deleted');
    // Delete a group notification channel
    CleverTap.deleteNotificationChannelGroup('Offers');
};

export const pushFcmRegistrationId = () => {
    showToast('Registered FCM Id for Push');
    // Setting up a Push Notification
    if (Platform.OS === 'android') {
        // Use only during custom implementation and make sure that FCM credentials used to generate token are same as CleverTap
        // or else two different tokens will be pushed to BackEnd resulting in unwanted behavior
        // => https://github.com/CleverTap/clevertap-react-native/issues/166
        // => https://developer.clevertap.com/docs/android#section-custom-android-push-notifications-handling
        CleverTap.setPushToken('1000test000token000fcm', CleverTap.FCM);
        // CleverTap.setPushToken("111056687894", CleverTap.HMS);//for Huawei push
        // CleverTap.setPushToken("111056687894", CleverTap.BPS);//for Baidu push
    }
};

export const create_notification = () => {
    // createNotification in your custom implementation => https://developer.clevertap.com/docs/android#section-custom-android-push-notifications-handling

    // Please note, extras passed in below method is just for showcase, you need to pass the one that you receive from FCM
    CleverTap.createNotification({
        wzrk_acct_id: '88R-R54-5Z6Z',
        nm: 'Testing 1..2..3..',
        nt: 'Test event',
        pr: 'max',
        wzrk_pivot: 'wzrk_default',
        wzrk_ttl_s: '2419200',
        wzrk_cid: 'CtRNS',
        wzrk_pid: new Date().getTime(),
        wzrk_rnv: false,
        wzrk_ttl: '1627053990',
        wzrk_push_amp: false,
        wzrk_bc: '',
        wzrk_bi: '2',
        wzrk_dt: 'FIREBASE',
        wzrk_id: '1624627506_20210625',
        wzrk_pn: true,
    });
};

export const createNotificationChannelWithSound = () => {
    // https://developer.clevertap.com/docs/add-a-sound-file-to-your-android-app

    CleverTap.createNotificationChannelWithSound(
        'CtRNS',
        'Clever Tap React Native Testing',
        'CT React Native Testing',
        1,
        true,
        'glitch.mp3',
    );
};

export const createNotificationChannelWithGroupId = () => {
    // https://developer.clevertap.com/docs/android#section-push-notifications-for-android-o

    CleverTap.createNotificationChannelWithGroupId(
        'offersMonthly',
        'Monthly Offers',
        'Offers given at every month',
        1,
        'Offers',
        true,
    );
    CleverTap.createNotificationChannelWithGroupId(
        'offersQuarterly',
        'Quarterly Offers',
        'Offers given at every Quarter',
        1,
        'Offers',
        true,
    );
};

export const createNotificationChannelWithGroupIdAndSound = () => {
    // https://developer.clevertap.com/docs/android#section-push-notifications-for-android-o

    CleverTap.createNotificationChannelWithGroupIdAndSound(
        'offersMonthly',
        'Monthly Offers',
        'Offers given at every month',
        1,
        'Offers',
        true,
        'glitch.mp3',
    );
    CleverTap.createNotificationChannelWithGroupIdAndSound(
        'offersQuarterly',
        'Quarterly Offers',
        'Offers given at every Quarter',
        1,
        'Offers',
        true,
        'glitch.mp3',
    );
};

// Native Display
export const getUnitID = () => {
    CleverTap.getDisplayUnitForId('Unit Id', (err, res) => {
        console.log('Get Display Unit for Id:', res, err);
        showToast(`Get Display Unit for Id: ${res}`);

        // Uncomment to access payload.
        // printDisplayUnit(res);
    });
};

export const getAllDisplayUnits = () => {
    CleverTap.getAllDisplayUnits((err, res) => {
        console.log('All Display Units: ', res, err);
        showToast(`All Display Units: ${res}`);

        // Uncomment to access payload.
        // printDisplayUnitsPayload(res);
    });
};

// Product Config
export const productConfig = () => {
    showToast('Product Configuration set to default');
    CleverTap.setDefaultsMap({
        text_color: 'red',
        msg_count: 100,
        price: 100.5,
        is_shown: true,
        json: '{"key":"val"}',
    });
};

export const fetch = () => {
    CleverTap.fetch();
};

export const activate = () => {
    CleverTap.activate();
};

export const fetchAndActivate = () => {
    CleverTap.fetchAndActivate();
};

export const fetchwithMinIntervalinsec = () => {
    CleverTap.fetchWithMinimumIntervalInSeconds(60);
};

export const setMinimumFetchIntervalInSeconds = () => {
    CleverTap.setMinimumFetchIntervalInSeconds(60);
};

export const getBoolean = () => {
    CleverTap.getProductConfigBoolean('is_shown', (err, res) => {
        console.log('PC is_shown val in boolean :', res, err);
        showToast(`PC is_shown val in boolean: ${res}`);
    });
};

export const getLong = () => {
    CleverTap.getNumber('msg_count', (err, res) => {
        console.log('PC is_shown val in number(long)  :', res, err);
        showToast(`PC is_shown val in number(long): ${res}`);
    });
};

export const getDouble = () => {
    CleverTap.getNumber('price', (err, res) => {
        console.log('PC price val in number :', res, err);
        showToast(`PC is_shown val in number(double) : ${res}`);
    });
};

export const getString = () => {
    CleverTap.getProductConfigString('text_color', (err, res) => {
        console.log('PC text_color val in string :', res, err);
        showToast(`PC is_shown val in String : ${res}`);
    });
};

export const getStrings = () => {
    CleverTap.getProductConfigString('json', (err, res) => {
        console.log('PC json val in string :', res, err);
        showToast(`PC json val in String: ${res}`);
    });
};

export const reset_config = () => {
    CleverTap.resetProductConfig();
};

export const getLastFetchTimeStampInMillis = () => {
    CleverTap.getLastFetchTimeStampInMillis((err, res) => {
        console.log('LastFetchTimeStampInMillis in string: ', res, err);
        showToast(`LastFetchTimeStampInMillis in string: ${res}`);
    });
};

// Feature flag
export const getFeatureFlag = () => {
    CleverTap.getFeatureFlag('is_dark_mode', false, (err, res) => {
        console.log('FF is_dark_mode val in boolean :', res, err);
        showToast(`FF is_dark_mode val in boolean: ${res}`);
    });
};

// App Personalisation
export const enablePersonalization = () => {
    CleverTap.enablePersonalization();
    showToast('Personalization enabled');
};

// Attributions
export const GetCleverTapAttributionIdentifier = () => {
    // Below method is deprecated since 0.6.0, please check index.js for deprecation, use CleverTap.getCleverTapID(callback) instead
    // Default Instance
    CleverTap.profileGetCleverTapAttributionIdentifier((err, res) => {
        console.log('CleverTapAttributionIdentifier', res, err);
        showToast(`CleverTapAttributionIdentifier: ${res}`);
    });
};

// CleverTap Listeners
export const _handleOpenUrl = (event, from) => {
    console.log('handleOpenUrl', event.url, from);
    showToast(`handleOpenUrl: ${event.url}, ${from}`);
};

export const removeCleverTapAPIListeners = () => {
    // Clean up listeners
    Linking.removeEventListener('url', _handleOpenUrl);
    CleverTap.removeListener(CleverTap.CleverTapProfileDidInitialize);
    CleverTap.removeListener(CleverTap.CleverTapProfileSync);
    CleverTap.removeListener(CleverTap.CleverTapInAppNotificationDismissed);
    CleverTap.removeListener(CleverTap.CleverTapInAppNotificationShowed);
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
    CleverTap.removeListener(CleverTap.CleverTapPushPermissionResponseReceived);
    showToast('Listeners removed successfully');
};

export const addCleverTapAPIListeners = (fromClick) => {
    // Add listeners for CleverTap Events
    CleverTap.addListener(CleverTap.CleverTapProfileDidInitialize, event => {
        _handleCleverTapEvent(CleverTap.CleverTapProfileDidInitialize, event);
    });
    CleverTap.addListener(CleverTap.CleverTapProfileSync, event => {
        _handleCleverTapEvent(CleverTap.CleverTapProfileSync, event);
    });
    CleverTap.addListener(
        CleverTap.CleverTapInAppNotificationDismissed,
        event => {
            _handleCleverTapInAppEvent(
                CleverTap.CleverTapInAppNotificationDismissed,
                event,
            );
        },
    );
    CleverTap.addListener(CleverTap.CleverTapInAppNotificationShowed, event => {
        _handleCleverTapInAppEvent(CleverTap.CleverTapInAppNotificationShowed, event);
    });
    CleverTap.addListener(CleverTap.CleverTapInboxDidInitialize, event => {
        _handleCleverTapInboxEvent(CleverTap.CleverTapInboxDidInitialize, event);
    });
    CleverTap.addListener(CleverTap.CleverTapInboxMessagesDidUpdate, event => {
        _handleCleverTapInboxEvent(
            CleverTap.CleverTapInboxMessagesDidUpdate,
            event,
        );
    });
    CleverTap.addListener(CleverTap.CleverTapInboxMessageButtonTapped, event => {
        _handleCleverTapInboxEvent(
            CleverTap.CleverTapInboxMessageButtonTapped,
            event,
        );
    });
    CleverTap.addListener(CleverTap.CleverTapInboxMessageTapped, event => {
        _handleCleverTapInboxEvent(CleverTap.CleverTapInboxMessageTapped, event);
    });

    CleverTap.addListener(CleverTap.CleverTapDisplayUnitsLoaded, event => {
        _handleCleverTapDisplayUnitsLoaded(
            CleverTap.CleverTapDisplayUnitsLoaded,
            event,
        );
    });
    CleverTap.addListener(
        CleverTap.CleverTapInAppNotificationButtonTapped,
        event => {
            _handleCleverTapInAppEvent(
                CleverTap.CleverTapInAppNotificationButtonTapped,
                event,
            );
        },
    );
    CleverTap.addListener(CleverTap.CleverTapFeatureFlagsDidUpdate, event => {
        _handleCleverTapEvent(CleverTap.CleverTapFeatureFlagsDidUpdate, event);
    });
    CleverTap.addListener(
        CleverTap.CleverTapProductConfigDidInitialize,
        event => {
            _handleCleverTapEvent(
                CleverTap.CleverTapProductConfigDidInitialize,
                event,
            );
        },
    );
    CleverTap.addListener(CleverTap.CleverTapProductConfigDidFetch, event => {
        _handleCleverTapEvent(CleverTap.CleverTapProductConfigDidFetch, event);
    });
    CleverTap.addListener(CleverTap.CleverTapProductConfigDidActivate, event => {
        _handleCleverTapEvent(CleverTap.CleverTapProductConfigDidActivate, event);
    });
    CleverTap.addListener(CleverTap.CleverTapPushNotificationClicked, event => {
        _handleCleverTapPushEvent(
            CleverTap.CleverTapPushNotificationClicked,
            event,
        );
    });
    CleverTap.addListener(
        CleverTap.CleverTapPushPermissionResponseReceived,
        event => {
            _handleCleverTapPushEvent(
                CleverTap.CleverTapPushPermissionResponseReceived,
                event,
            );
        },
    );
    if (fromClick) {
        showToast('Listeners added successfully');
    }
};

// CleverTap Event Handlers
export const _handleCleverTapEvent = (eventName, event) => {
    console.log('handleCleverTapEvent', eventName, event);
    showToast(`${eventName} called!`);

    // Uncomment to access payload for each events.
    // if (eventName == 'CleverTapProfileDidInitialize') {
    //   console.log('Profile did initialized with cleverTapID: '+ event['CleverTapID']);
    // }
    // if (eventName == 'CleverTapProfileSync') {
    //   console.log('Profile data updated with updates: ', event['updates']);
    // }
};

export const _handleCleverTapInboxEvent = (eventName, event) => {
    console.log('handleCleverTapInbox', eventName, event);
    showToast(`${eventName} called!`);

    // Uncomment to access payload for each events.
    // if (eventName == CleverTap.CleverTapInboxMessageTapped) {
    //   let contentPageIndex = event.contentPageIndex;
    //   let buttonIndex = event.buttonIndex;
    //   var data = event.data;
    //   let inboxMessageClicked = data.msg;
    //   console.log(
    //     'App Inbox ->',
    //     'InboxItemClicked at page-index ' +
    //       contentPageIndex +
    //       ' with button-index ' +
    //       buttonIndex,
    //   );

    //   //The contentPageIndex corresponds to the page index of the content, which ranges from 0 to the total number of pages for carousel templates. For non-carousel templates, the value is always 0, as they only have one page of content.
    //   let messageContentObject = inboxMessageClicked.content[contentPageIndex];

    //   //The buttonIndex corresponds to the CTA button clicked (0, 1, or 2). A value of -1 indicates the app inbox body/message clicked.
    //   if (buttonIndex != -1) {
    //     //button is clicked
    //     let buttonObject = messageContentObject.action.links[buttonIndex];
    //     let buttonType = buttonObject.type;
    //     switch (buttonType) {
    //       case 'copy':
    //         //this type copies the associated text to the clipboard
    //         let copiedText = buttonObject.copyText.text;
    //         console.log(
    //           'App Inbox ->',
    //           'copied text to Clipboard: ' + copiedText,
    //         );
    //         //_dismissAppInbox()
    //         break;

    //       case 'url':
    //         //this type fires the DeepLink
    //         let firedDeepLinkUrl = buttonObject.url.android.text;
    //         console.log(
    //           'App Inbox ->',
    //           'fired DeepLink url: ' + firedDeepLinkUrl,
    //         );
    //         //_dismissAppInbox();
    //         break;
    //       case 'kv':
    //         //this type contains the custom key-value pairs
    //         let kvPair = buttonObject.kv;
    //         console.log('App Inbox ->', 'custom key-value pair: ', kvPair);
    //         //_dismissAppInbox();
    //         break;
    //     }
    //   } else {
    //     //Item's body is clicked
    //     console.log(
    //       'App Inbox ->',
    //       'type/template of App Inbox item: ' + inboxMessageClicked.type,
    //     );
    //     //_dismissAppInbox();
    //   }
    // }

    // if (eventName == 'CleverTapInboxMessageButtonTapped') {
    //   console.log('Inbox message button tapped with customExtras:');
    //   for (const key of Object.keys(event)) {
    //     console.log('Value for key: ' + key + ' is:' + event[key]);
    //   }
    // }
};

export const _dismissAppInbox = () => {
    CleverTap.dismissInbox();
};

export const _handleCleverTapInAppEvent = (eventName, event) => {
    console.log('handleCleverTapInApp', eventName, event);
    showToast(`${eventName} called!`);

    // Uncomment to access payload for each events.
    // if (eventName == 'CleverTapInAppNotificationButtonTapped') {
    //   console.log('InApp button tapped with key-value pair:');
    //   for (const key of Object.keys(event)) {
    //     console.log('Value for key: '+ key + ' is:' + event[key]);
    //   }
    // }
    // if (eventName == 'CleverTapInAppNotificationDismissed') {
    //   let extras = event['extras'];
    //   let actionExtras = event['actionExtras'];
    //   console.log('InApp dismissed with extras: ', extras ,' and actionExtras: ', actionExtras);
    //   for (const key of Object.keys(extras)) {
    //     console.log('Value for extras key: '+ key + ' is:' + extras[key]);
    //   }
    //   for (const key of Object.keys(actionExtras)) {
    //     console.log('Value for actionExtras key: '+ key + ' is:' + actionExtras[key]);
    //   }
    // }
    // Following event is only applicable for the android platform
    // if (eventName == 'CleverTapInAppNotificationShowed') {
    //    let type = event.data.type;
    //    console.log('Value for inApp type:', type);
    // }
};

export const _handleCleverTapPushEvent = (eventName, event) => {
    console.log('handleCleverTapPush', eventName, event);
    showToast(`${JSON.stringify(eventName)} called!`);

    // Uncomment to access payload for each events.
    // if (eventName == 'CleverTapPushNotificationClicked') {
    //   if (event['wzrk_dl'] != null) {
    //     let deepLink = event['wzrk_dl'];
    //     console.log('Push Notification clicked with deeplink: ' + deepLink);
    //   }
    // }
    // if (eventName == 'CleverTapPushPermissionResponseReceived') {
    //   let accepted = event['accepted'];
    //   console.log('Push Permission accepted:', accepted);
    // }
};

export const _handleCleverTapDisplayUnitsLoaded = (eventName, event) => {
    console.log('handleCleverTapDisplayUnitsLoaded', eventName, event);
    showToast(`${eventName} called!`);

    let data = event['displayUnits'];

    // Uncomment to access payload.
    // printDisplayUnitsPayload(data);
};

export const printDisplayUnitsPayload = (data) => {
    if (data != null) {
        console.log('Total Display units count = ' + data.length);
        data.forEach(element => {
            printDisplayUnit(element);
        });
    }
};

export const printDisplayUnit = (element) => {
    if (element != null) {
        let content = element['content'];
        content.forEach(contentElement => {
            let title = contentElement['title'];
            let message = contentElement['message'];
            console.log('Title text of display unit is: ' + title['text']);
            console.log('Message text of display unit is: ' + message['text']);
        });
        let customKV = element['custom_kv'];
        if (customKV != null) {
            console.log('Display units custom key-values: ', customKV);
            for (const key of Object.keys(customKV)) {
                console.log('Value for key: ' + key + ' is:' + customKV[key]);
            }
        }
    }
};